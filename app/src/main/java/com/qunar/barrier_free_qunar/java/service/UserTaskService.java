package com.qunar.barrier_free_qunar.java.service;


import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.util.Log;



import com.qunar.barrier_free_qunar.java.sdk.BarrierFreeBuilder;
import com.qunar.barrier_free_qunar.java.sdk.action.ActionManager;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionResult;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BroadcastSender;
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.MessageRequest;
import com.qunar.barrier_free_qunar.java.sdk.consts.URLConst;
import com.qunar.barrier_free_qunar.java.sdk.gesture.GestureApi;
import com.qunar.barrier_free_qunar.java.sdk.model.BarrierFreeException;
import com.qunar.barrier_free_qunar.java.sdk.model.StreamEventData;
import com.qunar.barrier_free_qunar.java.sdk.model.UserTask;
import com.qunar.barrier_free_qunar.java.sdk.util.BFHttpUtils;
import com.qunar.barrier_free_qunar.java.sdk.util.LlmUtil;
import com.qunar.barrier_free_qunar.java.sdk.util.StreamEventParser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 执行用户命令：一次性只能执行一个，如若要执行下一个命令；必须终止
 */
public class UserTaskService {

    private GestureApi gestureApi;

    private AccessibilityService accessibilityService;

    private BroadcastSender broadcastSender;

    private ActionManager actionManager;

    // 用于跟踪已完成的任务，防止重复发送
    private final Set<String> completedTasks = ConcurrentHashMap.newKeySet();
    
    // 用于跟踪已处理的请求，防止重复请求
    private final Set<String> processedRequests = ConcurrentHashMap.newKeySet();
    
    // 用于同步onComplete方法的锁对象
    private final Object completeLock = new Object();

    public UserTaskService(AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
        this.gestureApi = BarrierFreeBuilder.buildGestureApi(this.accessibilityService);
        this.broadcastSender = new BroadcastSender(this.accessibilityService);
        this.actionManager = new ActionManager(this.gestureApi);
    }

    /**
     * 执行
     */
    @SuppressLint("RestrictedApi")
    public void run(UserTask userTask) {
        try {

            userTask.checkArgs();

            // 当前执行步数
            int currentStep = userTask.getCurrentStep();
            // 最大执行步数
            int limitStep = userTask.getLimitStep();


            String sessionId = UUID.randomUUID().toString().substring(0, 10);

            LlmUtil.LlmRequest llmRequest = LlmUtil.newRequest(sessionId, new String[]{"instruct_extractor", "operator"}, true);
            llmRequest.addUserText(userTask.getInstruction())
                    .addAssistantMessage(userTask.getThrough());

            Map<String, String> headers = llmRequest.getHeaders();

            // 这里先执行一波返回主页
            gestureApi.home();
            

            
            for (; currentStep < limitStep; currentStep++) {
                // 生成请求唯一标识，防止重复请求
                String requestKey = sessionId + "_step_" + currentStep;
                
                // 检查是否已经处理过这个请求
                if (processedRequests.contains(requestKey)) {
                    Log.d("【UserTaskService】", "请求已处理，跳过: " + requestKey);
                    continue;
                }
                
                // 标记请求为已处理
                processedRequests.add(requestKey);
                Log.d("【UserTaskService】", "开始处理请求: " + requestKey);
                
                // 每次循环创建新的CountDownLatch
                CountDownLatch stepLatch = new CountDownLatch(1);
                AtomicBoolean stepCompleted = new AtomicBoolean(false);
                AtomicBoolean isBroad = new AtomicBoolean(false);

                // 截个屏幕给模型
                String screenShotUrl = gestureApi.screenShot();
                String nConversationId = UUID.randomUUID().toString().substring(0, 10);
                sendMultimediaMessage("", screenShotUrl, userTask.getInstruction(), sessionId, nConversationId);
                llmRequest.addUserImage(screenShotUrl);
                Map<String, Object> requestBody = llmRequest.getRequestBody();

                // 调用大LLM
                BFHttpUtils.postObjectStream(URLConst.LLM_STREAM_URL, requestBody, headers, new BFHttpUtils.StreamCallback() {
                    private StringBuilder responseBuilder = new StringBuilder();
                    private String conversationId = "";
                    private StringBuilder finalContent = new StringBuilder();


                    @Override
                    public void onChunk(String chunk) {
                            responseBuilder.append(chunk);
                            // 使用StreamEventParser解析数据
                            StreamEventData eventDatum = StreamEventParser.parseChunk(chunk);
                            if (eventDatum != null && eventDatum.getEventType() != StreamEventData.EventType.UNKNOWN) {
                                // 更新conversationId（如果有的话）
                                if (eventDatum.getMessageId() != null && !eventDatum.getMessageId().isBlank()) {
                                    conversationId = eventDatum.getMessageId();
                                }
                                // 处理流式事件，让BroadcastSender内部决定是否发送
                                String content = broadcastSender.handleStreamEvent(eventDatum, chunk, sessionId, isBroad);
                                if (content != null && !content.isEmpty()) {
                                    finalContent.append(content);
                                }
                            }
                        }


                    @Override
                    public void onComplete() {
                        // 使用CAS确保只执行一次
                        if (!stepCompleted.compareAndSet(false, true)) {
                            return;
                        }
                        
                        // 生成任务唯一标识key，基于sessionId和conversationId
                        String taskKey = sessionId + "_" + (conversationId != null ? conversationId : "default");
                        
                        synchronized (completeLock) {
                            // 检查是否已经完成过相同的任务
                            if (completedTasks.contains(taskKey)) {
                                stepLatch.countDown();
                                return;
                            }
                            
                            // 标记任务为已完成
                            completedTasks.add(taskKey);
                        }
                        
                        try {
                            String executeStr = finalContent.toString();
                            Log.i("【执行操作指令】",  executeStr);
                            // 解析并执行finalContent中的动作指令
                            processActionCommands(executeStr);
                            llmRequest.addAssistantMessage(finalContent.toString());
                        } catch (Exception e) {
                            // 如果处理失败，从已完成集合中移除，允许重试
                            synchronized (completeLock) {
                                completedTasks.remove(taskKey);
                            }
                            // 同时从请求集合中移除，允许重试
                            processedRequests.remove(requestKey);
                        } finally {
                            // TODO 这里发送广播是异步的，这里应该join一下，后续做吧，现在循环里面waite一会吧
                            stepLatch.countDown();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // 使用CAS确保只执行一次
                        if (!stepCompleted.compareAndSet(false, true)) {
                            Log.d("【UserTaskService】", "步骤已完成，忽略错误回调: " + requestKey);
                            return;
                        }
                        
                        Log.e("【UserTaskService】", "stream处理任务时发生异常: " + error + ", requestKey: " + requestKey);
                        
                        // 发生错误时，从请求集合中移除，允许重试
                        processedRequests.remove(requestKey);
                        
                        stepLatch.countDown();
                    }
                });

                try {
                    stepLatch.await();
                    Log.d("【UserTaskService】", "步骤完成，等待下一步: " + requestKey);
                } catch (InterruptedException e) {
                    Log.e("【UserTaskService】", "等待步骤完成时被中断: " + requestKey, e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            

            
        } catch (BarrierFreeException bfe) {
            Log.w("【Barrier-Free】执行用户命令", "执行命令失败", bfe);
        } catch (Exception e) {
            Log.e("【Barrier-Free】执行用户命令", "执行命令失败", e);
        }
    }

    /**
     * 处理动作指令
     * 解析finalContent中的动作指令并执行
     *
     * @param content 包含动作指令的内容
     */
    private void processActionCommands(String content) {
        if (content == null || content.trim().isEmpty()) {
            Log.d("【ActionProcessor】", "内容为空，跳过动作处理");
            return;
        }

        Log.d("【ActionProcessor】", "开始处理动作指令: " + content);

        try {
            // 检查是否包含动作指令
            if (!actionManager.containsActions(content)) {
                Log.d("【ActionProcessor】", "内容中未发现动作指令");
                return;
            }

            Log.i("【ActionProcessor】", "发现动作指令，开始解析和执行");

            // 解析并执行动作指令
            List<ActionResult> results = actionManager.parseAndExecuteActions(content);

            // 处理执行结果
            processActionResults(results);

        } catch (Exception e) {
            Log.e("【ActionProcessor】", "处理动作指令时发生异常", e);
        }
    }

    /**
     * 处理动作执行结果
     *
     * @param results 动作执行结果列表
     */
    private void processActionResults(List<ActionResult> results) {
        if (results == null || results.isEmpty()) {
            Log.d("【ActionProcessor】", "没有动作执行结果");
            return;
        }

        Log.d("【ActionProcessor】", "处理 " + results.size() + " 个动作执行结果");

        int successCount = 0;
        int failureCount = 0;

        for (ActionResult result : results) {
            if (result.isSuccess()) {
                successCount++;
                Log.i("【ActionProcessor】", "动作执行成功: " + result.getMessage());

                // 如果是截屏操作，可以获取截屏URL
                if (result.getData() != null) {
                    Log.d("【ActionProcessor】", "动作执行返回数据: " + result.getData());
                }
            } else {
                failureCount++;
                Log.w("【ActionProcessor】", "动作执行失败: " + result.getMessage());

                if (result.getErrorCode() != null) {
                    Log.w("【ActionProcessor】", "错误代码: " + result.getErrorCode());
                }
            }
        }

        Log.i("【ActionProcessor】", String.format("动作执行完成 - 成功: %d, 失败: %d", successCount, failureCount));

        // 可以根据需要发送执行结果的广播消息
        if (successCount > 0) {
            Log.i("【ActionProcessor】", "所有动作指令处理完成");
        }
    }

    /**
     * 发送包含图片的多媒体消息
     *
     * @param textContent     文本内容
     * @param imageUrl        图片URL
     * @param originalMessage 原始消息
     * @param sessionId       会话ID
     */
    private void sendMultimediaMessage(String textContent, String imageUrl, String originalMessage, String sessionId, String conversationId) {
        try {
            // 创建多媒体数据
            MessageRequest.MultimediaData multimediaData = new MessageRequest.MultimediaData();
            multimediaData.setText(textContent);
            multimediaData.setImageData(imageUrl);
            multimediaData.setType(MessageRequest.MultimediaData.MultimediaType.IMAGE);

            // 创建多媒体消息请求
            MessageRequest multimediaRequest = new MessageRequest.Builder()
                    .broadCastKey(MessageRequest.BroadCastListenerKey.CHAT_MESSAGE_BROAD_CAST)
                    .sendMode(MessageRequest.SendMode.SHORT_CONNECTION)
                    .messageType(MessageRequest.MessageType.MULTIMEDIA)
                    .senderRole(MessageRequest.SenderRole.SYSTEM)
                    .multimediaData(multimediaData)
                    .originalMessage(originalMessage)
                    .sessionId(sessionId)
                    .conversationId(conversationId)
                    .build();

            // 发送多媒体消息
            broadcastSender.send(multimediaRequest);
        } catch (Exception e) {
            Log.e("【UserTaskService】", "发送多媒体消息失败", e);
        }
    }
    

}
