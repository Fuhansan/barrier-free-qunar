package com.qunar.barrier_free_qunar.java.sdk.broadcast.sender;

import android.util.Log;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BroadcastManager;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.model.MessageRequest;
import com.qunar.barrier_free_qunar.java.sdk.consts.BroadcastConst;
import java.util.HashMap;
import java.util.Map;

/**
 * 短链接消息发送器
 * 实现一次性发送完整数据的逻辑
 */
public class ShortConnectionMessageSender extends MessageSender {
    
    private static final String TAG = "ShortConnectionMessageSender";
    
    private BroadcastManager broadcastManager;
    
    public ShortConnectionMessageSender() {
        // 注意：这里需要Context，实际使用时需要通过构造函数或setter传入
    }
    
    public void setBroadcastManager(BroadcastManager broadcastManager) {
        this.broadcastManager = broadcastManager;
    }
    
    @Override
    public boolean send(MessageRequest request) {
        if (!validateRequest(request)) {
            Log.e(TAG, "消息请求验证失败");
            return false;
        }
        
        if (broadcastManager == null) {
            Log.e(TAG, "BroadcastManager未初始化");
            return false;
        }
        
        try {
            switch (request.getMessageType()) {
                case TEXT:
                    return sendTextMessage(request);
                case MULTIMEDIA:
                    return sendMultimediaMessage(request);
                default:
                    Log.e(TAG, "不支持的消息类型: " + request.getMessageType());
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "短链接发送消息失败", e);
            return false;
        }
    }
    
    /**
     * 发送文字消息
     */
    private boolean sendTextMessage(MessageRequest request) {
        String content = request.getContent();
        if (content == null) {
            content = "";
        }
        
        // 构建消息数据
        Map<String, Object> extras = buildTextExtras(request, content);
        
        // 发送消息
        boolean success = broadcastManager.sendBroadcast(
            getActionByRole(request.getSenderRole()), 
            extras
        );
        
        if (success) {
            Log.d(TAG, "短链接文字消息发送成功");
        } else {
            Log.e(TAG, "短链接文字消息发送失败");
        }
        
        return success;
    }
    
    /**
     * 发送多媒体消息
     */
    private boolean sendMultimediaMessage(MessageRequest request) {
        MessageRequest.MultimediaData multimediaData = request.getMultimediaData();
        if (multimediaData == null) {
            Log.e(TAG, "多媒体数据为空");
            return false;
        }
        
        // 构建多媒体消息数据
        Map<String, Object> extras = buildMultimediaExtras(request);
        
        // 发送消息
        boolean success = broadcastManager.sendBroadcast(
            getActionByRole(request.getSenderRole()), 
            extras
        );
        
        if (success) {
            Log.d(TAG, "短链接多媒体消息发送成功");
        } else {
            Log.e(TAG, "短链接多媒体消息发送失败");
        }
        
        return success;
    }
    
    /**
     * 构建文字消息的额外数据
     */
    private Map<String, Object> buildTextExtras(MessageRequest request, String content) {
        Map<String, Object> extras = new HashMap<>();
        
        // 基础信息
        extras.put(BroadcastConst.Extra.ORIGINAL_MESSAGE, request.getOriginalMessage() != null ? request.getOriginalMessage() : "");
        extras.put(BroadcastConst.Extra.REPLY, content);
        extras.put(BroadcastConst.Extra.CONVERSATION_ID, request.getConversationId() != null ? request.getConversationId() : "");
        extras.put(BroadcastConst.Extra.TIMESTAMP, System.currentTimeMillis());
        
        // 发送者信息
        if (request.getSenderRole() == MessageRequest.SenderRole.USER) {
            extras.put(BroadcastConst.Extra.SENDER_NAME, BroadcastConst.SenderName.USER);
            extras.put(BroadcastConst.Extra.SENDER_ID, BroadcastConst.SenderId.USER);
            extras.put(BroadcastConst.Extra.USER_INPUT, content);
        } else {
            extras.put(BroadcastConst.Extra.SENDER_NAME, BroadcastConst.SenderName.AI_ASSISTANT);
            extras.put(BroadcastConst.Extra.SENDER_ID, BroadcastConst.SenderId.AI_ASSISTANT);
        }
        
        // 消息类型
        extras.put(BroadcastConst.Extra.MESSAGE_TYPE, "text");
        extras.put("is_streaming", false);
        extras.put("content_length", content.length());
        
        // 添加额外参数
        if (request.getExtraParams() != null) {
            extras.putAll(request.getExtraParams());
        }
        
        return extras;
    }
    
    /**
     * 构建多媒体消息的额外数据
     */
    private Map<String, Object> buildMultimediaExtras(MessageRequest request) {
        Map<String, Object> extras = new HashMap<>();
        MessageRequest.MultimediaData multimediaData = request.getMultimediaData();
        
        // 基础信息
        extras.put(BroadcastConst.Extra.ORIGINAL_MESSAGE, request.getOriginalMessage() != null ? request.getOriginalMessage() : "");
        extras.put(BroadcastConst.Extra.CONVERSATION_ID, request.getConversationId() != null ? request.getConversationId() : "");
        extras.put(BroadcastConst.Extra.TIMESTAMP, System.currentTimeMillis());
        
        // 发送者信息
        if (request.getSenderRole() == MessageRequest.SenderRole.USER) {
            extras.put(BroadcastConst.Extra.SENDER_NAME, BroadcastConst.SenderName.USER);
            extras.put(BroadcastConst.Extra.SENDER_ID, BroadcastConst.SenderId.USER);
        } else {
            extras.put(BroadcastConst.Extra.SENDER_NAME, BroadcastConst.SenderName.AI_ASSISTANT);
            extras.put(BroadcastConst.Extra.SENDER_ID, BroadcastConst.SenderId.AI_ASSISTANT);
        }
        
        // 多媒体信息
        extras.put(BroadcastConst.Extra.MESSAGE_TYPE, "multimedia");
        extras.put("multimedia_type", multimediaData.getType().name());
        extras.put("is_streaming", false);
        
        // 文字内容
        if (multimediaData.getText() != null && !multimediaData.getText().isEmpty()) {
            extras.put(BroadcastConst.Extra.REPLY, multimediaData.getText());
            extras.put("text_content", multimediaData.getText());
        }
        
        // 多媒体数据
        if (multimediaData.getAudioData() != null) {
            extras.put("audio_data", multimediaData.getAudioData());
            extras.put("has_audio", true);
        }
        if (multimediaData.getVideoData() != null) {
            extras.put("video_data", multimediaData.getVideoData());
            extras.put("has_video", true);
        }
        if (multimediaData.getImageData() != null) {
            extras.put("image_data", multimediaData.getImageData());
            extras.put("has_image", true);
        }
        
        // 添加额外参数
        if (request.getExtraParams() != null) {
            extras.putAll(request.getExtraParams());
        }
        
        return extras;
    }
    
    /**
     * 根据发送角色获取对应的广播Action
     */
    private String getActionByRole(MessageRequest.SenderRole senderRole) {
        switch (senderRole) {
            case USER:
                return BroadcastConst.Action.USER_INPUT;
            case SYSTEM:
                // 修复：前端只监听SIMULATE_MESSAGE，所以系统消息也发送到SIMULATE_MESSAGE
                return BroadcastConst.Action.SIMULATE_MESSAGE;
            default:
                return BroadcastConst.Action.SIMULATE_MESSAGE;
        }
    }
    
    @Override
    public String getSenderType() {
        return "短链接消息发送器";
    }
}