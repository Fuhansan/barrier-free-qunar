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
    
    // 专门用于解析OPEN_APP动作的参数模式
    private static final Pattern OPEN_APP_PARAM_PATTERN = Pattern.compile(
        "app_name\\s*=\\s*['\"]([^'\"]*)['\"]?",
        Pattern.CASE_INSENSITIVE
    );
    
    // 专门用于解析CLICK动作的参数模式
    private static final Pattern CLICK_PARAM_PATTERN = Pattern.compile(
        "start_box\\s*=\\s*['\"]([^'\"]*)['\"]?",
        Pattern.CASE_INSENSITIVE
    );
    
    // 专门用于解析PERFORM_SWIPE_GESTURE动作的参数模式
    private static final Pattern SWIPE_FROM_PATTERN = Pattern.compile(
        "from\\s*=\\s*['\"]([^'\"]*)['\"]?",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SWIPE_TO_PATTERN = Pattern.compile(
        "to\\s*=\\s*['\"]([^'\"]*)['\"]?",
        Pattern.CASE_INSENSITIVE
    );
    
    // 专门用于解析INPUT_TEXT动作的参数模式
    private static final Pattern INPUT_TEXT_PATTERN = Pattern.compile(
        "text\\s*=\\s*['\"]([^'\"]*)['\"]?",
        Pattern.CASE_INSENSITIVE
    );
    
    // 专门用于解析SCROLL动作的参数模式
    private static final Pattern SCROLL_START_BOX_PATTERN = Pattern.compile(
        "start_box\\s*=\\s*['\"]([^'\"]*)['\"]?",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SCROLL_END_BOX_PATTERN = Pattern.compile(
        "end_box\\s*=\\s*['\"]([^'\"]*)['\"]?",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SCROLL_DIRECTION_PATTERN = Pattern.compile(
        "direction\\s*=\\s*['\"]([^'\"]*)['\"]?",
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
                Map<String, String> parameters = parseParametersForAction(actionType, paramString);
                ActionCommand command = new ActionCommand(actionType, parameters, originalText);
                commands.add(command);
                Log.i(TAG, "成功解析动作指令: " + command);
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
                Log.d(TAG, "解析动作完成，未知指令： " + actionName);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 根据动作类型解析参数字符串
     * 
     * @param actionType 动作类型
     * @param paramString 参数字符串
     * @return 参数映射
     */
    private Map<String, String> parseParametersForAction(ActionType actionType, String paramString) {
        Map<String, String> parameters = new HashMap<>();
        
        if (paramString == null || paramString.trim().isEmpty()) {
            return parameters;
        }
        
        Log.d(TAG, "为动作类型 " + actionType + " 解析参数: " + paramString);
        
        switch (actionType) {
            case OPEN_APP:
                parseOpenAppParameters(paramString, parameters);
                break;
            case CLICK:
                parseClickParameters(paramString, parameters);
                break;
            case scroll:
                parseScrollParameters(paramString, parameters);
                break;
            case type:
                parseInputTextParameters(paramString, parameters);
                break;
            case BACK:
            case HOME:
            case COMPLETE:
            case SCREENSHOT:
                // 这些动作不需要参数
                break;
            default:
                // 对于未知或新增的动作类型，使用通用解析
                parseGenericParameters(paramString, parameters);
                break;
        }
        
        return parameters;
    }
    
    /**
     * 解析OPEN_APP动作的参数
     */
    private void parseOpenAppParameters(String paramString, Map<String, String> parameters) {
        Matcher matcher = OPEN_APP_PARAM_PATTERN.matcher(paramString);
        if (matcher.find()) {
            String appName = matcher.group(1);
            if (appName != null) {
                parameters.put("app_name", appName.trim());
                // 为了兼容性，同时设置packageName参数
                parameters.put("packageName", appName.trim());
                Log.d(TAG, "解析OPEN_APP参数: app_name = " + appName);
            }
        } else {
            Log.w(TAG, "OPEN_APP动作参数解析失败: " + paramString);
        }
    }
    
    /**
     * 解析CLICK动作的参数
     */
    private void parseClickParameters(String paramString, Map<String, String> parameters) {
        Matcher matcher = CLICK_PARAM_PATTERN.matcher(paramString);
        if (matcher.find()) {
            String startBox = matcher.group(1);
            if (startBox != null) {
                parameters.put("start_box", startBox.trim());
                Log.d(TAG, "解析CLICK参数: start_box = " + startBox);
            }
        } else {
            Log.w(TAG, "CLICK动作参数解析失败: " + paramString);
        }
    }
    

    
    /**
     * 解析INPUT_TEXT动作的参数
     */
    private void parseInputTextParameters(String paramString, Map<String, String> parameters) {
        Matcher matcher = INPUT_TEXT_PATTERN.matcher(paramString);
        if (matcher.find()) {
            String text = matcher.group(1);
            if (text != null) {
                parameters.put("text", text.trim());
                Log.d(TAG, "解析INPUT_TEXT参数: text = " + text);
            }
        } else {
            Log.w(TAG, "INPUT_TEXT动作参数解析失败: " + paramString);
        }
    }
    
    /**
     * 解析SCROLL动作的参数
     */
    private void parseScrollParameters(String paramString, Map<String, String> parameters) {
        Matcher startBoxMatcher = SCROLL_START_BOX_PATTERN.matcher(paramString);
        Matcher endBoxMatcher = SCROLL_END_BOX_PATTERN.matcher(paramString);
        Matcher directionMatcher = SCROLL_DIRECTION_PATTERN.matcher(paramString);
        
        boolean hasValidParams = false;
        
        if (startBoxMatcher.find()) {
            String startBox = startBoxMatcher.group(1);
            if (startBox != null) {
                parameters.put("start_box", startBox.trim());
                Log.d(TAG, "解析SCROLL参数: start_box = " + startBox);
                hasValidParams = true;
            }
        }
        
        if (endBoxMatcher.find()) {
            String endBox = endBoxMatcher.group(1);
            if (endBox != null) {
                parameters.put("end_box", endBox.trim());
                Log.d(TAG, "解析SCROLL参数: end_box = " + endBox);
                hasValidParams = true;
            }
        }
        
        if (directionMatcher.find()) {
            String direction = directionMatcher.group(1);
            if (direction != null) {
                parameters.put("direction", direction.trim());
                Log.d(TAG, "解析SCROLL参数: direction = " + direction);
                hasValidParams = true;
            }
        }
        
        if (!hasValidParams) {
            Log.w(TAG, "SCROLL动作参数解析失败: " + paramString);
        }
    }
    
    /**
     * 通用参数解析（用于未知或新增的动作类型）
     */
    private void parseGenericParameters(String paramString, Map<String, String> parameters) {
        Matcher paramMatcher = PARAM_PATTERN.matcher(paramString);
        
        while (paramMatcher.find()) {
            String key = paramMatcher.group(1);
            String value = paramMatcher.group(2);
            
            if (key != null && value != null) {
                parameters.put(key.trim(), value.trim());
                Log.d(TAG, "解析通用参数: " + key + " = " + value);
            }
        }
    }
}