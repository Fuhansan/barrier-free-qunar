package com.qunar.barrier_free_qunar.java.sdk.ui;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * 悬浮按钮调试助手
 * 用于排查悬浮按钮显示问题
 */
public class FloatingButtonDebugHelper {
    private static final String TAG = "FloatingButtonDebugHelper";
    
    /**
     * 检查悬浮窗权限
     */
    public static boolean checkOverlayPermission(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean hasPermission = Settings.canDrawOverlays(context);
            Log.d(TAG, "悬浮窗权限检查: " + hasPermission);
            return hasPermission;
        } else {
            Log.d(TAG, "Android版本低于6.0，无需检查悬浮窗权限");
            return true;
        }
    }
    
    /**
     * 强制显示悬浮按钮（用于调试）
     */
    public static void forceShowFloatingButton(FloatingButtonService service) {
        if (service == null) {
            Log.e(TAG, "FloatingButtonService为空");
            return;
        }
        
        Log.d(TAG, "强制显示悬浮按钮");
        service.showFloatingButton();
    }
    
    /**
     * 打印应用状态信息
     */
    public static void printAppStateInfo(AppStateMonitor monitor) {
        if (monitor == null) {
            Log.e(TAG, "AppStateMonitor为空");
            return;
        }
        
        Log.d(TAG, "当前应用状态 - 在前台: " + monitor.isCurrentlyInForeground());
        // 手动触发一次状态检查
        monitor.updateState();
    }
    
    /**
     * 完整的调试信息输出
     */
    public static void debugFloatingButton(Context context, FloatingButtonService service, AppStateMonitor monitor) {
        Log.d(TAG, "=== 悬浮按钮调试信息 ===");
        
        // 检查权限
        boolean hasPermission = checkOverlayPermission(context);
        Log.d(TAG, "悬浮窗权限: " + hasPermission);
        
        // 检查服务状态
        if (service != null) {
            Log.d(TAG, "FloatingButtonService: 已初始化");
        } else {
            Log.e(TAG, "FloatingButtonService: 未初始化");
        }
        
        // 检查应用状态监听器
        if (monitor != null) {
            Log.d(TAG, "AppStateMonitor: 已初始化");
            printAppStateInfo(monitor);
        } else {
            Log.e(TAG, "AppStateMonitor: 未初始化");
        }
        
        Log.d(TAG, "=== 调试信息结束 ===");
    }
}