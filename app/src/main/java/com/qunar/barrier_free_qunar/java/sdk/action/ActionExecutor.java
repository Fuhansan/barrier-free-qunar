package com.qunar.barrier_free_qunar.java.sdk.action;

import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionCommand;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionResult;

/**
 * 动作执行器接口
 * 负责执行具体的动作指令
 */
public interface ActionExecutor {
    
    /**
     * 执行动作指令
     * 
     * @param command 待执行的动作指令
     * @return 执行结果
     */
    ActionResult execute(ActionCommand command);
    
    /**
     * 检查是否支持执行指定的动作指令
     * 
     * @param command 动作指令
     * @return 是否支持执行
     */
    boolean canExecute(ActionCommand command);
}