package com.qunar.barrier_free_qunar.java.sdk;

import android.accessibilityservice.AccessibilityService;


import com.qunar.barrier_free_qunar.java.sdk.gesture.GestureApi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BarrierFreeBuilder {

    // 单例，使用Map存储各种对象
    private static final ConcurrentMap<String, Object> instance = new ConcurrentHashMap<>();

    public static GestureApi buildGestureApi(AccessibilityService accessibilityService) {
        String name = accessibilityService.getClass().getName();
        // 如果已经存在，直接返回
        if (instance.containsKey(name)) {
            Object result = instance.get(name);

            if (result instanceof GestureApi) {
                return (GestureApi) result;
            }

            throw new RuntimeException("错误服务类型");
        }
        // 如果不存在，创建并返回
        GestureApi gestureApi = new GestureApi(accessibilityService);
        instance.put(name, gestureApi);
        return gestureApi;
    }


}
