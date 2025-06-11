package com.qunar.barrier_free_qunar.java.sdk.broadcast;

import android.content.Intent;

import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.AIResponseData;
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.MessageData;
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.UserInputData;

public interface BroadcastListener {
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


        String primaryKey();
    }