package com.qunar.barrier_free_qunar.java;

import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;


import com.qunar.barrier_free_qunar.java.sdk.BarrierFreeBuilder;
import com.qunar.barrier_free_qunar.java.sdk.consts.URLConst;
import com.qunar.barrier_free_qunar.java.sdk.gesture.GestureApi;
import com.qunar.barrier_free_qunar.java.sdk.model.UserTask;
import com.qunar.barrier_free_qunar.java.sdk.model.http.HttpResult;
import com.qunar.barrier_free_qunar.java.sdk.util.BFHttpUtils;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BroadcastSender;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.model.MessageRequest;
import com.qunar.barrier_free_qunar.java.service.UserTaskControl;
import com.qunar.barrier_free_qunar.java.service.UserTaskService;
import com.qunar.barrier_free_qunar.LogCollector;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.qunar.barrier_free_qunar.java.sdk.model.StreamEventData;
import com.qunar.barrier_free_qunar.java.sdk.util.StreamEventParser;

public class GlobalAccessibilityService extends AccessibilityService {

    private static final String TAG = "GlobalAccessibilityService";
    private UserTaskService userTaskService;

    private String currentSessionId;
    private BroadcastSender broadcastSender;
    private LogCollector logCollector;




    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // 只监听点击事件，避免过多事件导致卡顿
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        // 添加必要的flags以获取完整的节点信息
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.packageNames = new String[]{getPackageName()};
        info.notificationTimeout = 100; // 增加超时时间，减少频繁触发
        setServiceInfo(info);
        userTaskService = UserTaskControl.build(this);
        broadcastSender = new BroadcastSender(this);
        logCollector = LogCollector.Companion.getInstance();

        logCollector.i(TAG, "无障碍服务已连接，包名: " + getPackageName());
        logCollector.i(TAG, "会话ID已生成: " + currentSessionId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }

        // 只处理本应用的事件
        if (!getPackageName().equals(event.getPackageName())) {
            return;
        }

        // 只处理点击事件
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null) {
                try {
                    String viewIdResourceName = source.getViewIdResourceName();
                    // 获取更多节点信息用于调试
                    android.graphics.Rect bounds = new android.graphics.Rect();
                    source.getBoundsInScreen(bounds);

                    // 通过ID判断是否为发送按钮
                    boolean isSendButton = "com.qunar.barrier_free_qunar:id/btn_send".equals(viewIdResourceName);
                    if (isSendButton) {
                        logCollector.i(TAG, "✅ 检测到发送按钮点击！");
                        // 获取输入框的文字内容
                        String userMessage = getInputText();
                        if (userMessage != null && !userMessage.trim().isBlank()) {
                            logCollector.i(TAG, "获取到用户输入: " + userMessage);
                            // 调用HTTP接口处理用户消息
                             sendMessageToAPI(userMessage, "");
                        }
                    }
                } catch (Exception e) {
                    logCollector.e(TAG, "处理点击事件时出错: " + e.getMessage(), e);
                } finally {
                    source.recycle();
                }
            }
        }
    }

    /**
     * 获取聊天记录中用户的最后一句话
     *
     * @return 用户最后发送的消息内容，如果获取失败返回null
     */
    private String getInputText() {
        try {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                String lastUserMessage = findLastUserMessage(rootNode);
                rootNode.recycle();
                return lastUserMessage;
            }
        } catch (Exception e) {
            Log.e("【无障碍服务】错误", "获取用户最后消息时出错: " + e.getMessage());
        }
        return null;
    }

    /**
     * 递归查找聊天记录中用户的最后一条消息
     *
     * @param node 当前节点
     * @return 用户最后发送的消息内容
     */
    private String findLastUserMessage(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        try {
            // 查找RecyclerView
            String viewIdResourceName = node.getViewIdResourceName();
            if ("com.qunar.barrier_free_qunar:id/rv_messages".equals(viewIdResourceName)) {
                // Log.d("【无障碍服务】聊天记录", "找到聊天记录RecyclerView");
                return getLastUserMessageFromRecyclerView(node);
            }

            // 递归查找子节点
            for (int i = 0; i < node.getChildCount() ; i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    String result = findLastUserMessage(child);
                    child.recycle();
                    if (result != null) {
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("【无障碍服务】错误", "查找用户最后消息时出错: " + e.getMessage());
        }

        return null;
    }

    /**
     * 从RecyclerView中获取用户最后发送的消息
     *
     * @param recyclerView RecyclerView节点
     * @return 用户最后发送的消息内容
     */
    private String getLastUserMessageFromRecyclerView(AccessibilityNodeInfo recyclerView) {
        if (recyclerView == null) {
            return null;
        }

        try {
            // 从最后一个子项开始向前查找用户发送的消息
            for (int i = recyclerView.getChildCount() - 2; i >= 0; i--) {
                AccessibilityNodeInfo messageItem = recyclerView.getChild(i);
                if (messageItem != null) {
                    try {
                        // 检查是否是用户发送的消息（通过布局来判断）
                        String messageContent = getUserMessageFromItem(messageItem);
                        if (messageContent != null && !messageContent.trim().isEmpty()) {
                            // Log.d("【无障碍服务】用户消息", "找到用户最后消息: " + messageContent);
                            return messageContent;
                        }
                    } finally {
                        messageItem.recycle();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("【无障碍服务】错误", "从RecyclerView获取用户消息时出错: " + e.getMessage());
        }

        return null;
    }

    /**
     * 从消息项中获取用户发送的消息内容
     *
     * @param messageItem 消息项节点
     * @return 如果是用户发送的消息则返回内容，否则返回null
     */
    private String getUserMessageFromItem(AccessibilityNodeInfo messageItem) {
        if (messageItem == null) {
            return null;
        }

        try {
            // 查找消息内容TextView
            String messageContent = findMessageContent(messageItem);
            if (messageContent != null) {
                // 通过检查布局特征判断是否是用户发送的消息
                // 用户发送的消息通常在右侧，可以通过布局参数或者特定的背景来判断
                if (isUserSentMessage(messageItem)) {
                    return messageContent;
                }
            }
        } catch (Exception e) {
            Log.e("【无障碍服务】错误", "从消息项获取用户消息时出错: " + e.getMessage());
        }

        return null;
    }

    /**
     * 查找消息内容
     *
     * @param node 节点
     * @return 消息内容
     */
    private String findMessageContent(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        try {
            // 查找tv_message
            String viewIdResourceName = node.getViewIdResourceName();
            if ("com.qunar.barrier_free_qunar:id/tv_message".equals(viewIdResourceName)) {
                CharSequence text = node.getText();
                return text != null ? text.toString() : null;
            }

            // 递归查找子节点
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    String result = findMessageContent(child);
                    child.recycle();
                    if (result != null) {
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("【无障碍服务】错误", "查找消息内容时出错: " + e.getMessage());
        }

        return null;
    }

    /**
     * 判断是否是用户发送的消息
     *
     * @param messageItem 消息项节点
     * @return 是否是用户发送的消息
     */
    private boolean isUserSentMessage(AccessibilityNodeInfo messageItem) {
        if (messageItem == null) {
            return false;
        }

        try {
            // 通过查找是否包含发送者名称来判断
            // 用户发送的消息通常没有发送者名称显示
            boolean hasSenderName = findSenderName(messageItem) != null;
            // 如果没有发送者名称，通常是用户自己发送的消息
            return !hasSenderName;
        } catch (Exception e) {
            Log.e("【无障碍服务】错误", "判断消息发送者时出错: " + e.getMessage());
        }

        return false;
    }

    /**
     * 查找发送者名称
     *
     * @param node 节点
     * @return 发送者名称，如果没有返回null
     */
    private String findSenderName(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        try {
            // 查找tv_sender_name
            String viewIdResourceName = node.getViewIdResourceName();
            if ("com.qunar.barrier_free_qunar:id/tv_sender_name".equals(viewIdResourceName)) {
                CharSequence text = node.getText();
                return text != null ? text.toString() : null;
            }

            // 递归查找子节点
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    String result = findSenderName(child);
                    child.recycle();
                    if (result != null) {
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("【无障碍服务】错误", "查找发送者名称时出错: " + e.getMessage());
        }

        return null;
    }




    /**
     * 发送消息到API
     *
     * @param userMessage 用户消息
     */
    private void sendMessageToAPI(String userMessage, String sessionId) {
        logCollector.i(TAG, "开始发送消息到API，使用流式请求starting：" + userMessage);
        AtomicBoolean isBroadcastingStarted = new AtomicBoolean(false); // 标记是否已开始广播
        // 在后台线程执行网络请求，避免NetworkOnMainThreadException
        new Thread(() -> {
            try {
                // 构建请求头
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "text/event-stream");

                // 构建请求体 - 使用新的消息格式
                Map<String, Object> requestBody = new HashMap<>();

                // 构建messages数组
                Map<String, Object> userMessageObj = new HashMap<>();
                userMessageObj.put("role", "user");
                userMessageObj.put("content", userMessage);

                Map<String, Object>[] messages = new Map[]{userMessageObj};
                requestBody.put("messages", messages);

                // 添加其他配置参数
                requestBody.put("debug", false);
                requestBody.put("deep_thinking_mode", false);
                requestBody.put("search_before_planning", false);
                requestBody.put("team_members", null);

                // 保留原有的会话和流式控制参数
                requestBody.put("sessionId", sessionId);
                requestBody.put("timestamp", System.currentTimeMillis());
                String[] teamMembers = new String[]{"instruct_extractor"};
                requestBody.put("team_members", teamMembers);


                StreamEventParser.resetState();
                final StreamEventData.AgentType[] instructExt = {null};
                    // 使用流式POST请求
                BFHttpUtils.postObjectStream(URLConst.LLM_STREAM_URL, requestBody, headers, new BFHttpUtils.StreamCallback() {
                        private StringBuilder responseBuilder = new StringBuilder();

                        @Override
                        public void onChunk(String chunk) {
                            logCollector.d(TAG, "接收到新的响应片段: " + chunk);
                            // 使用StreamEventParser解析数据
                            StreamEventData  eventDatum = StreamEventParser.parseChunk(chunk);

                            if (eventDatum != null) {
                                logCollector.d(TAG, "解析成功，事件类型: " + eventDatum.getEventType());
                                if(instructExt[0] == null || instructExt[0] != StreamEventData.AgentType.INSTRUCT_EXTRACTOR){
                                    instructExt[0] = eventDatum.getCurrConvAgentType();
                                }

                                String sendContent = broadcastSender.handleStreamEvent(eventDatum, userMessage, currentSessionId, isBroadcastingStarted);
                                responseBuilder.append(sendContent);
                            }
                        }

                        @Override
                        public void onComplete() {
                            StreamEventData.AgentType agentType = instructExt[0];
                            logCollector.d(TAG, "接收数据完成: " + agentType + "数据：" + responseBuilder.toString());
                            // 这里判断AgentType是不是到了INSTRUCT_EXTRACTOR
                            if (agentType == StreamEventData.AgentType.INSTRUCT_EXTRACTOR) {
                                // 这里需要去执行operator的任务
                                userTaskService.run(UserTaskControl.build(userMessage, responseBuilder.toString()));
                            }
                        }
                        
                        @Override
                        public void onError(String error) {
                            logCollector.e(TAG, "流式请求失败: " + error);
                            currentSessionId = UUID.randomUUID().toString().substring(0, 20);
                            // 发送错误消息广播
                            MessageRequest errorRequest = new MessageRequest.Builder()
                                .sendMode(MessageRequest.SendMode.STREAMING)
                                .messageType(MessageRequest.MessageType.TEXT)
                                .senderRole(MessageRequest.SenderRole.SYSTEM)
                                .content("抱歉，网络请求失败: " + error)
                                .originalMessage(userMessage)
                                .conversationId(currentSessionId)
                                .build();
                            broadcastSender.send(errorRequest);
                        }
                });


            } catch (Exception e) {
                logCollector.e(TAG, "发送消息到API时出错: " + e.getMessage(), e);
                // 发送错误消息广播
                MessageRequest errorRequest = new MessageRequest.Builder()
                    .sendMode(MessageRequest.SendMode.SHORT_CONNECTION)
                    .messageType(MessageRequest.MessageType.TEXT)
                    .senderRole(MessageRequest.SenderRole.SYSTEM)
                    .content("抱歉，网络请求失败。请检查网络连接后重试。")
                    .originalMessage(userMessage)
                    .conversationId(currentSessionId)
                    .build();
                broadcastSender.send(errorRequest);
            }
        }).start();
    }


    @Override
    public void onInterrupt() {

    }


    @Override
    public boolean onUnbind(Intent intent) {
        // 服务关闭时调用，释放资源
        if (logCollector != null) {
            logCollector.i(TAG, "无障碍服务已断开连接");
        }
        return super.onUnbind(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        logCollector = LogCollector.Companion.getInstance();
        logCollector.i(TAG, "无障碍服务已创建");
        // 初始化服务
        userTaskService = new UserTaskService(this);
        // 初始化广播发送器
        broadcastSender = new BroadcastSender(this);
        // 生成会话ID
        currentSessionId = "session_" + System.currentTimeMillis();
        // Log.d("【无障碍服务】", "当前会话ID: " + currentSessionId);
    }



}
