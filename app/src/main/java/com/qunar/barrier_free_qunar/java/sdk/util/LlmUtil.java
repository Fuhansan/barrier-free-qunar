package com.qunar.barrier_free_qunar.java.sdk.util;

import java.util.*;

/**
 * LLM 请求构建工具类
 * 只负责构建 LlmRequest 对象
 */
public class LlmUtil {

    /**
     * 消息内容类型枚举
     */
    public enum ContentType {
        TEXT("text"),
        IMAGE("image");
        
        private final String value;
        
        ContentType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }

    /**
     * 消息内容项
     */
    public static class ContentItem {
        private String type;
        private String text;
        private String imageUrl;
        
        public ContentItem(String type) {
            this.type = type;
        }
        
        public ContentItem setText(String text) {
            this.text = text;
            return this;
        }
        
        public ContentItem setImage(String url) {
            this.imageUrl = url;
            return this;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type);
            if (text != null) {
                map.put("text", text);
            }
            if (imageUrl != null) {
                map.put("image_url", imageUrl);
            }
            return map;
        }
    }

    /**
     * 创建新的 LLM 请求对象
     */
    public static LlmRequest newRequest(String sessionId, String [] teamMembers, Boolean executionLoop) {
        return new LlmRequest(sessionId, teamMembers, executionLoop);
    }

    
    /**
     * LLM 请求对象
     * 负责管理消息历史和生成请求体
     */
    public static class LlmRequest {
        private final List<Map<String, Object>> messages;
        private final String sessionId;
        private final Map<String, Object> parameters;
        
        public LlmRequest(String sessionId,  String[] teamMembers, Boolean executionLoop) {
            this.messages = new ArrayList<>();
            this.sessionId = sessionId;
            this.parameters = new HashMap<>();
            initializeDefaultParameters(teamMembers, executionLoop);
        }
        
        private LlmRequest(List<Map<String, Object>> messages, String sessionId, Map<String, Object> parameters, Boolean executionLoop) {
            this.messages = new ArrayList<>(messages);
            this.sessionId = sessionId;
            this.parameters = new HashMap<>(parameters);
        }
        
        /**
         * 初始化默认参数
         */
        private void initializeDefaultParameters(String[] teamMembers, Boolean executionLoop) {
            parameters.put("debug", false);
            parameters.put("deep_thinking_mode", false);
            parameters.put("search_before_planning", false);
            parameters.put("execution_loop", executionLoop);
//            new String[]{"instruct_extractor"}
            parameters.put("team_members", teamMembers);
        }
        
        /**
         * 添加用户文本消息
         */
        public LlmRequest addUserText(String text) {
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", text);
            messages.add(message);
            return this;
        }
        

        public LlmRequest addUserImage(String imageUrl) {
            List<Map<String, Object>> contentList = new ArrayList<>();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                contentList.add(createImageContent(imageUrl).toMap());
            }
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", contentList);
            messages.add(message);
            return this;
        }
        
        /**
         * 添加助手回复消息
         */
        public LlmRequest addAssistantMessage(String text) {

            Map<String, Object> message = new HashMap<>();
            message.put("role", "assistant");
            message.put("content", text);
            messages.add(message);
            return this;
        }
        
        /**
         * 添加自定义角色消息
         */
        public LlmRequest addMessage(String role, String text) {
            Map<String, Object> message = new HashMap<>();
            message.put("role", role);
            message.put("content", text);
            messages.add(message);
            return this;
        }
        
        /**
         * 添加混合内容消息
         */
        public LlmRequest addMixedMessage(String role, List<ContentItem> contents) {
            List<Map<String, Object>> contentList = new ArrayList<>();
            for (ContentItem item : contents) {
                contentList.add(item.toMap());
            }
            
            Map<String, Object> message = new HashMap<>();
            message.put("role", role);
            message.put("content", contentList);
            messages.add(message);
            return this;
        }
        

        
        /**
         * 获取请求头
         */
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "text/event-stream");
            return headers;
        }
        
        /**
         * 获取请求体
         */
        public Map<String, Object> getRequestBody() {
            Map<String, Object> requestBody = new HashMap<>(parameters);
            requestBody.put("messages", messages.toArray(new Map[0]));
            if (sessionId != null) {
                requestBody.put("sessionId", sessionId);
            }
            requestBody.put("timestamp", System.currentTimeMillis());
            return requestBody;
        }
        
        /**
         * 获取消息历史
         */
        public List<Map<String, Object>> getMessages() {
            return new ArrayList<>(messages);
        }
        
        /**
         * 获取会话ID
         */
        public String getSessionId() {
            return sessionId;
        }
        
        /**
         * 获取消息数量
         */
        public int getMessageCount() {
            return messages.size();
        }

        /**
         * 创建文本内容项
         */
        public  ContentItem createTextContent(String text) {
            return new ContentItem(ContentType.TEXT.getValue()).setText(text);
        }

        /**
         * 创建图片内容项
         */
        public  ContentItem createImageContent(String imageUrl) {
            return new ContentItem(ContentType.IMAGE.getValue()).setImage(imageUrl);
        }
    }

    

}
