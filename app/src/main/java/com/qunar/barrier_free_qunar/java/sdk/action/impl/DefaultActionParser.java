package com.qunar.barrier_free_qunar.java.sdk.action.impl;

import android.util.Log;

import com.qunar.barrier_free_qunar.java.sdk.action.ActionParser;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionCommand;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认动作解析器实现
 * 使用正则表达式解析文本中的动作指令
 */
public class DefaultActionParser implements ActionParser {
    
    private static final String TAG = "DefaultActionParser";
    
    // 匹配动作指令的正则表达式
    // 例如: click(start_box='[700,2300,886,2450]'), performSwipeGesture(from='[100,200]', to='[300,400]'), back(), complete
    private static final Pattern ACTION_PATTERN = Pattern.compile(
        "(\\w+)(?:\\(([^)]*)\\))?",
        Pattern.CASE_INSENSITIVE
    );
    
    // 匹配参数的正则表达式
    // 例如: start_box='[700,2300,886,2450]', packageName='com.example.app'
    private static final Pattern PARAM_PATTERN = Pattern.compile(
        "(\\w+)\\s*=\\s*['\"]([^'\"]*)['\"]?",
        Pattern.CASE_INSENSITIVE
    );
    
    @Override
    public List<ActionCommand> parseActions(String content) {
        List<ActionCommand> commands = new ArrayList<>();
        
        if (content == null || content.trim().isEmpty()) {
            return commands;
        }
        
        Log.d(TAG, "开始解析动作指令: " + content);
        
        Matcher actionMatcher = ACTION_PATTERN.matcher(content);
        
        while (actionMatcher.find()) {
            String actionName = actionMatcher.group(1);
            String paramString = actionMatcher.group(2);
            String originalText = actionMatcher.group(0);
            
            Log.d(TAG, "发现动作: " + actionName + ", 参数: " + paramString);
            
            ActionType actionType = ActionType.fromActionName(actionName);
            
            // 只处理已知的动作类型
            if (actionType != ActionType.UNKNOWN) {
                Map<String, String> parameters = parseParameters(paramString);
                ActionCommand command = new ActionCommand(actionType, parameters, originalText);
                commands.add(command);
                
                Log.d(TAG, "成功解析动作指令: " + command);
            } else {
                Log.w(TAG, "未知的动作类型: " + actionName);
            }
        }
        
        Log.d(TAG, "解析完成，共找到 " + commands.size() + " 个动作指令");
        return commands;
    }
    
    @Override
    public boolean containsActions(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        Matcher actionMatcher = ACTION_PATTERN.matcher(content);
        
        while (actionMatcher.find()) {
            String actionName = actionMatcher.group(1);
            ActionType actionType = ActionType.fromActionName(actionName);
            
            if (actionType != ActionType.UNKNOWN) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 解析参数字符串
     * 
     * @param paramString 参数字符串
     * @return 参数映射
     */
    private Map<String, String> parseParameters(String paramString) {
        Map<String, String> parameters = new HashMap<>();
        
        if (paramString == null || paramString.trim().isEmpty()) {
            return parameters;
        }
        
        Matcher paramMatcher = PARAM_PATTERN.matcher(paramString);
        
        while (paramMatcher.find()) {
            String key = paramMatcher.group(1);
            String value = paramMatcher.group(2);
            
            if (key != null && value != null) {
                parameters.put(key.trim(), value.trim());
                Log.d(TAG, "解析参数: " + key + " = " + value);
            }
        }
        
        return parameters;
    }
}