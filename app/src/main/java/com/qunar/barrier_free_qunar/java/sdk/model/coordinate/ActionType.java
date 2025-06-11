package com.qunar.barrier_free_qunar.java.sdk.model.coordinate;

/**
 * 动作类型枚举
 * 定义系统支持的所有动作类型
 */
public enum ActionType {
    
    /**
     * 点击动作
     * 参数: start_box (坐标范围)
     */
    CLICK("click"),
    
    /**
     * 滑动手势
     * 参数: 起始和结束坐标
     */
    scroll("scroll"),
    
    /**
     * 返回操作
     */
    BACK("back"),
    
    /**
     * Home键操作
     */
    HOME("home"),
    
    /**
     * 完成操作
     */
    COMPLETE("complete"),
    
    /**
     * 截屏操作
     */
    SCREENSHOT("screenShot"),
    
    /**
     * 打开应用
     * 参数: packageName (包名)
     */
    OPEN_APP("open_app"),

    /**
     * 输入文字
     * 参数type:("text")
     */
    type("type"),


    /**
     * 未知动作类型
     */
    UNKNOWN("unknown");
    
    private final String actionName;
    
    ActionType(String actionName) {
        this.actionName = actionName;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    /**
     * 根据动作名称获取对应的动作类型
     * 
     * @param actionName 动作名称
     * @return 对应的动作类型，如果未找到则返回UNKNOWN
     */
    public static ActionType fromActionName(String actionName) {
        if (actionName == null || actionName.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        for (ActionType type : ActionType.values()) {
            if (type.actionName.equalsIgnoreCase(actionName.trim())) {
                return type;
            }
        }
        
        return UNKNOWN;
    }
}