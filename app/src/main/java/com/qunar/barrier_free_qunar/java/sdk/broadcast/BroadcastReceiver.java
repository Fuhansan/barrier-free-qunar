package com.qunar.barrier_free_qunar.java.sdk.broadcast;

import android.content.Context;
import android.content.Intent;

import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.AIResponseData;
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.MessageData;
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.UserInputData;
import com.qunar.barrier_free_qunar.java.sdk.consts.BroadcastConst;
import com.qunar.barrier_free_qunar.java.sdk.consts.ReceiverConst;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天广播接收器
 * 专门处理聊天相关的广播消息
 */
public class BroadcastReceiver extends BaseBroadcastReceiver {
    private static final String TAG = "ChatBroadcastReceiver";
    
    private final Map<String,BroadcastListener> listenerMap;
    
    public BroadcastReceiver(BroadcastListener listener) {
        this.listenerMap = new ConcurrentHashMap<>();
        this.listenerMap.put(listener.primaryKey(), listener);
    }
    
    @Override
    protected String getReceiverId() {
        return ReceiverConst.CHAT_ACTIVITY_RECEIVER;
    }
    
    @Override
    protected void onMessageReceived(Context context, MessageData messageData) {
        BroadcastListener listener = getListener(messageData.broadcastKey);
        if (listener != null) {
            listener.onMessageReceived(messageData);
        }
    }
    
    @Override
    protected void onUserInputReceived(Context context, UserInputData inputData) {
        // Log.d(TAG, "接收到用户输入广播 - 内容长度: " + 
//               (inputData.userInput != null ? inputData.userInput.length() : 0));
        BroadcastListener listener = getListener(inputData.broadcastKey);
        if (listener != null) {
            listener.onUserInputReceived(inputData);
        }
    }
    
    @Override
    protected void onAIResponseReceived(Context context, AIResponseData responseData) {
        // Log.d(TAG, "接收到AI响应广播 - 响应时间: " + responseData.responseTime + "ms");
        BroadcastListener listener = getListener(responseData.broadcastKey);
        if (listener != null) {
            listener.onAIResponseReceived(responseData);
        }
    }
    
    @Override
    protected void onCustomBroadcastReceived(Context context, Intent intent, String action) {
        // Log.d(TAG, "接收到自定义广播: " + action);
        String broadcastKey = intent.getStringExtra(BroadcastConst.Extra.BROADCAST_PRIMARY_KEY);
        BroadcastListener listener = getListener(broadcastKey);
        if (listener != null) {
            listener.onCustomBroadcastReceived(intent, action);
        }
    }
    
    @Override
    protected void onError(Context context, Intent intent, Exception error) {
        String broadcastKey = intent.getStringExtra(BroadcastConst.Extra.BROADCAST_PRIMARY_KEY);
        BroadcastListener listener = getListener(broadcastKey);
        if (listener != null) {
            listener.onError(intent, error);
        }
    }


    private  BroadcastListener getListener(String broadcastKey){
        return  listenerMap.get(broadcastKey);
    }

}