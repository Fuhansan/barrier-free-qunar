package com.qunar.barrier_free_qunar.java.sdk.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.AIResponseData;
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.MessageData;
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.UserInputData;
import com.qunar.barrier_free_qunar.java.sdk.consts.BroadcastConst;

import java.util.HashMap;
import java.util.Map;

/**
 * 广播接收器基类
 * 提供统一的广播接收处理逻辑和回调接口
 */
public abstract class BaseBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BaseBroadcastReceiver";
    
    /**
     * 获取当前接收器的唯一标识
     * 子类必须实现此方法以提供接收器ID
     */
    protected abstract String getReceiverId();
    
    @Override
    public final void onReceive(Context context, Intent intent) {
        try {

            if (intent == null || intent.getAction() == null) {
                Log.w(TAG, "接收到空的广播或动作");
                return;
            }
            
            String action = intent.getAction();
            
            // 检查目标接收器过滤
            String targetReceiverId = intent.getStringExtra(BroadcastConst.Extra.BROADCAST_PRIMARY_KEY);

            if (targetReceiverId == null || targetReceiverId.trim().isEmpty()) {
                Log.i(TAG, "广播接收器收到消息,targetReceiverId=" + targetReceiverId + "过滤");
                return;
            }

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
        String broadcastPrimaryKey = intent.getStringExtra(BroadcastConst.Extra.BROADCAST_PRIMARY_KEY);

        long timestamp = intent.getLongExtra(BroadcastConst.Extra.TIMESTAMP, System.currentTimeMillis());
        
        // 解析多媒体字段
        String imageData = intent.getStringExtra(BroadcastConst.Extra.IMAGE_DATA);
        String videoData = intent.getStringExtra(BroadcastConst.Extra.VIDEO_DATA);
        String audioData = intent.getStringExtra(BroadcastConst.Extra.AUDIO_DATA);
        String multimediaType = intent.getStringExtra(BroadcastConst.Extra.MULTIMEDIA_TYPE);
        String multimediaData = intent.getStringExtra(BroadcastConst.Extra.MULTIMEDIA_DATA);
        
        // 提取额外参数
        Map<String, Object> extras = new HashMap<>();
        // 检查是否有输入控制相关的额外参数

        if (intent.hasExtra("action")) {
            extras.put("action", intent.getStringExtra("action"));
        }
        
        MessageData messageData = new MessageData(
                broadcastPrimaryKey,
            originalMessage, reply, senderName, senderId, messageType, sessionId, conversationId, timestamp,
            imageData, videoData, audioData, multimediaType, multimediaData, extras
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
        String broadcastKey = intent.getStringExtra(BroadcastConst.Extra.BROADCAST_PRIMARY_KEY);
        long timestamp = intent.getLongExtra(BroadcastConst.Extra.TIMESTAMP, System.currentTimeMillis());
        
        UserInputData inputData = new UserInputData(broadcastKey, userInput, sessionId, timestamp);
        
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
        String broadcastKey = intent.getStringExtra(BroadcastConst.Extra.BROADCAST_PRIMARY_KEY);

        long timestamp = intent.getLongExtra(BroadcastConst.Extra.TIMESTAMP, System.currentTimeMillis());
        long responseTime = intent.getLongExtra(BroadcastConst.Extra.RESPONSE_TIME, 0);
        
        AIResponseData responseData = new AIResponseData(broadcastKey,
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
}