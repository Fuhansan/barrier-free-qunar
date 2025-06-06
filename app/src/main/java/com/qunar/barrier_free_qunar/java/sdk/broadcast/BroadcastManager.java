package com.qunar.barrier_free_qunar.java.sdk.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 广播管理器 - 统一管理应用内的所有广播操作
 * 支持多个广播的注册、注销和发送
 * 提供线程安全的广播管理功能
 */
public class BroadcastManager {
    private static final String TAG = "BroadcastManager";
    private static volatile BroadcastManager instance;
    
    private final Context context;
    private final LocalBroadcastManager localBroadcastManager;
    
    // 存储已注册的广播接收器
    private final Map<String, BroadcastReceiver> registeredReceivers;
    // 存储广播接收器的注册状态
    private final Map<String, Boolean> receiverStatus;
    
    private BroadcastManager(Context context) {
        this.context = context.getApplicationContext();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(this.context);
        this.registeredReceivers = new ConcurrentHashMap<>();
        this.receiverStatus = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取单例实例
     */
    public static BroadcastManager getInstance(Context context) {
        if (instance == null) {
            synchronized (BroadcastManager.class) {
                if (instance == null) {
                    instance = new BroadcastManager(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册广播接收器
     * @param receiverId 接收器唯一标识
     * @param receiver 广播接收器
     * @param actions 要监听的广播动作列表
     * @return 是否注册成功
     */
    public boolean registerReceiver(String receiverId, BroadcastReceiver receiver, String... actions) {
        try {
            if (receiverId == null || receiver == null || actions == null || actions.length == 0) {
                Log.w(TAG, "注册广播失败：参数不能为空");
                return false;
            }
            
            // 检查是否已经注册
            if (isReceiverRegistered(receiverId)) {
                // Log.d(TAG, "广播接收器已注册，跳过重复注册: " + receiverId);
                return true;
            }
            
            // 创建IntentFilter
            IntentFilter filter = new IntentFilter();
            for (String action : actions) {
                filter.addAction(action);
            }
            
            // 注册广播接收器
            localBroadcastManager.registerReceiver(receiver, filter);
            
            // 记录注册信息
            registeredReceivers.put(receiverId, receiver);
            receiverStatus.put(receiverId, true);
            
            // Log.d(TAG, "广播接收器注册成功: " + receiverId + ", 监听动作: " + String.join(", ", actions));
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "注册广播接收器失败: " + receiverId, e);
            return false;
        }
    }
    
    /**
     * 注销广播接收器
     * @param receiverId 接收器唯一标识
     * @return 是否注销成功
     */
    public boolean unregisterReceiver(String receiverId) {
        try {
            if (receiverId == null) {
                Log.w(TAG, "注销广播失败：接收器ID不能为空");
                return false;
            }
            
            BroadcastReceiver receiver = registeredReceivers.get(receiverId);
            if (receiver == null || !isReceiverRegistered(receiverId)) {
                // Log.d(TAG, "广播接收器未注册或已注销: " + receiverId);
                return true;
            }
            
            // 注销广播接收器
            localBroadcastManager.unregisterReceiver(receiver);
            
            // 清除注册信息
            registeredReceivers.remove(receiverId);
            receiverStatus.put(receiverId, false);
            
            // Log.d(TAG, "广播接收器注销成功: " + receiverId);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "注销广播接收器失败: " + receiverId, e);
            return false;
        }
    }
    
    /**
     * 发送广播
     * @param action 广播动作
     * @param extras 广播数据
     * @return 是否发送成功
     */
    public boolean sendBroadcast(String action, Map<String, Object> extras) {
        try {
            if (action == null) {
                Log.w(TAG, "发送广播失败：动作不能为空");
                return false;
            }
            
            Intent intent = new Intent(action);
            
            // 添加额外数据
            if (extras != null) {
                for (Map.Entry<String, Object> entry : extras.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    
                    if (value instanceof String) {
                        intent.putExtra(key, (String) value);
                    } else if (value instanceof Integer) {
                        intent.putExtra(key, (Integer) value);
                    } else if (value instanceof Long) {
                        intent.putExtra(key, (Long) value);
                    } else if (value instanceof Boolean) {
                        intent.putExtra(key, (Boolean) value);
                    } else if (value instanceof Double) {
                        intent.putExtra(key, (Double) value);
                    } else if (value instanceof Float) {
                        intent.putExtra(key, (Float) value);
                    } else {
                        // 其他类型转为字符串
                        intent.putExtra(key, value.toString());
                    }
                }
            }
            
            // 发送本地广播
            localBroadcastManager.sendBroadcast(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "发送广播失败: " + action, e);
            return false;
        }
    }
    
    /**
     * 检查广播接收器是否已注册
     * @param receiverId 接收器唯一标识
     * @return 是否已注册
     */
    public boolean isReceiverRegistered(String receiverId) {
        return receiverStatus.getOrDefault(receiverId, false) && registeredReceivers.containsKey(receiverId);
    }
    

}