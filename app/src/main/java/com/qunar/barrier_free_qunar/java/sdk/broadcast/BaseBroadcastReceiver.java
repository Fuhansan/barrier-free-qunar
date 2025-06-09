package com.qunar.barrier_free_qunar.java.sdk.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.qunar.barrier_free_qunar.java.sdk.consts.BroadcastConst;

/**
 * 广播接收器基类
 * 提供统一的广播接收处理逻辑和回调接口
 */
public abstract class BaseBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BaseBroadcastReceiver";
    
    @Override
    public final void onReceive(Context context, Intent intent) {
        try {
            if (intent == null || intent.getAction() == null) {
                Log.w(TAG, "接收到空的广播或动作");
                return;
            }
            
            String action = intent.getAction();

            
            // 根据不同的广播动作分发处理
            switch (action) {
                case BroadcastConst.Action.SIMULATE_MESSAGE:
                    handleMessageBroadcast(context, intent);
                    break;
                case BroadcastConst.Action.USER_INPUT:
                    handleUserInputBroadcast(context, intent);
                    break;
                case BroadcastConst.Action.AI_RESPONSE:
                    handleAIResponseBroadcast(context, intent);
                    break;
                default:
                    handleCustomBroadcast(context, intent, action);
                    break;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "处理广播时发生错误: " + (intent != null ? intent.getAction() : "unknown"), e);
            onError(context, intent, e);
        }
    }
    
    /**
     * 处理消息广播
     * @param context 上下文
     * @param intent 广播意图
     */
    private void handleMessageBroadcast(Context context, Intent intent) {
        String originalMessage = intent.getStringExtra(BroadcastConst.Extra.ORIGINAL_MESSAGE);
        String reply = intent.getStringExtra(BroadcastConst.Extra.REPLY);
        String senderName = intent.getStringExtra(BroadcastConst.Extra.SENDER_NAME);
        String senderId = intent.getStringExtra(BroadcastConst.Extra.SENDER_ID);
        String messageType = intent.getStringExtra(BroadcastConst.Extra.MESSAGE_TYPE);
        String sessionId = intent.getStringExtra(BroadcastConst.Extra.SESSION_ID);
        String conversationId = intent.getStringExtra(BroadcastConst.Extra.CONVERSATION_ID);
        long timestamp = intent.getLongExtra(BroadcastConst.Extra.TIMESTAMP, System.currentTimeMillis());
        
        // 解析多媒体字段
        String imageData = intent.getStringExtra(BroadcastConst.Extra.IMAGE_DATA);
        String videoData = intent.getStringExtra(BroadcastConst.Extra.VIDEO_DATA);
        String audioData = intent.getStringExtra(BroadcastConst.Extra.AUDIO_DATA);
        String multimediaType = intent.getStringExtra(BroadcastConst.Extra.MULTIMEDIA_TYPE);
        String multimediaData = intent.getStringExtra(BroadcastConst.Extra.MULTIMEDIA_DATA);
        
        MessageData messageData = new MessageData(
            originalMessage, reply, senderName, senderId, messageType, sessionId, conversationId, timestamp,
            imageData, videoData, audioData, multimediaType, multimediaData
        );
        
        onMessageReceived(context, messageData);
    }
    
    /**
     * 处理用户输入广播
     * @param context 上下文
     * @param intent 广播意图
     */
    private void handleUserInputBroadcast(Context context, Intent intent) {
        String userInput = intent.getStringExtra(BroadcastConst.Extra.USER_INPUT);
        String sessionId = intent.getStringExtra(BroadcastConst.Extra.SESSION_ID);
        long timestamp = intent.getLongExtra(BroadcastConst.Extra.TIMESTAMP, System.currentTimeMillis());
        
        UserInputData inputData = new UserInputData(userInput, sessionId, timestamp);
        
        onUserInputReceived(context, inputData);
    }
    
    /**
     * 处理AI响应广播
     * @param context 上下文
     * @param intent 广播意图
     */
    private void handleAIResponseBroadcast(Context context, Intent intent) {
        String originalMessage = intent.getStringExtra(BroadcastConst.Extra.ORIGINAL_MESSAGE);
        String reply = intent.getStringExtra(BroadcastConst.Extra.REPLY);
        String sessionId = intent.getStringExtra(BroadcastConst.Extra.SESSION_ID);
        long timestamp = intent.getLongExtra(BroadcastConst.Extra.TIMESTAMP, System.currentTimeMillis());
        long responseTime = intent.getLongExtra(BroadcastConst.Extra.RESPONSE_TIME, 0);
        
        AIResponseData responseData = new AIResponseData(
            originalMessage, reply, sessionId, timestamp, responseTime
        );
        
        onAIResponseReceived(context, responseData);
    }
    
    /**
     * 处理自定义广播
     * @param context 上下文
     * @param intent 广播意图
     * @param action 广播动作
     */
    private void handleCustomBroadcast(Context context, Intent intent, String action) {
        onCustomBroadcastReceived(context, intent, action);
    }
    
    // 抽象方法，由子类实现具体的处理逻辑
    
    /**
     * 处理消息广播
     * @param context 上下文
     * @param messageData 消息数据
     */
    protected abstract void onMessageReceived(Context context, MessageData messageData);
    
    /**
     * 处理用户输入广播（可选实现）
     * @param context 上下文
     * @param inputData 输入数据
     */
    protected void onUserInputReceived(Context context, UserInputData inputData) {
        // 默认空实现，子类可选择性重写
    }
    
    /**
     * 处理AI响应广播（可选实现）
     * @param context 上下文
     * @param responseData 响应数据
     */
    protected void onAIResponseReceived(Context context, AIResponseData responseData) {
        // 默认空实现，子类可选择性重写
    }
    
    /**
     * 处理自定义广播（可选实现）
     * @param context 上下文
     * @param intent 广播意图
     * @param action 广播动作
     */
    protected void onCustomBroadcastReceived(Context context, Intent intent, String action) {
        // 默认空实现，子类可选择性重写
    }
    
    /**
     * 错误处理（可选实现）
     * @param context 上下文
     * @param intent 广播意图
     * @param error 错误信息
     */
    protected void onError(Context context, Intent intent, Exception error) {
        // 默认空实现，子类可选择性重写
    }
    
    // 数据类定义
    
    /**
     * 消息数据类
     */
    public static class MessageData {
        public final String originalMessage;
        public final String reply;
        public final String senderName;
        public final String senderId;
        public final String messageType;
        public final String sessionId;
        public final String conversationId;
        public final long timestamp;
        
        // 多媒体字段
        public final String imageData;
        public final String videoData;
        public final String audioData;
        public final String multimediaType;
        public final String multimediaData;
        
        public MessageData(String originalMessage, String reply, String senderName, 
                          String senderId, String messageType, String sessionId, String conversationId, long timestamp) {
            this(originalMessage, reply, senderName, senderId, messageType, sessionId, conversationId, timestamp,
                 null, null, null, null, null);
        }
        
        public MessageData(String originalMessage, String reply, String senderName, 
                          String senderId, String messageType, String sessionId, String conversationId, long timestamp,
                          String imageData, String videoData, String audioData, String multimediaType, String multimediaData) {
            this.originalMessage = originalMessage;
            this.reply = reply;
            this.senderName = senderName;
            this.senderId = senderId;
            this.messageType = messageType;
            this.sessionId = sessionId;
            this.conversationId = conversationId;
            this.timestamp = timestamp;
            this.imageData = imageData;
            this.videoData = videoData;
            this.audioData = audioData;
            this.multimediaType = multimediaType;
            this.multimediaData = multimediaData;
        }
        
        public boolean isStreamChunk() {
            return BroadcastConst.MessageType.STREAM_CHUNK.equals(messageType);
        }
        
        public boolean isError() {
            return BroadcastConst.MessageType.ERROR.equals(messageType);
        }
        
        public boolean isSystemMessage() {
            return BroadcastConst.MessageType.SYSTEM.equals(messageType);
        }
        
        public boolean isTextMessage() {
            return BroadcastConst.MessageType.TEXT.equals(messageType);
        }
        
        public boolean isImageMessage() {
            return BroadcastConst.MessageType.IMAGE.equals(messageType);
        }
        
        public boolean isVideoMessage() {
            return BroadcastConst.MessageType.VIDEO.equals(messageType);
        }
        
        public boolean isAudioMessage() {
            return BroadcastConst.MessageType.AUDIO.equals(messageType);
        }
        
        public boolean isMultimediaMessage() {
            return BroadcastConst.MessageType.MULTIMEDIA.equals(messageType);
        }
        
        public boolean hasMultimediaContent() {
            return imageData != null || videoData != null || audioData != null || multimediaData != null;
        }
    }
    
    /**
     * 用户输入数据类
     */
    public static class UserInputData {
        public final String userInput;
        public final String sessionId;
        public final long timestamp;
        
        public UserInputData(String userInput, String sessionId, long timestamp) {
            this.userInput = userInput;
            this.sessionId = sessionId;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * AI响应数据类
     */
    public static class AIResponseData {
        public final String originalMessage;
        public final String reply;
        public final String sessionId;
        public final long timestamp;
        public final long responseTime;
        
        public AIResponseData(String originalMessage, String reply, String sessionId, 
                             long timestamp, long responseTime) {
            this.originalMessage = originalMessage;
            this.reply = reply;
            this.sessionId = sessionId;
            this.timestamp = timestamp;
            this.responseTime = responseTime;
        }
    }
}