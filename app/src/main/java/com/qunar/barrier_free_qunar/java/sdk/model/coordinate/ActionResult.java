package com.qunar.barrier_free_qunar.java.sdk.model.coordinate;

/**
 * 动作执行结果模型
 * 封装动作执行的结果信息
 */
public class ActionResult {
    
    private boolean success;
    private String message;
    private String errorCode;
    private Object data;
    private ActionCommand originalCommand;
    
    public ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public ActionResult(boolean success, String message, ActionCommand originalCommand) {
        this.success = success;
        this.message = message;
        this.originalCommand = originalCommand;
    }
    
    public static ActionResult success(String message) {
        return new ActionResult(true, message);
    }
    
    public static ActionResult success(String message, Object data) {
        ActionResult result = new ActionResult(true, message);
        result.setData(data);
        return result;
    }
    
    public static ActionResult failure(String message) {
        return new ActionResult(false, message);
    }
    
    public static ActionResult failure(String message, String errorCode) {
        ActionResult result = new ActionResult(false, message);
        result.setErrorCode(errorCode);
        return result;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public ActionCommand getOriginalCommand() {
        return originalCommand;
    }
    
    public void setOriginalCommand(ActionCommand originalCommand) {
        this.originalCommand = originalCommand;
    }
    
    @Override
    public String toString() {
        return "ActionResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", data=" + data +
                ", originalCommand=" + originalCommand +
                '}';
    }
}