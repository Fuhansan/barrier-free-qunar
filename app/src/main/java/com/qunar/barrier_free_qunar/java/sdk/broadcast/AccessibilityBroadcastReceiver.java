package com.qunar.barrier_free_qunar.java.sdk.broadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 无障碍服务广播接收器
 * 专门处理无障碍服务相关的广播消息
 */
public class AccessibilityBroadcastReceiver extends BaseBroadcastReceiver {
    private static final String TAG = "AccessibilityBroadcastReceiver";
    
    private final AccessibilityBroadcastListener listener;
    
    public AccessibilityBroadcastReceiver(AccessibilityBroadcastListener listener) {
        this.listener = listener;
    }
    
    @Override
    protected void onMessageReceived(Context context, MessageData messageData) {

        
        if (listener != null) {
            listener.onMessageReceived(messageData);
        }
    }
    
    @Override
    protected void onUserInputReceived(Context context, UserInputData inputData) {
        // Log.d(TAG, "无障碍服务接收到用户输入广播 - 内容长度: " + 
//               (inputData.userInput != null ? inputData.userInput.length() : 0));
        
        if (listener != null) {
            listener.onUserInputReceived(inputData);
        }
    }
    
    @Override
    protected void onAIResponseReceived(Context context, AIResponseData responseData) {
        // Log.d(TAG, "无障碍服务接收到AI响应广播 - 响应时间: " + responseData.responseTime + "ms");
        
        if (listener != null) {
            listener.onAIResponseReceived(responseData);
        }
    }
    
    @Override
    protected void onCustomBroadcastReceived(Context context, Intent intent, String action) {
        // Log.d(TAG, "无障碍服务接收到自定义广播: " + action);
        
        if (listener != null) {
            listener.onCustomBroadcastReceived(intent, action);
        }
    }
    
    @Override
    protected void onError(Context context, Intent intent, Exception error) {
        Log.e(TAG, "无障碍服务处理广播时发生错误: " + (intent != null ? intent.getAction() : "unknown"), error);
        
        if (listener != null) {
            listener.onError(intent, error);
        }
    }
    
    /**
     * 无障碍服务广播监听器接口
     */
    public interface AccessibilityBroadcastListener {
        /**
         * 接收到消息广播
         * @param messageData 消息数据
         */
        void onMessageReceived(MessageData messageData);
        
        /**
         * 接收到用户输入广播（可选实现）
         * @param inputData 输入数据
         */
        default void onUserInputReceived(UserInputData inputData) {
            // 默认空实现
        }
        
        /**
         * 接收到AI响应广播（可选实现）
         * @param responseData 响应数据
         */
        default void onAIResponseReceived(AIResponseData responseData) {
            // 默认空实现
        }
        
        /**
         * 接收到自定义广播（可选实现）
         * @param intent 广播意图
         * @param action 广播动作
         */
        default void onCustomBroadcastReceived(Intent intent, String action) {
            // 默认空实现
        }
        
        /**
         * 错误处理（可选实现）
         * @param intent 广播意图
         * @param error 错误信息
         */
        default void onError(Intent intent, Exception error) {
            // 默认空实现
        }
    }
}