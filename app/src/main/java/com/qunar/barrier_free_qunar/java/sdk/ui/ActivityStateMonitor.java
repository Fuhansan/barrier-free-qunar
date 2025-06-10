package com.qunar.barrier_free_qunar.java.sdk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity状态监听器
 * 通过监听Activity生命周期来精确判断应用是否有界面在前台显示
 */
public class ActivityStateMonitor implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "ActivityStateMonitor";
    
    private int activityCount = 0; // 当前活跃的Activity数量
    private int visibleActivityCount = 0; // 当前可见的Activity数量
    private boolean isAppVisible = false; // 应用是否有界面可见
    
    private AppStateListener listener;
    
    public interface AppStateListener {
        void onAppBecomeVisible();
        void onAppBecomeInvisible();
    }
    
    public void setListener(AppStateListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG, "Activity创建: " + activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityStarted(Activity activity) {
        activityCount++;
        Log.d(TAG, "Activity启动: " + activity.getClass().getSimpleName() + ", 活跃数量: " + activityCount);
        
        if (activityCount == 1) {
            // 第一个Activity启动，应用变为可见
            if (!isAppVisible) {
                isAppVisible = true;
                Log.i(TAG, "应用变为可见");
                if (listener != null) {
                    listener.onAppBecomeVisible();
                }
            }
        }
    }
    
    @Override
    public void onActivityResumed(Activity activity) {
        visibleActivityCount++;
        Log.d(TAG, "Activity恢复: " + activity.getClass().getSimpleName() + ", 可见数量: " + visibleActivityCount);
    }
    
    @Override
    public void onActivityPaused(Activity activity) {
        visibleActivityCount--;
        Log.d(TAG, "Activity暂停: " + activity.getClass().getSimpleName() + ", 可见数量: " + visibleActivityCount);
    }
    
    @Override
    public void onActivityStopped(Activity activity) {
        activityCount--;
        Log.d(TAG, "Activity停止: " + activity.getClass().getSimpleName() + ", 活跃数量: " + activityCount);
        
        if (activityCount == 0) {
            // 所有Activity都停止，应用变为不可见
            if (isAppVisible) {
                isAppVisible = false;
                Log.i(TAG, "应用变为不可见");
                if (listener != null) {
                    listener.onAppBecomeInvisible();
                }
            }
        }
    }
    
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d(TAG, "Activity保存状态: " + activity.getClass().getSimpleName());
    }
    
    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, "Activity销毁: " + activity.getClass().getSimpleName());
    }
    
    /**
     * 获取当前应用是否有界面可见
     */
    public boolean isAppVisible() {
        return isAppVisible;
    }
    
    /**
     * 获取当前活跃的Activity数量
     */
    public int getActiveActivityCount() {
        return activityCount;
    }
    
    /**
     * 获取当前可见的Activity数量
     */
    public int getVisibleActivityCount() {
        return visibleActivityCount;
    }
    
    /**
     * 获取状态信息
     */
    @SuppressLint("DefaultLocale")
    public String getStateInfo() {
        return String.format("可见: %s, 活跃Activity: %d, 可见Activity: %d", 
                           isAppVisible, activityCount, visibleActivityCount);
    }
}