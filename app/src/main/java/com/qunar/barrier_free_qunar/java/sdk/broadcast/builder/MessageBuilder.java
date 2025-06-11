package com.qunar.barrier_free_qunar.java.sdk.broadcast.builder;

import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.MessageRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息构建器框架
 * 用于构建不同类型的消息数据
 */
public class MessageBuilder {
    
    /**
     * 创建文字消息构建器
     */
    public static TextMessageBuilder text() {
        return new TextMessageBuilder();
    }
    
    /**
     * 创建多媒体消息构建器
     */
    public static MultimediaMessageBuilder multimedia() {
        return new MultimediaMessageBuilder();
    }
    
    /**
     * 文字消息构建器
     */
    public static class TextMessageBuilder {
        private MessageRequest.Builder requestBuilder = new MessageRequest.Builder();
        
        public TextMessageBuilder() {
            requestBuilder.messageType(MessageRequest.MessageType.TEXT);
        }
        
        /**
         * 设置发送模式
         */
        public TextMessageBuilder sendMode(MessageRequest.SendMode sendMode) {
            requestBuilder.sendMode(sendMode);
            return this;
        }
        
        /**
         * 流式发送
         */
        public TextMessageBuilder streaming() {
            requestBuilder.sendMode(MessageRequest.SendMode.STREAMING);
            return this;
        }
        
        /**
         * 短链接发送
         */
        public TextMessageBuilder shortConnection() {
            requestBuilder.sendMode(MessageRequest.SendMode.SHORT_CONNECTION);
            return this;
        }
        
        /**
         * 设置发送角色
         */
        public TextMessageBuilder senderRole(MessageRequest.SenderRole senderRole) {
            requestBuilder.senderRole(senderRole);
            return this;
        }
        
        /**
         * 用户发送
         */
        public TextMessageBuilder fromUser() {
            requestBuilder.senderRole(MessageRequest.SenderRole.USER);
            return this;
        }
        
        /**
         * 系统发送
         */
        public TextMessageBuilder fromSystem() {
            requestBuilder.senderRole(MessageRequest.SenderRole.SYSTEM);
            return this;
        }
        
        /**
         * 设置消息内容
         */
        public TextMessageBuilder content(String content) {
            requestBuilder.content(content);
            return this;
        }
        
        /**
         * 设置原始消息
         */
        public TextMessageBuilder originalMessage(String originalMessage) {
            requestBuilder.originalMessage(originalMessage);
            return this;
        }
        
        /**
         * 设置会话ID
         */
        public TextMessageBuilder conversationId(String conversationId) {
            requestBuilder.conversationId(conversationId);
            return this;
        }
        
        /**
         * 设置流式分块大小
         */
        public TextMessageBuilder chunkSize(int chunkSize) {
            Map<String, Object> extraParams = new HashMap<>();
            extraParams.put("chunk_size", chunkSize);
            requestBuilder.extraParams(extraParams);
            return this;
        }
        
        /**
         * 添加额外参数
         */
        public TextMessageBuilder extraParam(String key, Object value) {
            Map<String, Object> extraParams = new HashMap<>();
            extraParams.put(key, value);
            requestBuilder.extraParams(extraParams);
            return this;
        }
        
        /**
         * 构建消息请求
         */
        public MessageRequest build() {
            return requestBuilder.build();
        }
    }
    
    /**
     * 多媒体消息构建器
     */
    public static class MultimediaMessageBuilder {
        private MessageRequest.Builder requestBuilder = new MessageRequest.Builder();
        private MessageRequest.MultimediaData multimediaData = new MessageRequest.MultimediaData();
        
        public MultimediaMessageBuilder() {
            requestBuilder.messageType(MessageRequest.MessageType.MULTIMEDIA);
        }
        
        /**
         * 设置发送模式
         */
        public MultimediaMessageBuilder sendMode(MessageRequest.SendMode sendMode) {
            requestBuilder.sendMode(sendMode);
            return this;
        }
        
        /**
         * 流式发送
         */
        public MultimediaMessageBuilder streaming() {
            requestBuilder.sendMode(MessageRequest.SendMode.STREAMING);
            return this;
        }
        
        /**
         * 短链接发送
         */
        public MultimediaMessageBuilder shortConnection() {
            requestBuilder.sendMode(MessageRequest.SendMode.SHORT_CONNECTION);
            return this;
        }
        
        /**
         * 设置发送角色
         */
        public MultimediaMessageBuilder senderRole(MessageRequest.SenderRole senderRole) {
            requestBuilder.senderRole(senderRole);
            return this;
        }
        
        /**
         * 用户发送
         */
        public MultimediaMessageBuilder fromUser() {
            requestBuilder.senderRole(MessageRequest.SenderRole.USER);
            return this;
        }
        
        /**
         * 系统发送
         */
        public MultimediaMessageBuilder fromSystem() {
            requestBuilder.senderRole(MessageRequest.SenderRole.SYSTEM);
            return this;
        }
        
        /**
         * 设置文字内容
         */
        public MultimediaMessageBuilder text(String text) {
            multimediaData.setText(text);
            if (multimediaData.getType() == null) {
                multimediaData.setType(MessageRequest.MultimediaData.MultimediaType.TEXT_ONLY);
            }
            return this;
        }
        
        /**
         * 设置语音数据
         */
        public MultimediaMessageBuilder audio(String audioData) {
            multimediaData.setAudioData(audioData);
            multimediaData.setType(MessageRequest.MultimediaData.MultimediaType.AUDIO);
            return this;
        }
        
        /**
         * 设置视频数据
         */
        public MultimediaMessageBuilder video(String videoData) {
            multimediaData.setVideoData(videoData);
            multimediaData.setType(MessageRequest.MultimediaData.MultimediaType.VIDEO);
            return this;
        }
        
        /**
         * 设置图片数据
         */
        public MultimediaMessageBuilder image(String imageData) {
            multimediaData.setImageData(imageData);
            multimediaData.setType(MessageRequest.MultimediaData.MultimediaType.IMAGE);
            return this;
        }
        
        /**
         * 设置混合多媒体类型
         */
        public MultimediaMessageBuilder mixed() {
            multimediaData.setType(MessageRequest.MultimediaData.MultimediaType.MIXED);
            return this;
        }
        
        /**
         * 设置原始消息
         */
        public MultimediaMessageBuilder originalMessage(String originalMessage) {
            requestBuilder.originalMessage(originalMessage);
            return this;
        }
        
        /**
         * 设置会话ID
         */
        public MultimediaMessageBuilder conversationId(String conversationId) {
            requestBuilder.conversationId(conversationId);
            return this;
        }
        
        /**
         * 添加额外参数
         */
        public MultimediaMessageBuilder extraParam(String key, Object value) {
            Map<String, Object> extraParams = new HashMap<>();
            extraParams.put(key, value);
            requestBuilder.extraParams(extraParams);
            return this;
        }
        
        /**
         * 构建消息请求
         */
        public MessageRequest build() {
            requestBuilder.multimediaData(multimediaData);
            return requestBuilder.build();
        }
    }
    
    /**
     * 快速构建常用消息类型的工具方法
     */
    public static class QuickBuilder {
        
        /**
         * 快速构建用户文字消息（短链接）
         */
        public static MessageRequest userText(String content, String conversationId) {
            return text()
                .shortConnection()
                .fromUser()
                .content(content)
                .conversationId(conversationId)
                .build();
        }
        
        /**
         * 快速构建AI流式文字回复
         */
        public static MessageRequest aiStreamingText(String content, String originalMessage, String conversationId) {
            return text()
                .streaming()
                .fromSystem()
                .content(content)
                .originalMessage(originalMessage)
                .conversationId(conversationId)
                .build();
        }
        
        /**
         * 快速构建AI短链接文字回复
         */
        public static MessageRequest aiText(String content, String originalMessage, String conversationId) {
            return text()
                .shortConnection()
                .fromSystem()
                .content(content)
                .originalMessage(originalMessage)
                .conversationId(conversationId)
                .build();
        }
        
        /**
         * 快速构建用户图片消息
         */
        public static MessageRequest userImage(String imageData, String conversationId) {
            return multimedia()
                .shortConnection()
                .fromUser()
                .image(imageData)
                .conversationId(conversationId)
                .build();
        }
        
        /**
         * 快速构建用户语音消息
         */
        public static MessageRequest userAudio(String audioData, String conversationId) {
            return multimedia()
                .shortConnection()
                .fromUser()
                .audio(audioData)
                .conversationId(conversationId)
                .build();
        }
    }
}