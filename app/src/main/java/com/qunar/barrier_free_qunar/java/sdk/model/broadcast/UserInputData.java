package com.qunar.barrier_free_qunar.java.sdk.model.broadcast;

/**
 * 用户输入数据类
 */
public class UserInputData {
    public final String broadcastKey;
    public final String userInput;
    public final String sessionId;
    public final long timestamp;


    public UserInputData(String broadcastKey, String userInput, String sessionId, long timestamp) {
        this.broadcastKey = broadcastKey;
        this.userInput = userInput;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
    }
}