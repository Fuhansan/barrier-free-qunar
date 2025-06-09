package com.qunar.barrier_free_qunar.java.sdk.util;

import com.qunar.barrier_free_qunar.LogCollector;
import com.qunar.barrier_free_qunar.java.sdk.model.StreamEventData;

import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;

/**
 * 流式事件解析器
 * 使用状态机模式解析SSE格式的流式数据
 * 
 * 事件流程：
 * 1. event: start_of_llm -> 开始接收数据
 * 2. data: {"agent_name": "coordinator/planner"} -> 确定代理类型，决定是否开始广播
 * 3. event: message -> 开始发送广播数据
 * 4. data: {"message_id": "xxx", "delta": {"content": "xxx"}} -> 发送实际内容
 * 5. event: end_of_llm -> 结束接收数据
 */
public class StreamEventParser {

    private static final String TAG = "StreamEventParser";
    private static final LogCollector logCollector = LogCollector.Companion.getInstance();

    /**
     * 解析状态枚举
     */
    public enum ParseState {
        IDLE,           // 空闲状态
        WAITING_AGENT,  // 等待代理信息
        READY_TO_BROADCAST, // 准备广播
        BROADCASTING,   // 正在广播
        COMPLETED       // 完成
    }

    private static ParseState currentState = ParseState.IDLE;
    private static StreamEventData.AgentType currentAgentType = StreamEventData.AgentType.OTHER;

    /**
     * 解析SSE格式的数据块
     *
     * @param chunk 原始数据块
     * @return 解析后的事件数据，如果解析失败或不是有效事件则返回null
     */
    public static StreamEventData parseChunk(String chunk) {
        if (chunk == null || chunk.trim().isEmpty()) {
            logCollector.d(TAG, "数据块为空，跳过解析");
            return null;
        }

        try {
            logCollector.d(TAG, "开始解析数据块: " + chunk + ", 当前状态: " + currentState);

            String trimmedChunk = chunk.trim();
            
            // 解析事件类型
            if (trimmedChunk.startsWith("event:")) {
                return parseEventLine(trimmedChunk);
            }
            
            // 解析数据内容
            if (trimmedChunk.startsWith("data:")) {
                return parseDataLine(trimmedChunk);
            }
            
            // 处理纯JSON数据（兼容旧格式）
            return parseDirectJson(trimmedChunk);
        } catch (Exception e) {
            logCollector.w(TAG, "解析流式数据时出错: " + e.getMessage());
            return null;
        }
    }

    /**
     * 解析事件行
     */
    private static StreamEventData parseEventLine(String eventLine) {
        String eventType = eventLine.substring(6).trim(); // 移除"event:"前缀
        logCollector.d(TAG, "解析事件类型: " + eventType);
        
        StreamEventData eventData = new StreamEventData();
        
        switch (eventType) {
            case "start_of_llm":
                currentState = ParseState.WAITING_AGENT;
                eventData.setEventType(StreamEventData.EventType.START_OF_LLM);
                logCollector.i(TAG, "开始LLM流程，状态切换到WAITING_AGENT");
                break;
                
            case "message":
                if (currentState == ParseState.READY_TO_BROADCAST) {
                    currentState = ParseState.BROADCASTING;
                    eventData.setEventType(StreamEventData.EventType.MESSAGE);
                    eventData.setShouldStartBroadcast(true);
                    logCollector.i(TAG, "开始消息广播，状态切换到BROADCASTING");
                } else {
                    eventData.setEventType(StreamEventData.EventType.MESSAGE);
                    logCollector.d(TAG, "接收到message事件，但状态不允许广播: " + currentState);
                }
                break;
                
            case "end_of_llm":
                currentState = ParseState.COMPLETED;
                eventData.setEventType(StreamEventData.EventType.END_OF_LLM);
                logCollector.i(TAG, "LLM流程结束，状态切换到COMPLETED");
                // 重置状态为下次使用做准备
                resetState();
                break;

                
            default:
                eventData.setEventType(StreamEventData.EventType.UNKNOWN);
                logCollector.w(TAG, "未知事件类型: " + eventType);
                break;
        }
        
        return eventData;
    }

    /**
     * 解析数据行
     */
    private static StreamEventData parseDataLine(String dataLine) {
        String jsonString = dataLine.substring(5).trim(); // 移除"data:"前缀
        logCollector.d(TAG, "解析数据内容: " + jsonString);
        
        try {
            JSONObject jsonData = new JSONObject(jsonString);
            StreamEventData eventData = new StreamEventData();
            eventData.setRawData(jsonData);
            
            // 根据当前状态和JSON内容确定事件类型
            if (currentState == ParseState.WAITING_AGENT && jsonData.has("agent_name")) {
                // 解析代理名称
                String agentName = jsonData.getString("agent_name");
                eventData.setEventType(StreamEventData.EventType.AGENT_NAME);
                eventData.setAgentName(agentName);
                
                // 确定代理类型
                currentAgentType = parseAgentType(agentName);
                eventData.setAgentType(currentAgentType);
                
                // 如果是需要广播的代理类型，切换状态
                if (currentAgentType == StreamEventData.AgentType.INSTRUCT_EXTRACTOR ||
                        currentAgentType == StreamEventData.AgentType.OPERATOR ||
                    currentAgentType == StreamEventData.AgentType.PLANNER) {
                    currentState = ParseState.READY_TO_BROADCAST;
                    eventData.setShouldStartBroadcast(true);

                    if(currentAgentType == StreamEventData.AgentType.INSTRUCT_EXTRACTOR){
                        eventData.setCurrConvAgentType(StreamEventData.AgentType.INSTRUCT_EXTRACTOR);
                    }


                    logCollector.i(TAG, "检测到广播代理: " + agentName + "，状态切换到READY_TO_BROADCAST");
                } else {
                    logCollector.d(TAG, "检测到非广播代理: " + agentName);
                }
                
            } else if (currentState == ParseState.BROADCASTING && jsonData.has("delta")) {
                // 解析增量内容
                eventData.setEventType(StreamEventData.EventType.DELTA);
                
                if (jsonData.has("message_id")) {
                    eventData.setMessageId(jsonData.getString("message_id"));
                }
                
                JSONObject delta = jsonData.getJSONObject("delta");
                if (delta.has("content")) {
                    String content = delta.getString("content");
                    eventData.setContent(content);
                    logCollector.d(TAG, "解析到增量内容: " + content);
                }
                
            } else {
                // 其他情况，尝试通用解析
                eventData = parseGenericData(jsonData);
            }
            
            return eventData;
            
        } catch (JSONException e) {
            logCollector.w(TAG, "解析JSON数据失败: " + e.getMessage() + ", 数据: " + jsonString);
            return null;
        }
    }

    /**
     * 解析代理类型
     */
    private static StreamEventData.AgentType parseAgentType(String agentName) {
        if (agentName == null) {
            return StreamEventData.AgentType.OTHER;
        }
        
        String lowerName = agentName.toLowerCase();
        if (lowerName.contains("coordinator")) {
            return StreamEventData.AgentType.COORDINATOR;
        } else if (lowerName.contains("planner")) {
            return StreamEventData.AgentType.PLANNER;
        } else if (lowerName.contains("instruct_extractor")) {
            return StreamEventData.AgentType.INSTRUCT_EXTRACTOR;
        } else if (lowerName.contains("operator")) {
            return StreamEventData.AgentType.OPERATOR;
        } else {
            return StreamEventData.AgentType.OTHER;
        }
    }

    /**
     * 通用数据解析（兼容旧格式）
     */
    private static StreamEventData parseGenericData(JSONObject jsonData) {
        StreamEventData eventData = new StreamEventData();
        eventData.setRawData(jsonData);
        
        // 根据JSON内容推断事件类型
        if (jsonData.has("delta") && jsonData.optJSONObject("delta") != null) {
            JSONObject delta = jsonData.optJSONObject("delta");
            if (delta.has("content")) {
                eventData.setEventType(StreamEventData.EventType.DELTA);
                eventData.setContent(delta.optString("content", ""));
                if (jsonData.has("message_id")) {
                    eventData.setMessageId(jsonData.optString("message_id"));
                }
                logCollector.d(TAG, "通用解析识别为DELTA事件");
            }
        } else if (jsonData.has("agent_name")) {
            eventData.setEventType(StreamEventData.EventType.AGENT_NAME);
            String agentName = jsonData.optString("agent_name");
            eventData.setAgentName(agentName);
            eventData.setAgentType(parseAgentType(agentName));
            logCollector.d(TAG, "通用解析识别为AGENT_NAME事件");
        } else {
            eventData.setEventType(StreamEventData.EventType.UNKNOWN);
            logCollector.d(TAG, "通用解析无法识别事件类型");
        }
        
        return eventData;
    }

    /**
     * 直接解析JSON数据（兼容旧格式）
     */
    private static StreamEventData parseDirectJson(String jsonString) {
        try {
            logCollector.d(TAG, "尝试直接解析JSON: " + jsonString);
            
            // 尝试修复常见的JSON格式错误
            String cleanJson = fixCommonJsonErrors(jsonString);
            JSONObject jsonData = new JSONObject(cleanJson);
            
            return parseGenericData(jsonData);
            
        } catch (Exception e) {
            logCollector.w(TAG, "直接解析JSON失败: " + e.getMessage() + ", 数据: " + jsonString);
            return null;
        }
    }

    /**
     * 修复常见的JSON格式错误
     */
    private static String fixCommonJsonErrors(String jsonString) {
        String fixed = jsonString.trim();
        
        try {
            // 修复缺少引号的键名
            fixed = fixed.replaceAll("([a-zA-Z_][a-zA-Z0-9_]*)\\s*:", "\"$1\":");
            
            // 修复缺少引号的字符串值
            fixed = fixed.replaceAll(":\\s*([a-zA-Z_][a-zA-Z0-9_]*)(?=\\s*[,}])", ": \"$1\"");
            
            logCollector.d(TAG, "JSON修复结果: " + fixed);
            
        } catch (Exception e) {
            logCollector.w(TAG, "JSON修复过程中出错: " + e.getMessage());
            return jsonString;
        }
        
        return fixed;
    }

    /**
     * 重置解析器状态
     */
    public static void resetState() {
        currentState = ParseState.IDLE;
        currentAgentType = StreamEventData.AgentType.OTHER;
        logCollector.d(TAG, "解析器状态已重置");
    }

    /**
     * 获取当前解析状态
     */
    public static ParseState getCurrentState() {
        return currentState;
    }

    /**
     * 获取当前代理类型
     */
    public static StreamEventData.AgentType getCurrentAgentType() {
        return currentAgentType;
    }
}