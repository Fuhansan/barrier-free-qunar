package com.qunar.barrier_free_qunar.java.sdk.model.coordinate;

import java.util.Map;

/**
 * 动作指令模型
 * 封装解析出的动作指令信息
 */
public class ActionCommand {
    
    private ActionType actionType;
    private Map<String, String> parameters;
    private String originalText;
    
    public ActionCommand(ActionType actionType, Map<String, String> parameters, String originalText) {
        this.actionType = actionType;
        this.parameters = parameters;
        this.originalText = originalText;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public String getOriginalText() {
        return originalText;
    }
    
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }
    
    public String getParameter(String key) {
        return parameters != null ? parameters.get(key) : null;
    }
    
    @Override
    public String toString() {
        return "ActionCommand{" +
                "actionType=" + actionType +
                ", parameters=" + parameters +
                ", originalText='" + originalText + '\'' +
                '}';
    }
}