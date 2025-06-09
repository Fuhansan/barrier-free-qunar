package com.qunar.barrier_free_qunar.java.service;


import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.util.Log;


import com.qunar.barrier_free_qunar.java.sdk.BarrierFreeBuilder;
import com.qunar.barrier_free_qunar.java.sdk.action.ActionManager;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionResult;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BroadcastSender;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.model.MessageRequest;
import com.qunar.barrier_free_qunar.java.sdk.consts.URLConst;
import com.qunar.barrier_free_qunar.java.sdk.gesture.GestureApi;
import com.qunar.barrier_free_qunar.java.sdk.model.BarrierFreeException;
import com.qunar.barrier_free_qunar.java.sdk.model.StreamEventData;
import com.qunar.barrier_free_qunar.java.sdk.model.UserTask;
import com.qunar.barrier_free_qunar.java.sdk.util.BFHttpUtils;
import com.qunar.barrier_free_qunar.java.sdk.util.JsonUtil;
import com.qunar.barrier_free_qunar.java.sdk.util.LlmUtil;
import com.qunar.barrier_free_qunar.java.sdk.util.StreamEventParser;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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

            // 这里先执行一波返回主页
            gestureApi.home();
            // 截个屏幕给模型
            String screenShotUrl = gestureApi.screenShot();
            Log.d("【UserTaskService】", "截图返回URL：" + screenShotUrl + ",执行参数,currentStep:" + currentStep + ", 最大执行步长：" + limitStep);
            String sessionId = UUID.randomUUID().toString().substring(0, 10);


            sendMultimediaMessage("", screenShotUrl, userTask.getInstruction(), sessionId, "");


            LlmUtil.LlmRequest llmRequest = LlmUtil.newRequest(sessionId, new String[]{"instruct_extractor", "operator"}, true);

            llmRequest.addUserText(userTask.getInstruction())
                    .addAssistantMessage(userTask.getThrough())
                    .addUserImage(screenShotUrl);

            Map<String, String> headers = llmRequest.getHeaders();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            for (; currentStep < limitStep; currentStep++) {
                // 每次需要重置
                AtomicBoolean isBroad = new AtomicBoolean(false);
                Map<String, Object> requestBody = llmRequest.getRequestBody();


                Log.d("【执行操作指令】", "requestBody：" + JsonUtil.toJson(requestBody));

                // 调用大LLM
                BFHttpUtils.postObjectStream(URLConst.LLM_STREAM_URL, requestBody, headers, new BFHttpUtils.StreamCallback() {
                    private StringBuilder responseBuilder = new StringBuilder();
                    private String conversationId = "";
                    private StringBuilder finalContent = new StringBuilder();

                    @Override
                    public void onChunk(String chunk) {
                        Log.d("【UserTaskService】", "接收消息：" + chunk);
                        responseBuilder.append(chunk);
                        // 使用StreamEventParser解析数据
                        StreamEventData eventDatum = StreamEventParser.parseChunk(chunk);
                        if (eventDatum != null) {
                            // 创建包含图片的多媒体消息请求
                            String content = broadcastSender.handleStreamEvent(eventDatum, chunk, sessionId, isBroad);
                            finalContent.append(content);
                            if (!conversationId.isBlank() && eventDatum.getMessageId() != null && !eventDatum.getMessageId().isBlank()) {
                                conversationId = eventDatum.getMessageId();
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        // 解析并执行finalContent中的动作指令
                        processActionCommands(finalContent.toString());
                        String nextImage = gestureApi.screenShot();
                        llmRequest.addUserText(finalContent.toString())
                                .addUserImage(nextImage);
                        sendMultimediaMessage("", nextImage, finalContent.toString(), sessionId, "");
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(String error) {
                        countDownLatch.countDown();
                    }
                });

                countDownLatch.await();
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
            Log.d("【UserTaskService】", "发送多媒体消息成功，包含图片: " + imageUrl);

        } catch (Exception e) {
            Log.e("【UserTaskService】", "发送多媒体消息失败", e);
        }
    }

}
