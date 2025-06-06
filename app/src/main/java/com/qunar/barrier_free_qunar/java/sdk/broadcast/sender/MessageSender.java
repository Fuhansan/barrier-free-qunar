package com.qunar.barrier_free_qunar.java.sdk.broadcast.sender;

import com.qunar.barrier_free_qunar.java.sdk.broadcast.model.MessageRequest;

/**
 * 消息发送器抽象基类
 * 使用多态设计，根据发送模式选择具体的发送策略
 */
public abstract class MessageSender {
    
    /**
     * 发送消息的抽象方法
     * 子类需要实现具体的发送逻辑
     * 
     * @param request 消息发送请求
     * @return 是否发送成功
     */
    public abstract boolean send(MessageRequest request);
    
    /**
     * 获取发送器类型描述
     * 
     * @return 发送器类型描述
     */
    public abstract String getSenderType();
    
    /**
     * 验证消息请求是否有效
     * 
     * @param request 消息请求
     * @return 是否有效
     */
    protected boolean validateRequest(MessageRequest request) {
        if (request == null) {
            return false;
        }
        
        if (request.getSendMode() == null) {
            return false;
        }
        
        if (request.getMessageType() == null) {
            return false;
        }
        
        if (request.getSenderRole() == null) {
            return false;
        }
        
        // 文字消息必须有内容
        if (request.getMessageType() == MessageRequest.MessageType.TEXT && 
            (request.getContent() == null || request.getContent().trim().isEmpty())) {
            return false;
        }
        
        // 多媒体消息必须有多媒体数据
        if (request.getMessageType() == MessageRequest.MessageType.MULTIMEDIA && 
            request.getMultimediaData() == null) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取消息发送器工厂方法
     * 根据发送模式返回对应的发送器实例
     * 
     * @param sendMode 发送模式
     * @return 对应的消息发送器
     */
    public static MessageSender createSender(MessageRequest.SendMode sendMode) {
        switch (sendMode) {
            case STREAMING:
                return new StreamingMessageSender();
            case SHORT_CONNECTION:
                return new ShortConnectionMessageSender();
            default:
                throw new IllegalArgumentException("不支持的发送模式: " + sendMode);
        }
    }
}