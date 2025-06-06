package com.qunar.barrier_free_qunar.java.sdk.broadcast;

import android.content.Context;
import android.util.Log;

import com.qunar.barrier_free_qunar.LogCollector;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.model.MessageRequest;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.sender.MessageSender;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.sender.StreamingMessageSender;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.sender.ShortConnectionMessageSender;
import com.qunar.barrier_free_qunar.java.sdk.consts.BroadcastConst;
import com.qunar.barrier_free_qunar.java.sdk.model.StreamEventData;
import com.qunar.barrier_free_qunar.java.sdk.util.StreamEventParser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 广播发送器 - 重构后的统一消息发送入口
 * 使用多态设计，根据发送模式选择具体的发送策略
 */
public class BroadcastSender {
    private static final String TAG = "BroadcastSender";
    private final BroadcastManager broadcastManager;
    private final StreamingMessageSender streamingSender;
    private final ShortConnectionMessageSender shortConnectionSender;

    private LogCollector logCollector;


    public BroadcastSender(Context context) {
        this.broadcastManager = BroadcastManager.getInstance(context);

        // 初始化发送器
        this.streamingSender = new StreamingMessageSender();
        this.streamingSender.setBroadcastManager(broadcastManager);

        this.shortConnectionSender = new ShortConnectionMessageSender();
        this.logCollector = LogCollector.Companion.getInstance();
        this.shortConnectionSender.setBroadcastManager(broadcastManager);
    }

    /**
     * 统一的消息发送入口（新架构）
     * 根据MessageRequest中的发送模式选择对应的发送策略
     *
     * @param request 消息发送请求
     * @return 是否发送成功
     */
    public boolean send(MessageRequest request) {
        if (request == null) {
            Log.e(TAG, "消息请求不能为空");
            return false;
        }

        if (request.getSendMode() == null) {
            Log.e(TAG, "发送模式不能为空");
            return false;
        }

        try {
            // 根据发送模式选择对应的发送器
            MessageSender sender = getSenderByMode(request.getSendMode());

            Log.d(TAG, "使用" + sender.getSenderType() + "发送消息");

            // 执行发送
            boolean success = sender.send(request);

            if (success) {
                Log.d(TAG, "消息发送成功 - 模式: " + request.getSendMode() +
                        ", 类型: " + request.getMessageType() +
                        ", 角色: " + request.getSenderRole());
            } else {
                Log.w(TAG, "消息发送失败 - 模式: " + request.getSendMode() +
                        ", 类型: " + request.getMessageType() +
                        ", 角色: " + request.getSenderRole());
            }

            return success;
        } catch (Exception e) {
            Log.e(TAG, "发送消息异常", e);
            return false;
        }
    }

    /**
     * 根据发送模式获取对应的发送器
     */
    private MessageSender getSenderByMode(MessageRequest.SendMode sendMode) {
        switch (sendMode) {
            case STREAMING:
                return streamingSender;
            case SHORT_CONNECTION:
                return shortConnectionSender;
            default:
                throw new IllegalArgumentException("不支持的发送模式: " + sendMode);
        }
    }

    /**
     * 处理流式事件数据
     * 配合新的状态机解析器处理SSE事件流
     *
     * @param eventData   解析后的事件数据
     * @param userMessage 用户原始消息
     */
    public String handleStreamEvent(StreamEventData eventData, String userMessage, String currentSessionId, AtomicBoolean isBroadcastingStarted) {
        String result = "";
        try {
            logCollector.d(TAG, "处理流式事件: " + eventData.getEventType() + ", 解析器状态: " + StreamEventParser.getCurrentState());
            switch (eventData.getEventType()) {
                case START_OF_LLM:
                    logCollector.i(TAG, "LLM流程开始");
                    // 重置广播状态
                    isBroadcastingStarted.set(false);
                    break;

                case AGENT_NAME:
                    String agentName = eventData.getAgentName();
                    StreamEventData.AgentType agentType = eventData.getAgentType();
                    logCollector.i(TAG, "检测到代理: " + agentName + ", 类型: " + agentType);

                    // 如果是需要广播的代理类型，准备开始广播
                    if (eventData.isBroadcastAgent()) {
                        logCollector.i(TAG, "代理 " + agentName + " 需要广播，准备开始广播模式");
                        // 可以在这里显示"正在思考..."等状态提示
                    } else {
                        logCollector.d(TAG, "代理 " + agentName + " 不需要广播");
                    }
                    break;

                case MESSAGE:
                    // 根据解析器状态决定是否开始广播
                    if (eventData.isShouldStartBroadcast()) {
                        isBroadcastingStarted.set(true);
                        logCollector.i(TAG, "接收到message事件，开始广播模式");
                    } else {
                        logCollector.d(TAG, "接收到message事件，但不满足广播条件" + userMessage);
                    }
                    break;

                case DELTA:
                    // 只有在广播已开始的情况下才处理delta内容
                    if (isBroadcastingStarted.get() && eventData.getContent() != null && !eventData.getContent().trim().isEmpty()) {
                        String conversationId = eventData.getMessageId() != null ?
                                eventData.getMessageId() : currentSessionId;

                        MessageRequest streamRequest = new MessageRequest.Builder()
                                .sendMode(MessageRequest.SendMode.STREAMING)
                                .messageType(MessageRequest.MessageType.TEXT)
                                .senderRole(MessageRequest.SenderRole.SYSTEM)
                                .content(eventData.getContent())
                                .originalMessage(userMessage)
                                .conversationId(conversationId)
                                .build();

                        result = eventData.getContent();
                        send(streamRequest);
                        logCollector.d(TAG, "发送流式消息内容: " + eventData.getContent());
                    } else if (!isBroadcastingStarted.get()) {
                        logCollector.d(TAG, "广播未开始，跳过delta内容: " + eventData.getContent());
                    }
                    break;

                case END_OF_LLM:
                    logCollector.i(TAG, "LLM流程结束，停止广播模式");
                    isBroadcastingStarted.set(false);
                    // 可以在这里发送流程结束的通知
                    break;

                case UNKNOWN:
                default:
                    logCollector.w(TAG, "未知或未处理的事件类型: " + eventData.getEventType());
                    break;
            }
        } catch (Exception e) {
            logCollector.e(TAG, "处理流式事件时出错: " + e.getMessage(), e);
        }

        return result;
    }

}