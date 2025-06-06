package com.qunar.barrier_free_qunar.java.sdk.broadcast.sender;

import android.util.Log;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BroadcastManager;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.model.MessageRequest;
import com.qunar.barrier_free_qunar.java.sdk.consts.BroadcastConst;
import java.util.HashMap;
import java.util.Map;

/**
 * 流式消息发送器
 * 实现流式发送逻辑，数据分块传输，前端实时显示
 */
public class StreamingMessageSender extends MessageSender {
    
    private static final String TAG = "StreamingMessageSender";
    private static final int DEFAULT_CHUNK_SIZE = 50; // 默认每块字符数
    
    private BroadcastManager broadcastManager;
    
    public StreamingMessageSender() {
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
                    return sendTextStreaming(request);
                case MULTIMEDIA:
                    return sendMultimediaStreaming(request);
                default:
                    Log.e(TAG, "不支持的消息类型: " + request.getMessageType());
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "流式发送消息失败", e);
            return false;
        }
    }
    
    /**
     * 流式发送文字消息
     */
    private boolean sendTextStreaming(MessageRequest request) {
        String content = request.getContent();
        if (content == null || content.isEmpty()) {
            return false;
        }
        
        // 分块发送
        int chunkSize = getChunkSize(request);
        int totalChunks = (int) Math.ceil((double) content.length() / chunkSize);
        
        for (int i = 0; i < totalChunks; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, content.length());
            String chunk = content.substring(start, end);
            
            // 构建流式消息数据
            Map<String, Object> extras = buildStreamingExtras(request, chunk, i, totalChunks);
            
            // 发送流式块
            boolean success = broadcastManager.sendBroadcast(
                BroadcastConst.Action.SIMULATE_MESSAGE, 
                extras
            );
            
            if (!success) {
                Log.e(TAG, "发送第" + (i + 1) + "块流式消息失败");
                return false;
            }
            
            // 添加小延迟，模拟真实的流式效果
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 发送流式结束标记
        sendStreamingEndMarker(request);
        
        Log.d(TAG, "流式文字消息发送完成，共" + totalChunks + "块");
        return true;
    }
    
    /**
     * 流式发送多媒体消息
     */
    private boolean sendMultimediaStreaming(MessageRequest request) {
        MessageRequest.MultimediaData multimediaData = request.getMultimediaData();
        if (multimediaData == null) {
            return false;
        }
        
        // 先发送文字部分（如果有）
        if (multimediaData.getText() != null && !multimediaData.getText().isEmpty()) {
            MessageRequest textRequest = new MessageRequest.Builder()
                .sendMode(request.getSendMode())
                .messageType(MessageRequest.MessageType.TEXT)
                .senderRole(request.getSenderRole())
                .content(multimediaData.getText())
                .originalMessage(request.getOriginalMessage())
                .conversationId(request.getConversationId())
                .extraParams(request.getExtraParams())
                .build();
            
            if (!sendTextStreaming(textRequest)) {
                return false;
            }
        }
        
        // 发送多媒体数据
        Map<String, Object> extras = buildMultimediaExtras(request);
        boolean success = broadcastManager.sendBroadcast(
            BroadcastConst.Action.SIMULATE_MESSAGE, 
            extras
        );
        
        if (success) {
            Log.d(TAG, "流式多媒体消息发送完成");
        }
        
        return success;
    }
    
    /**
     * 构建流式消息的额外数据
     */
    private Map<String, Object> buildStreamingExtras(MessageRequest request, String chunk, int chunkIndex, int totalChunks) {
        Map<String, Object> extras = new HashMap<>();
        
        // 基础信息
        extras.put(BroadcastConst.Extra.ORIGINAL_MESSAGE, request.getOriginalMessage() != null ? request.getOriginalMessage() : "");
        extras.put(BroadcastConst.Extra.REPLY, chunk);
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
        
        // 流式特定信息
        extras.put(BroadcastConst.Extra.MESSAGE_TYPE, "stream_chunk");
        extras.put("chunk_index", chunkIndex);
        extras.put("total_chunks", totalChunks);
        extras.put("chunk_length", chunk.length());
        extras.put("is_streaming", true);
        
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
        
        if (multimediaData.getAudioData() != null) {
            extras.put("audio_data", multimediaData.getAudioData());
        }
        if (multimediaData.getVideoData() != null) {
            extras.put("video_data", multimediaData.getVideoData());
        }
        if (multimediaData.getImageData() != null) {
            extras.put("image_data", multimediaData.getImageData());
        }
        
        extras.put("is_streaming", true);
        
        // 添加额外参数
        if (request.getExtraParams() != null) {
            extras.putAll(request.getExtraParams());
        }
        
        return extras;
    }
    
    /**
     * 发送流式结束标记
     */
    private void sendStreamingEndMarker(MessageRequest request) {
        Map<String, Object> extras = new HashMap<>();
        extras.put(BroadcastConst.Extra.CONVERSATION_ID, request.getConversationId() != null ? request.getConversationId() : "");
        extras.put(BroadcastConst.Extra.TIMESTAMP, System.currentTimeMillis());
        extras.put(BroadcastConst.Extra.MESSAGE_TYPE, "stream_end");
        extras.put("is_streaming", false);
        
        broadcastManager.sendBroadcast(BroadcastConst.Action.SIMULATE_MESSAGE, extras);
    }
    
    /**
     * 获取分块大小
     */
    private int getChunkSize(MessageRequest request) {
        if (request.getExtraParams() != null && request.getExtraParams().containsKey("chunk_size")) {
            Object chunkSizeObj = request.getExtraParams().get("chunk_size");
            if (chunkSizeObj instanceof Integer) {
                return (Integer) chunkSizeObj;
            }
        }
        return DEFAULT_CHUNK_SIZE;
    }
    
    @Override
    public String getSenderType() {
        return "流式消息发送器";
    }
}