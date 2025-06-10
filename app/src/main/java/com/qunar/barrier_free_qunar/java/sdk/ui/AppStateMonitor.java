package com.qunar.barrier_free_qunar.java.sdk.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;

/**
 * 应用状态监听器
 * 用于监听应用是否在前台或后台运行
 */
public class AppStateMonitor {
    
    private static final String TAG = "AppStateMonitor";
    private static final long CHECK_INTERVAL = 1000; // 1秒检查一次，更频繁地检测状态变化
    
    private Context context;
    private Handler handler;
    private Runnable checkRunnable;
    private AppStateListener listener;
    private boolean isMonitoring = false;
    private boolean isAppInForeground = true; // 默认应用在前台
    private String packageName;
    
    public interface AppStateListener {
        /**
         * 应用进入前台
         */
        void onAppEnterForeground();
        
        /**
         * 应用进入后台
         */
        void onAppEnterBackground();
    }
    
    public AppStateMonitor(Context context) {
        this.context = context;
        this.packageName = context.getPackageName();
        this.handler = new Handler(Looper.getMainLooper());
        
        this.checkRunnable = new Runnable() {
            @Override
            public void run() {
                checkAppState();
                if (isMonitoring) {
                    handler.postDelayed(this, CHECK_INTERVAL);
                }
            }
        };
    }
    
    /**
     * 设置状态监听器
     */
    public void setAppStateListener(AppStateListener listener) {
        this.listener = listener;
    }
    
    /**
     * 开始监听
     */
    public void startMonitoring() {
        if (!isMonitoring) {
            isMonitoring = true;
            handler.post(checkRunnable);
            Log.d(TAG, "开始监听应用状态");
        }
    }
    
    /**
     * 停止监听
     */
    public void stopMonitoring() {
        if (isMonitoring) {
            isMonitoring = false;
            handler.removeCallbacks(checkRunnable);
            Log.d(TAG, "停止监听应用状态");
        }
    }
    
    /**
     * 检查应用状态
     */
    private void checkAppState() {
        try {
            boolean currentState = isAppInForeground();
            
            if (currentState != this.isAppInForeground) {
                this.isAppInForeground = currentState;
                
                if (listener != null) {
                    if (this.isAppInForeground) {
                        Log.d(TAG, "应用进入前台");
                        listener.onAppEnterForeground();
                    } else {
                        Log.d(TAG, "应用进入后台");
                        listener.onAppEnterBackground();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查应用状态失败", e);
        }
    }
    
    /**
     * 检查应用是否在前台
     */
    private boolean isAppInForeground() {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                Log.w(TAG, "ActivityManager为空");
                return false;
            }
            
            // 方法1: 检查运行中的任务（在高版本Android中可能受限）
            try {
                List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
                if (runningTasks != null && !runningTasks.isEmpty()) {
                    ActivityManager.RunningTaskInfo topTask = runningTasks.get(0);
                    if (topTask.topActivity != null) {
                        String topPackageName = topTask.topActivity.getPackageName();
                        boolean isForeground = packageName.equals(topPackageName);
                        return isForeground;
                    }
                }
            } catch (SecurityException e) {
                Log.w(TAG, "RunningTask检测失败，权限不足: " + e.getMessage());
            }
            
            // 方法2: 检查运行中的应用进程
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
            if (runningProcesses != null) {
                for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                    if (packageName.equals(processInfo.processName)) {
                        // IMPORTANCE_FOREGROUND和IMPORTANCE_VISIBLE都算前台（用户能看到应用界面）
                        return processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                                             processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
                    }
                }
            }
            
            Log.d(TAG, "未找到应用进程，默认返回false");
            
        } catch (Exception e) {
            Log.e(TAG, "检查前台状态失败", e);
        }
        
        return false;
    }
    
    /**
     * 手动更新应用状态
     */
    public void updateState() {
        checkAppState();
    }
    
    /**
     * 获取当前应用状态
     */
    public boolean isCurrentlyInForeground() {
        return isAppInForeground;
    }
    
    /**
     * 手动更新应用状态（用于特殊情况）
     */
    public void updateAppState(boolean inForeground) {
        if (isAppInForeground != inForeground) {
            isAppInForeground = inForeground;
            
            if (listener != null) {
                if (isAppInForeground) {
                    Log.d(TAG, "手动设置应用进入前台");
                    listener.onAppEnterForeground();
                } else {
                    Log.d(TAG, "手动设置应用进入后台");
                    listener.onAppEnterBackground();
                }
            }
        }
    }
    
    /**
     * 清理资源
     */
    public void destroy() {
        stopMonitoring();
        listener = null;
    }
}