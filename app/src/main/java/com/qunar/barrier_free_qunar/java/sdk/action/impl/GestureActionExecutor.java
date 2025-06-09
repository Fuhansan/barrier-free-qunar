package com.qunar.barrier_free_qunar.java.sdk.action.impl;

import android.util.Log;

import com.qunar.barrier_free_qunar.java.sdk.action.ActionExecutor;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionCommand;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionResult;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.ActionType;
import com.qunar.barrier_free_qunar.java.sdk.gesture.GestureApi;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.Point;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.SlidPoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手势动作执行器
 * 负责执行点击、滑动、返回、Home等手势操作
 */
public class GestureActionExecutor implements ActionExecutor {
    
    private static final String TAG = "GestureActionExecutor";
    
    private final GestureApi gestureApi;
    
    // 匹配坐标的正则表达式
    // 例如: [700,2300,886,2450] 或 [100,200]
    private static final Pattern COORDINATE_PATTERN = Pattern.compile(
        "\\[(\\d+),(\\d+)(?:,(\\d+),(\\d+))?\\]"
    );
    
    public GestureActionExecutor(GestureApi gestureApi) {
        this.gestureApi = gestureApi;
    }
    
    @Override
    public ActionResult execute(ActionCommand command) {
        if (command == null || !canExecute(command)) {
            return ActionResult.failure("不支持的动作指令: " + command);
        }
        
        Log.d(TAG, "执行动作指令: " + command);
        
        try {
            switch (command.getActionType()) {
                case CLICK:
                    return executeClick(command);
                case PERFORM_SWIPE_GESTURE:
                    return executeSwipe(command);
                case BACK:
                    return executeBack(command);
                case HOME:
                    return executeHome(command);
                case SCREENSHOT:
                    return executeScreenshot(command);
                case OPEN_APP:
                    return executeOpenApp(command);
                case COMPLETE:
                    return executeComplete(command);
                default:
                    return ActionResult.failure("未实现的动作类型: " + command.getActionType());
            }
        } catch (Exception e) {
            Log.e(TAG, "执行动作指令失败", e);
            return ActionResult.failure("执行失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean canExecute(ActionCommand command) {
        if (command == null || gestureApi == null) {
            return false;
        }
        
        ActionType actionType = command.getActionType();
        return actionType == ActionType.CLICK ||
               actionType == ActionType.PERFORM_SWIPE_GESTURE ||
               actionType == ActionType.BACK ||
               actionType == ActionType.HOME ||
               actionType == ActionType.SCREENSHOT ||
               actionType == ActionType.OPEN_APP ||
               actionType == ActionType.COMPLETE;
    }
    
    /**
     * 执行点击操作
     */
    private ActionResult executeClick(ActionCommand command) {
        String startBox = command.getParameter("start_box");
        if (startBox == null || startBox.trim().isEmpty()) {
            return ActionResult.failure("点击操作缺少start_box参数");
        }
        
        Point clickPoint = parseClickPoint(startBox);
        if (clickPoint == null) {
            return ActionResult.failure("无法解析点击坐标: " + startBox);
        }
        
        boolean success = gestureApi.click(clickPoint);
        if (success) {
            return ActionResult.success("点击操作执行成功: " + clickPoint);
        } else {
            return ActionResult.failure("点击操作执行失败");
        }
    }
    
    /**
     * 执行滑动操作
     */
    private ActionResult executeSwipe(ActionCommand command) {
        String fromParam = command.getParameter("from");
        String toParam = command.getParameter("to");
        
        if (fromParam == null || toParam == null) {
            return ActionResult.failure("滑动操作缺少from或to参数");
        }
        
        Point fromPoint = parsePoint(fromParam);
        Point toPoint = parsePoint(toParam);
        
        if (fromPoint == null || toPoint == null) {
            return ActionResult.failure("无法解析滑动坐标: from=" + fromParam + ", to=" + toParam);
        }
        
        SlidPoint slidPoint = new SlidPoint(fromPoint, toPoint);
        boolean success = gestureApi.slider(slidPoint);
        
        if (success) {
            return ActionResult.success("滑动操作执行成功: " + fromPoint + " -> " + toPoint);
        } else {
            return ActionResult.failure("滑动操作执行失败");
        }
    }
    
    /**
     * 执行返回操作
     */
    private ActionResult executeBack(ActionCommand command) {
        gestureApi.back();
        return ActionResult.success("返回操作执行成功");
    }
    
    /**
     * 执行Home操作
     */
    private ActionResult executeHome(ActionCommand command) {
        gestureApi.home();
        return ActionResult.success("Home操作执行成功");
    }
    
    /**
     * 执行截屏操作
     */
    private ActionResult executeScreenshot(ActionCommand command) {
        String screenshotUrl = gestureApi.screenShot();
        if (screenshotUrl != null && !screenshotUrl.isEmpty()) {
            return ActionResult.success("截屏操作执行成功", screenshotUrl);
        } else {
            return ActionResult.failure("截屏操作执行失败");
        }
    }
    
    /**
     * 执行打开应用操作
     */
    private ActionResult executeOpenApp(ActionCommand command) {
        String packageName = command.getParameter("packageName");
        if (packageName == null || packageName.trim().isEmpty()) {
            return ActionResult.failure("打开应用操作缺少packageName参数");
        }
        
        gestureApi.openApp(packageName);
        return ActionResult.success("打开应用操作执行成功: " + packageName);
    }
    
    /**
     * 执行完成操作
     */
    private ActionResult executeComplete(ActionCommand command) {
        Log.i(TAG, "任务完成标记");
        return ActionResult.success("任务完成");
    }
    
    /**
     * 解析点击坐标（从矩形区域计算中心点）
     */
    private Point parseClickPoint(String coordinateStr) {
        Matcher matcher = COORDINATE_PATTERN.matcher(coordinateStr);
        if (matcher.find()) {
            try {
                int x1 = Integer.parseInt(matcher.group(1));
                int y1 = Integer.parseInt(matcher.group(2));
                
                // 如果是矩形坐标，计算中心点
                if (matcher.group(3) != null && matcher.group(4) != null) {
                    int x2 = Integer.parseInt(matcher.group(3));
                    int y2 = Integer.parseInt(matcher.group(4));
                    
                    int centerX = (x1 + x2) / 2;
                    int centerY = (y1 + y2) / 2;
                    
                    return new Point(centerX, centerY);
                } else {
                    // 如果是单点坐标
                    return new Point(x1, y1);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "解析坐标失败: " + coordinateStr, e);
            }
        }
        return null;
    }
    
    /**
     * 解析单点坐标
     */
    private Point parsePoint(String coordinateStr) {
        Matcher matcher = COORDINATE_PATTERN.matcher(coordinateStr);
        if (matcher.find()) {
            try {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                return new Point(x, y);
            } catch (NumberFormatException e) {
                Log.e(TAG, "解析坐标失败: " + coordinateStr, e);
            }
        }
        return null;
    }
}