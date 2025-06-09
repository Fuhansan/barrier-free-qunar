package com.qunar.barrier_free_qunar.java.sdk.consts;

/**
 * 广播常量类
 * 统一管理应用中使用的广播Action和Extra键名
 */
public class BroadcastConst {
    
    /**
     * 广播Action常量
     */
    public static class Action {
        /** 模拟消息广播 - 用于从无障碍服务向ChatActivity发送消息 */
        public static final String SIMULATE_MESSAGE = "com.qunar.barrier_free_qunar.SIMULATE_MESSAGE";
        
        /** 用户输入广播 - 用于通知用户输入了新消息 */
        public static final String USER_INPUT = "com.qunar.barrier_free_qunar.USER_INPUT";
        
        /** AI响应广播 - 用于通知AI响应完成 */
        public static final String AI_RESPONSE = "com.qunar.barrier_free_qunar.AI_RESPONSE";
    }
    
    /**
     * 广播Extra键名常量
     */
    public static class Extra {
        /** 原始用户消息 */
        public static final String ORIGINAL_MESSAGE = "original_message";
        
        /** AI回复内容 */
        public static final String REPLY = "reply";
        
        /** 发送者名称 */
        public static final String SENDER_NAME = "sender_name";
        
        /** 发送者ID */
        public static final String SENDER_ID = "sender_id";
        
        /** 时间戳 */
        public static final String TIMESTAMP = "timestamp";
        
        /** 会话ID */
        public static final String SESSION_ID = "session_id";
        
        /** 消息类型 */
        public static final String MESSAGE_TYPE = "message_type";
        
        /** 用户输入 */
        public static final String USER_INPUT = "user_input";
        
        /** 响应时间 */
        public static final String RESPONSE_TIME = "response_time";
        
        /** 错误代码 */
        public static final String ERROR_CODE = "error_code";
        
        /** 错误信息 */
        public static final String ERROR_MESSAGE = "error_message";

        /** 会话ID */
        public static final String CONVERSATION_ID = "conversation_id";
        
        /** 图片数据 */
        public static final String IMAGE_DATA = "image_data";
        
        /** 视频数据 */
        public static final String VIDEO_DATA = "video_data";
        
        /** 语音数据 */
        public static final String AUDIO_DATA = "audio_data";
        
        /** 多媒体类型 */
        public static final String MULTIMEDIA_TYPE = "multimedia_type";
        
        /** 多媒体数据 */
        public static final String MULTIMEDIA_DATA = "multimedia_data";
    }
    
    /**
     * 消息类型常量
     */
    public static class MessageType {
        /** 普通文本消息 */
        public static final String TEXT = "text";
        
        /** 流式消息块 */
        public static final String STREAM_CHUNK = "stream_chunk";
        
        /** 错误消息 */
        public static final String ERROR = "error";
        
        /** 系统消息 */
        public static final String SYSTEM = "system";
        
        /** 用户输入消息 */
        public static final String USER_INPUT = "user_input";
        
        /** AI响应完成消息 */
        public static final String AI_RESPONSE_COMPLETE = "ai_response_complete";
        
        /** 图片消息 */
        public static final String IMAGE = "image";
        
        /** 视频消息 */
        public static final String VIDEO = "video";
        
        /** 语音消息 */
        public static final String AUDIO = "audio";
        
        /** 多媒体混合消息 */
        public static final String MULTIMEDIA = "multimedia";
    }
    
    /**
     * 发送者ID常量
     */
    public static class SenderId {
        /** AI助手 */
        public static final String AI_ASSISTANT = "ai_assistant";
        
        /** 系统 */
        public static final String SYSTEM = "system";
        
        /** 用户 */
        public static final String USER = "user";
        
        /** 未知 */
        public static final String UNKNOWN = "unknown";
    }
    
    /**
     * 发送者名称常量
     */
    public static class SenderName {
        /** AI助手 */
        public static final String AI_ASSISTANT = "AI助手";
        
        /** 系统 */
        public static final String SYSTEM = "系统";
        
        /** 小助手 */
        public static final String ASSISTANT = "小助手";
        
        /** 未知 */
        public static final String UNKNOWN = "未知";


        /** 用户 */
        public static final String USER = "user";
    }
}