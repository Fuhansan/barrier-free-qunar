package com.qunar.barrier_free_qunar.java.sdk.model.broadcast;

import java.util.Map;

/**
 * 消息发送请求封装类
 * 用于封装所有发送消息所需的参数
 */
public class MessageRequest {

    /** 发送广播key*/
    private BroadCastListenerKey broadcastKey;

    /** 发送模式 */
    private SendMode sendMode;
    
    /** 消息类型 */
    private MessageType messageType;
    
    /** 发送角色 */
    private SenderRole senderRole;
    
    /** 消息内容 */
    private String content;
    
    /** 原始消息（用于回复场景） */
    private String originalMessage;
    
    /** 消息ID */
    private String conversationId;
    
    /** 多媒体数据（当消息类型为多媒体时使用） */
    private MultimediaData multimediaData;
    
    /** 额外参数 */
    private Map<String, Object> extraParams;

    /** 会话ID，会话是多轮的包含多个消息 */
    private String sessionId;

    /** 目标接收器ID，用于指定特定的广播接收器 */
    private String targetReceiverId;

    
    // 构造函数
    public MessageRequest() {}
    
    public MessageRequest(SendMode sendMode, MessageType messageType, SenderRole senderRole, String content) {
        this.sendMode = sendMode;
        this.messageType = messageType;
        this.senderRole = senderRole;
        this.content = content;
    }
    
    // Getter和Setter方法
    public SendMode getSendMode() {
        return sendMode;
    }
    
    public void setSendMode(SendMode sendMode) {
        this.sendMode = sendMode;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public SenderRole getSenderRole() {
        return senderRole;
    }
    
    public void setSenderRole(SenderRole senderRole) {
        this.senderRole = senderRole;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getOriginalMessage() {
        return originalMessage;
    }
    
    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTargetReceiverId() {
        return targetReceiverId;
    }

    public void setTargetReceiverId(String targetReceiverId) {
        this.targetReceiverId = targetReceiverId;
    }

    public MultimediaData getMultimediaData() {
        return multimediaData;
    }
    
    public void setMultimediaData(MultimediaData multimediaData) {
        this.multimediaData = multimediaData;
    }
    
    public Map<String, Object> getExtraParams() {
        return extraParams;
    }
    
    public void setExtraParams(Map<String, Object> extraParams) {
        this.extraParams = extraParams;
    }

    public BroadCastListenerKey getBroadcastKey() {
        return broadcastKey;
    }

    public void setBroadcastKey(BroadCastListenerKey broadcastKey) {
        this.broadcastKey = broadcastKey;
    }

    /**
     * 发送模式枚举
     */
    public enum SendMode {
        /** 流式发送 - 数据分块传输，前端实时显示 */
        STREAMING,
        /** 短链接发送 - 一次性传输完整数据 */
        SHORT_CONNECTION
    }
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /** 纯文字消息 */
        TEXT,
        /** 多媒体消息（包含文字、语音、视频、图片） */
        MULTIMEDIA
    }
    
    /**
     * 发送角色枚举
     */
    public enum SenderRole {
        /** 用户 */
        USER,
        /** 系统/AI助手 */
        SYSTEM
    }



    public enum BroadCastListenerKey {
       CHAT_MESSAGE_BROAD_CAST,
        CHAT_UI_BROAD_CAST
    }
    
    /**
     * 多媒体数据封装类
     */
    public static class MultimediaData {
        /** 文字内容 */
        private String text;
        /** 语音数据路径或Base64 */
        private String audioData;
        /** 视频数据路径或Base64 */
        private String videoData;
        /** 图片数据路径或Base64 */
        private String imageData;
        /** 多媒体类型标识 */
        private MultimediaType type;
        
        public enum MultimediaType {
            TEXT_ONLY,
            AUDIO,
            VIDEO,
            IMAGE,
            MIXED
        }
        
        // Getter和Setter方法
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getAudioData() { return audioData; }
        public void setAudioData(String audioData) { this.audioData = audioData; }
        
        public String getVideoData() { return videoData; }
        public void setVideoData(String videoData) { this.videoData = videoData; }
        
        public String getImageData() { return imageData; }
        public void setImageData(String imageData) { this.imageData = imageData; }
        
        public MultimediaType getType() { return type; }
        public void setType(MultimediaType type) { this.type = type; }
    }
    
    /**
     * 建造者模式，方便构建MessageRequest
     */
    public static class Builder {
        private MessageRequest request = new MessageRequest();
        
        public Builder sendMode(SendMode sendMode) {
            request.setSendMode(sendMode);
            return this;
        }
        
        public Builder messageType(MessageType messageType) {
            request.setMessageType(messageType);
            return this;
        }
        
        public Builder senderRole(SenderRole senderRole) {
            request.setSenderRole(senderRole);
            return this;
        }
        
        public Builder content(String content) {
            request.setContent(content);
            return this;
        }
        
        public Builder originalMessage(String originalMessage) {
            request.setOriginalMessage(originalMessage);
            return this;
        }
        
        public Builder conversationId(String conversationId) {
            request.setConversationId(conversationId);
            return this;
        }
        
        public Builder multimediaData(MultimediaData multimediaData) {
            request.setMultimediaData(multimediaData);
            return this;
        }

        public Builder sessionId(String sessionId){
            request.setSessionId(sessionId);
            return this;
        }
        
        public Builder targetReceiverId(String targetReceiverId) {
            request.setTargetReceiverId(targetReceiverId);
            return this;
        }
        
        public Builder extraParams(Map<String, Object> extraParams) {
            request.setExtraParams(extraParams);
            return this;
        }

        public Builder broadCastKey(BroadCastListenerKey broadcastKey){
            request.setBroadcastKey(broadcastKey);
            return this;
        }
        
        public MessageRequest build() {
            return request;
        }
    }
}