package com.qunar.barrier_free_qunar.java.sdk.gesture;

import android.accessibilityservice.AccessibilityService;


import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.Point;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.SlidPoint;


public class GestureApi {

    private GestureInstance gestureInstance;


    public GestureApi(AccessibilityService accessibilityService) {
        this.gestureInstance = new GestureInstance(accessibilityService);
    }


    /**
     * 打开app
     *
     * @param packageName app 包名
     */
    public void openApp(String packageName) {
        gestureInstance.openApp(packageName);
    }

    /**
     * 滑动
     *
     * @param slidPoint 坐标系
     * @return 执行成功与否
     */
    public boolean slider(SlidPoint slidPoint) {
        return gestureInstance.performSwipeGesture(slidPoint);
    }


    /**
     * 点击屏幕坐标点
     *
     * @param point 左边
     * @return 执行成功与否
     */
    public boolean click(Point point) {
        return gestureInstance.clickGesture(point);
    }


    /**
     * 截屏方法 - 仅适用于Android 11及以上版本
     *
     * @return 返回一个URL
     */
    public String screenShot() {
        try {
            return gestureInstance.screenShot();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void home() {
        gestureInstance.home();
    }

    public void back() {
        gestureInstance.back();
    }


}
