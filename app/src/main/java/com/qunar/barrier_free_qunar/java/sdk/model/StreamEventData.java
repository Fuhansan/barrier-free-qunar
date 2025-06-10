package com.qunar.barrier_free_qunar.java.sdk.model;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * 流式事件数据模型
 * 用于封装从SSE流中解析出的各种数据
 */
public class StreamEventData {
    
    /**
     * 事件类型枚举
     */
    public enum EventType {
        START_OF_LLM("start_of_llm"),    // 开始接收数据
        AGENT_NAME("agent_name"),      // 代理名称
        MESSAGE("message"),         // 消息事件
        DELTA("delta"),           // 内容增量
        END_OF_LLM("end_of_llm"),
        UNKNOWN("");          // 未知事件类型

        public String code;

        EventType(String code) {
            this.code = code;
        }

        public static EventType fromCode(String code){

            for (EventType value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }

            return UNKNOWN;
        }


    }
    
    /**
     * 代理类型枚举
     */
    public enum AgentType {
        COORDINATOR,        // 协调器
        PLANNER,           // 规划器
        INSTRUCT_EXTRACTOR, // 指令提取器（需要广播）
        OPERATOR,          // 操作器（需要广播并且执行操作）
        OTHER              // 其他类型
    }
    
    private EventType eventType;
    private AgentType agentType;
    private String messageId;
    private String content;
    private String agentName;
    private String agentId;
    private JSONObject rawData;
    private String userName;
    private Double cost;
    private Long timestamp;
    private boolean shouldStartBroadcast; // 是否应该开始广播


    private AgentType currConvAgentType;
    
    public StreamEventData() {
        this.eventType = null;
    }
    
    // Getters and Setters
    public EventType getEventType() {
        return eventType;
    }
    
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
    
    public AgentType getAgentType() {
        return agentType;
    }
    
    public void setAgentType(AgentType agentType) {
        this.agentType = agentType;
    }
    
    public boolean isShouldStartBroadcast() {
        return shouldStartBroadcast;
    }
    
    public void setShouldStartBroadcast(boolean shouldStartBroadcast) {
        this.shouldStartBroadcast = shouldStartBroadcast;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
    
    public JSONObject getRawData() {
        return rawData;
    }
    
    public void setRawData(JSONObject rawData) {
        this.rawData = rawData;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public Double getCost() {
        return cost;
    }
    
    public void setCost(Double cost) {
        this.cost = cost;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 判断是否为开始LLM事件
     */
    public boolean isStartOfLlmEvent() {
        return eventType == EventType.START_OF_LLM;
    }
    
    /**
     * 判断是否为代理名称事件
     */
    public boolean isAgentNameEvent() {
        return eventType == EventType.AGENT_NAME;
    }
    
    /**
     * 判断是否为消息事件
     */
    public boolean isMessageEvent() {
        return eventType == EventType.MESSAGE;
    }
    
    /**
     * 判断是否为内容增量事件
     */
    public boolean isDeltaEvent() {
        return eventType == EventType.DELTA;
    }
    
    /**
     * 判断是否为结束LLM事件
     */
    public boolean isEndOfLlmEvent() {
        return eventType == EventType.END_OF_LLM;
    }




    public AgentType getCurrConvAgentType() {
        return currConvAgentType;
    }

    public void setCurrConvAgentType(AgentType currConvAgentType) {
        this.currConvAgentType = currConvAgentType;
    }

    /**
     * 判断是否为需要开始广播的代理类型
     */
    public boolean isBroadcastAgent() {
        return agentType == AgentType.COORDINATOR || 
               agentType == AgentType.PLANNER || 
               agentType == AgentType.INSTRUCT_EXTRACTOR;
    }
    
    @Override
    public String toString() {
        return "StreamEventData{" +
                "eventType=" + eventType +
                ", messageId='" + messageId + '\'' +
                ", content='" + content + '\'' +
                ", agentName='" + agentName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", userName='" + userName + '\'' +
                ", cost=" + cost +
                ", timestamp=" + timestamp +
                '}';
    }
}