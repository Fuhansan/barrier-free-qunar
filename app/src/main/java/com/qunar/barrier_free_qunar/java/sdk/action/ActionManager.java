package com.qunar.barrier_free_qunar.java.sdk.action;

import android.util.Log;

import com.qunar.barrier_free_qunar.java.sdk.action.impl.DefaultActionParser;
import com.qunar.barrier_free_qunar.java.sdk.action.impl.GestureActionExecutor;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionCommand;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionResult;
import com.qunar.barrier_free_qunar.java.sdk.gesture.GestureApi;

import java.util.ArrayList;
import java.util.List;

/**
 * 动作管理器
 * 统一管理动作的解析和执行
 */
public class ActionManager {
    
    private static final String TAG = "ActionManager";
    
    private final ActionParser actionParser;
    private final List<ActionExecutor> executors;
    
    public ActionManager(GestureApi gestureApi) {
        this.actionParser = new DefaultActionParser();
        this.executors = new ArrayList<>();
        
        // 注册默认的执行器
        if (gestureApi != null) {
            this.executors.add(new GestureActionExecutor(gestureApi));
        }
    }
    
    /**
     * 添加自定义执行器
     * 
     * @param executor 执行器
     */
    public void addExecutor(ActionExecutor executor) {
        if (executor != null && !executors.contains(executor)) {
            executors.add(executor);
            Log.d(TAG, "添加执行器: " + executor.getClass().getSimpleName());
        }
    }
    
    /**
     * 移除执行器
     * 
     * @param executor 执行器
     */
    public void removeExecutor(ActionExecutor executor) {
        if (executor != null) {
            executors.remove(executor);
            Log.d(TAG, "移除执行器: " + executor.getClass().getSimpleName());
        }
    }
    
    /**
     * 检查内容是否包含动作指令
     * 
     * @param content 待检查的内容
     * @return 是否包含动作指令
     */
    public boolean containsActions(String content) {
        return actionParser.containsActions(content);
    }
    
    /**
     * 解析并执行动作指令
     * 
     * @param content 包含动作指令的文本内容
     * @return 执行结果列表
     */
    public List<ActionResult> parseAndExecuteActions(String content) {
        List<ActionResult> results = new ArrayList<>();
        
        if (content == null || content.trim().isEmpty()) {
            Log.w(TAG, "内容为空，无法解析动作指令");
            return results;
        }
        
        Log.d(TAG, "开始解析并执行动作指令: " + content);
        
        // 解析动作指令
        List<ActionCommand> commands = actionParser.parseActions(content);
        
        if (commands.isEmpty()) {
            Log.d(TAG, "未找到动作指令");
            return results;
        }
        
        Log.d(TAG, "解析到 " + commands.size() + " 个动作指令");
        
        // 执行动作指令
        for (ActionCommand command : commands) {
            ActionResult result = executeAction(command);
            results.add(result);
            
            Log.d(TAG, "动作执行结果: " + result);
            
            // 如果执行失败，可以选择是否继续执行后续动作
            if (!result.isSuccess()) {
                Log.w(TAG, "动作执行失败: " + result.getMessage());
                // 这里可以根据需要决定是否继续执行
            }
        }
        
        Log.d(TAG, "所有动作执行完成，共执行 " + results.size() + " 个动作");
        return results;
    }
    
    /**
     * 执行单个动作指令
     * 
     * @param command 动作指令
     * @return 执行结果
     */
    public ActionResult executeAction(ActionCommand command) {
        if (command == null) {
            return ActionResult.failure("动作指令为空");
        }
        
        Log.d(TAG, "执行动作指令: " + command);
        
        // 查找能够执行该动作的执行器
        for (ActionExecutor executor : executors) {
            if (executor.canExecute(command)) {
                Log.d(TAG, "使用执行器: " + executor.getClass().getSimpleName());
                return executor.execute(command);
            }
        }
        
        String errorMsg = "未找到能够执行该动作的执行器: " + command.getActionType();
        Log.w(TAG, errorMsg);
        return ActionResult.failure(errorMsg);
    }
    
    /**
     * 仅解析动作指令，不执行
     * 
     * @param content 包含动作指令的文本内容
     * @return 解析出的动作指令列表
     */
    public List<ActionCommand> parseActions(String content) {
        return actionParser.parseActions(content);
    }
    
    /**
     * 获取当前注册的执行器数量
     * 
     * @return 执行器数量
     */
    public int getExecutorCount() {
        return executors.size();
    }
    
    /**
     * 清空所有执行器
     */
    public void clearExecutors() {
        executors.clear();
        Log.d(TAG, "清空所有执行器");
    }
}