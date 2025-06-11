package com.qunar.barrier_free_qunar.java.sdk.consts;

/**
 * 广播接收器ID常量
 * 用于统一管理所有广播接收器的唯一标识
 */
public class ReceiverConst {
    
    /**
     * 聊天界面广播接收器ID
     */
    public static final String CHAT_ACTIVITY_RECEIVER = "chat_activity_receiver";
    
    /**
     * 服务广播接收器ID
     */
    public static final String SERVICE_RECEIVER = "service_receiver";
    
    /**
     * 适配器广播接收器ID
     */
    public static final String ADAPTER_RECEIVER = "adapter_receiver";
    
    /**
     * 全局无障碍服务接收器ID
     */
    public static final String ACCESSIBILITY_SERVICE_RECEIVER = "accessibility_service_receiver";
    
    /**
     * 自定义接收器ID前缀
     * 用于创建自定义接收器ID，格式：CUSTOM_PREFIX + "具体名称"
     */
    public static final String CUSTOM_PREFIX = "custom_";
    
    // 私有构造函数，防止实例化
    private ReceiverConst() {
        throw new AssertionError("不能实例化常量类");
    }
}