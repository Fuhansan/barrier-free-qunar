package com.qunar.barrier_free_qunar.java.sdk.model.broadcast;

/**
 * AI响应数据类
 */
public class AIResponseData {

    public final String broadcastKey;

    public final String originalMessage;
    public final String reply;
    public final String sessionId;
    public final long timestamp;
    public final long responseTime;

    public AIResponseData(String broadcastKey,String originalMessage, String reply, String sessionId,
                          long timestamp, long responseTime) {
        this.broadcastKey = broadcastKey;
        this.originalMessage = originalMessage;
        this.reply = reply;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.responseTime = responseTime;
    }
}