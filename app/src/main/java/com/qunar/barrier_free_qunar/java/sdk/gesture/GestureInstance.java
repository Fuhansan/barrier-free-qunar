package com.qunar.barrier_free_qunar.java.sdk.gesture;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.hardware.HardwareBuffer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.system.Os;
import android.util.Log;

import java.util.List;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.qunar.barrier_free_qunar.java.sdk.consts.URLConst;
import com.qunar.barrier_free_qunar.java.sdk.model.OssUploadResult;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.Point;
import com.qunar.barrier_free_qunar.java.sdk.model.coordinate.SlidPoint;
import com.qunar.barrier_free_qunar.java.sdk.model.http.HttpResult;
import com.qunar.barrier_free_qunar.java.sdk.util.BFHttpUtils;
import com.qunar.barrier_free_qunar.kotlin.utils.HttpResponseParser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class GestureInstance {

    private final AccessibilityService service;

    private Gson gson;

    // 创建一个自定义线程池，并配置参数，并指定淘汰策略
    private static ExecutorService executorService = new ThreadPoolExecutor(
            10, // 核心线程数
            20, // 最大线程数
            0L, // 空闲线程存活时间
            TimeUnit.MILLISECONDS, // 时间单位
            new ArrayBlockingQueue<>(100), // 任务队列
            new ThreadPoolExecutor.DiscardPolicy() // 不处理任务直接丢弃
    );


    public GestureInstance(AccessibilityService service) {
        this.service = service;
        this.gson = new Gson();
    }

    /**
     * 滑动屏幕
     *
     * @param slidPoint 滑动的坐标系
     * @return ture/false
     */
    public Boolean slider(SlidPoint slidPoint) {

        float startX = slidPoint.getStart().getX();
        float endX = slidPoint.getEnd().getX();

        float startY = slidPoint.getStart().getY();
        float endY = slidPoint.getEnd().getY();


        Log.d("滑动起始坐标", "startX=" + startX + ",startY=" + startY);
        Log.d("滑动结束坐标", "endX=" + endX + ",endY=" + endY);

        Path dragDownPath = new Path();
        dragDownPath.moveTo(startX, startY);
        dragDownPath.lineTo(endX, endY);

        GestureDescription.StrokeDescription downStroke = new GestureDescription.StrokeDescription(
                dragDownPath,
                slidPoint.getStartTime(),  // 起始时间
                slidPoint.getDuration() // 持续时间
        );

        return executeGesture(downStroke);
    }


    /**
     * @param point 点击的位置
     * @return ture/false
     */
    public boolean clickGesture(Point point) {
        Path clickPoint = new Path();
        clickPoint.moveTo(point.getX(), point.getY());
        clickPoint.lineTo(point.getX(), point.getY()); // 修复坐标错误
        Log.d("点击坐标", "x=" + point.getX() + ",y=" + point.getY());

        // 显示点击位置的视觉反馈
        showClickFeedback(point);

        GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(
                clickPoint,
                0,  // 起始时间
                1 // 持续时间
        );
        return executeGesture(strokeDescription);
    }

    /**
     * 显示点击位置的视觉反馈
     *
     * @param point 点击的坐标点
     */
    private void showClickFeedback(Point point) {
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.post(() -> {
            try {
                // 创建点击指示器视图
                android.widget.FrameLayout clickIndicator = new android.widget.FrameLayout(service);

                // 创建外圆（白色边框）
                android.view.View outerCircle = new android.view.View(service);
                android.graphics.drawable.GradientDrawable outerDrawable = new android.graphics.drawable.GradientDrawable();
                outerDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                outerDrawable.setColor(0x00000000); // 透明填充
                outerDrawable.setStroke(8, 0xFFFFFFFF); // 白色边框
                outerCircle.setBackground(outerDrawable);

                // 创建内圆（红色填充）
                android.view.View innerCircle = new android.view.View(service);
                android.graphics.drawable.GradientDrawable innerDrawable = new android.graphics.drawable.GradientDrawable();
                innerDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                innerDrawable.setColor(0xFFFF4444); // 红色填充
                innerCircle.setBackground(innerDrawable);

                // 设置布局参数
                int outerSize = 120; // 外圆直径
                int innerSize = 60;  // 内圆直径

                android.widget.FrameLayout.LayoutParams outerParams = new android.widget.FrameLayout.LayoutParams(
                        outerSize, outerSize);
                outerParams.gravity = android.view.Gravity.CENTER;

                android.widget.FrameLayout.LayoutParams innerParams = new android.widget.FrameLayout.LayoutParams(
                        innerSize, innerSize);
                innerParams.gravity = android.view.Gravity.CENTER;

                clickIndicator.addView(outerCircle, outerParams);
                clickIndicator.addView(innerCircle, innerParams);

                // 设置悬浮窗参数
                android.view.WindowManager windowManager = (android.view.WindowManager) service.getSystemService(android.content.Context.WINDOW_SERVICE);
                android.view.WindowManager.LayoutParams params = new android.view.WindowManager.LayoutParams(
                        outerSize,
                        outerSize,
                        android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        android.graphics.PixelFormat.TRANSLUCENT
                );

                // 设置位置（以点击点为中心）
                params.gravity = android.view.Gravity.TOP | android.view.Gravity.LEFT;
                params.x = (int) (point.getX() - (float) outerSize / 2);
                params.y = (int) (point.getY() - (float) outerSize / 2);

                // 添加到窗口
                windowManager.addView(clickIndicator, params);

                // 添加动画效果
                android.view.animation.ScaleAnimation scaleAnimation = new android.view.animation.ScaleAnimation(
                        0.0f, 1.0f, 0.0f, 1.0f,
                        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                        android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
                );
                scaleAnimation.setDuration(200);
                scaleAnimation.setInterpolator(new android.view.animation.OvershootInterpolator());

                android.view.animation.AlphaAnimation alphaAnimation = new android.view.animation.AlphaAnimation(1.0f, 0.0f);
                alphaAnimation.setDuration(800);
                alphaAnimation.setStartOffset(200);

                android.view.animation.AnimationSet animationSet = new android.view.animation.AnimationSet(false);
                animationSet.addAnimation(scaleAnimation);
                animationSet.addAnimation(alphaAnimation);

                clickIndicator.startAnimation(animationSet);

                // 1秒后自动移除
                handler.postDelayed(() -> {
                    try {
                        windowManager.removeView(clickIndicator);
                    } catch (Exception e) {
                        Log.e("【点击反馈】", "移除点击指示器失败", e);
                    }
                }, 1000);

                Log.d("【点击反馈】", "显示点击位置指示器: (" + point.getX() + ", " + point.getY() + ")");

            } catch (Exception e) {
                Log.e("【点击反馈】", "显示点击反馈失败", e);
            }
        });
    }


    /**
     * 使用无障碍服务实现文字输入功能
     * 支持多种输入策略，在当前活动应用中进行文字输入
     *
     * @param text 要输入的文字
     * @return 是否输入成功
     */
    public boolean inputText(String text) {
        if (text == null || text.isEmpty()) {
            Log.w("【文字输入】", "输入文字为空");
            return false;
        }

        try {
            // 获取当前活动窗口的根节点
            android.view.accessibility.AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
            if (rootNode == null) {
                Log.w("【文字输入】", "无法获取当前活动窗口");
                return false;
            }

            String currentPackage = rootNode.getPackageName() != null ? rootNode.getPackageName().toString() : "未知应用";
            Log.d("【文字输入】", "当前活动应用: " + currentPackage);

            // 策略1: 尝试查找当前有焦点的可编辑节点
            android.view.accessibility.AccessibilityNodeInfo focusedNode = findFocusedEditableNode(rootNode);
            if (focusedNode != null && performTextInput(focusedNode, text, "焦点节点")) {
                rootNode.recycle();
                return true;
            }

            // 策略2: 查找所有可编辑节点，优先选择可见且可点击的
            java.util.List<android.view.accessibility.AccessibilityNodeInfo> editableNodes = findAllEditableNodes(rootNode);
            if (!editableNodes.isEmpty()) {
                // 按优先级排序：可见且可点击 > 可见 > 其他
                editableNodes.sort((a, b) -> {
                    int scoreA = getNodePriority(a);
                    int scoreB = getNodePriority(b);
                    return Integer.compare(scoreB, scoreA); // 降序排列
                });

                for (android.view.accessibility.AccessibilityNodeInfo node : editableNodes) {
                    if (performTextInput(node, text, "可编辑节点")) {
                        // 清理资源
                        for (android.view.accessibility.AccessibilityNodeInfo n : editableNodes) {
                            n.recycle();
                        }
                        rootNode.recycle();
                        return true;
                    }
                }

                // 清理资源
                for (android.view.accessibility.AccessibilityNodeInfo node : editableNodes) {
                    node.recycle();
                }
            }

            // 策略3: 尝试使用全局粘贴操作（如果支持）
            if (tryGlobalPaste(text)) {
                rootNode.recycle();
                return true;
            }

            rootNode.recycle();
            Log.w("【文字输入】", "在应用 " + currentPackage + " 中所有输入策略都失败了");

        } catch (Exception e) {
            Log.e("【文字输入】", "输入文字时发生错误", e);
        }

        return false;
    }

    /**
     * 查找当前有焦点的可编辑节点
     */
    private android.view.accessibility.AccessibilityNodeInfo findFocusedEditableNode(android.view.accessibility.AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return null;

        // 查找有输入焦点的节点
        android.view.accessibility.AccessibilityNodeInfo focusedNode = rootNode.findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT);
        if (focusedNode != null && isEditableNode(focusedNode)) {
            return focusedNode;
        }
        if (focusedNode != null) {
            focusedNode.recycle();
        }

        // 查找有可访问性焦点的节点
        focusedNode = rootNode.findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
        if (focusedNode != null && isEditableNode(focusedNode)) {
            return focusedNode;
        }
        if (focusedNode != null) {
            focusedNode.recycle();
        }

        return null;
    }

    /**
     * 查找所有可编辑节点
     */
    private java.util.List<android.view.accessibility.AccessibilityNodeInfo> findAllEditableNodes(android.view.accessibility.AccessibilityNodeInfo rootNode) {
        java.util.List<android.view.accessibility.AccessibilityNodeInfo> editableNodes = new java.util.ArrayList<>();
        if (rootNode != null) {
            collectEditableNodes(rootNode, editableNodes);
        }
        return editableNodes;
    }

    /**
     * 递归收集所有可编辑节点
     */
    private void collectEditableNodes(android.view.accessibility.AccessibilityNodeInfo node, java.util.List<android.view.accessibility.AccessibilityNodeInfo> result) {
        if (node == null) return;

        if (isEditableNode(node)) {
            result.add(node);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            android.view.accessibility.AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectEditableNodes(child, result);
                // 注意：这里不要回收child，因为它可能被添加到result中
            }
        }
    }

    /**
     * 判断节点是否为可编辑节点
     */
    private boolean isEditableNode(android.view.accessibility.AccessibilityNodeInfo node) {
        if (node == null) return false;

        // 检查是否可编辑
        if (node.isEditable()) {
            return true;
        }

        // 检查常见的输入框类型
        String className = node.getClassName() != null ? node.getClassName().toString() : "";
        return className.equals("android.widget.EditText") ||
                className.equals("android.widget.AutoCompleteTextView") ||
                className.equals("android.widget.MultiAutoCompleteTextView") ||
                className.contains("EditText") ||
                (className.contains("Text") && node.isFocusable() && node.isClickable());
    }

    /**
     * 获取节点优先级分数
     */
    private int getNodePriority(android.view.accessibility.AccessibilityNodeInfo node) {
        if (node == null) return 0;

        int score = 0;
        if (node.isVisibleToUser()) score += 10;
        if (node.isClickable()) score += 5;
        if (node.isFocusable()) score += 3;
        if (node.isEnabled()) score += 2;
        if (node.isEditable()) score += 1;

        return score;
    }

    /**
     * 执行文字输入操作
     */
    private boolean performTextInput(android.view.accessibility.AccessibilityNodeInfo node, String text, String nodeType) {
        if (node == null) return false;

        try {
            // 确保节点获得焦点
            if (node.isFocusable()) {
                node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_FOCUS);
            }

            // 如果节点可点击，先点击它
            if (node.isClickable()) {
                node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
                // 等待一小段时间让界面响应
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 尝试设置文字
            android.os.Bundle arguments = new android.os.Bundle();
            arguments.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);

            boolean result = node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

            if (result) {
                Log.d("【文字输入】", "通过" + nodeType + "输入成功: " + text);
                return true;
            } else {
                Log.d("【文字输入】", "通过" + nodeType + "输入失败，尝试其他方式");
                // 尝试先选择所有文本再输入
                if (node.getText() != null && node.getText().length() > 0) {
                    android.os.Bundle selectArgs = new android.os.Bundle();
                    selectArgs.putInt(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0);
                    selectArgs.putInt(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, node.getText().length());
                    node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_SELECTION, selectArgs);
                }
                return node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            }

        } catch (Exception e) {
            Log.w("【文字输入】", "通过" + nodeType + "输入时发生错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 尝试使用全局粘贴操作
     */
    private boolean tryGlobalPaste(String text) {
        try {
            // 将文字复制到剪贴板
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) service.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                android.content.ClipData clip = android.content.ClipData.newPlainText("input_text", text);
                clipboard.setPrimaryClip(clip);

                // 尝试执行全局粘贴操作
                boolean result = service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK);
                // 注意：Android无障碍服务没有直接的全局粘贴操作，这里使用返回操作作为示例
                // 实际应用中可能需要通过其他方式实现粘贴功能
                if (result) {
                    Log.d("【文字输入】", "通过全局粘贴输入成功: " + text);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.w("【文字输入】", "全局粘贴操作失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 清空输入框内容
     * 使用多种策略尝试清空当前焦点的输入框
     *
     * @return 是否清空成功
     */
    public boolean clearInputText() {
        try {
            android.view.accessibility.AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
            if (rootNode == null) {
                Log.w("【文字输入】", "无法获取当前活动窗口");
                return false;
            }

            String currentPackage = rootNode.getPackageName() != null ? rootNode.getPackageName().toString() : "未知应用";
            Log.d("【文字输入】", "当前活动应用: " + currentPackage);

            // 策略1: 查找有焦点的可编辑节点
            android.view.accessibility.AccessibilityNodeInfo focusedNode = findFocusedEditableNode(rootNode);
            if (focusedNode != null && performTextClear(focusedNode, "焦点节点")) {
                rootNode.recycle();
                return true;
            }

            // 策略2: 查找所有可编辑节点并尝试清空
            java.util.List<android.view.accessibility.AccessibilityNodeInfo> editableNodes = findAllEditableNodes(rootNode);
            if (!editableNodes.isEmpty()) {
                editableNodes.sort((a, b) -> {
                    int scoreA = getNodePriority(a);
                    int scoreB = getNodePriority(b);
                    return Integer.compare(scoreB, scoreA);
                });

                for (android.view.accessibility.AccessibilityNodeInfo node : editableNodes) {
                    if (performTextClear(node, "可编辑节点")) {
                        for (android.view.accessibility.AccessibilityNodeInfo n : editableNodes) {
                            n.recycle();
                        }
                        rootNode.recycle();
                        return true;
                    }
                }

                for (android.view.accessibility.AccessibilityNodeInfo node : editableNodes) {
                    node.recycle();
                }
            }

            rootNode.recycle();
            Log.w("【文字输入】", "在应用 " + currentPackage + " 中无法清空输入框");

        } catch (Exception e) {
            Log.e("【文字输入】", "清空输入框时发生错误", e);
        }

        return false;
    }

    /**
     * 执行文字清空操作
     */
    private boolean performTextClear(android.view.accessibility.AccessibilityNodeInfo node, String nodeType) {
        if (node == null) return false;

        try {
            // 确保节点获得焦点
            if (node.isFocusable()) {
                node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_FOCUS);
            }

            // 如果节点可点击，先点击它
            if (node.isClickable()) {
                node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 策略1: 全选后删除
            if (node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_FOCUS)) {
                // 尝试选择所有文本（使用长按或其他方式）
                android.os.Bundle selectArgs = new android.os.Bundle();
                selectArgs.putInt(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0);
                selectArgs.putInt(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, node.getText() != null ? node.getText().length() : 0);
                if (node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_SELECTION, selectArgs)) {
                    // 删除选中的文本
                    android.os.Bundle deleteArgs = new android.os.Bundle();
                    deleteArgs.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                    if (node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, deleteArgs)) {
                        Log.d("【文字输入】", "通过" + nodeType + "选择删除清空成功");
                        return true;
                    }
                }
            }

            // 策略2: 设置空文字
            android.os.Bundle arguments = new android.os.Bundle();
            arguments.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");

            if (node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
                Log.d("【文字输入】", "通过" + nodeType + "设置空文字清空成功");
                return true;
            }

            // 策略3: 尝试通过设置选择范围后删除
            if (node.getText() != null && node.getText().length() > 0) {
                android.os.Bundle selectArgs = new android.os.Bundle();
                selectArgs.putInt(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0);
                selectArgs.putInt(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, node.getText().length());
                if (node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_SELECTION, selectArgs)) {
                    // 用空字符串替换选中内容
                    android.os.Bundle replaceArgs = new android.os.Bundle();
                    replaceArgs.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
                    if (node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, replaceArgs)) {
                        Log.d("【文字输入】", "通过" + nodeType + "选择替换清空成功");
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            Log.w("【文字输入】", "通过" + nodeType + "清空时发生错误: " + e.getMessage());
        }

        return false;
    }

    /**
     * 打开应用
     *
     * @param packageName 应用包名或Activity完整类名
     */
    public void openApp(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            Log.e("openApp", "包名为空");
            return;
        }

        String cleanPackageName = packageName.trim();
        if (cleanPackageName.startsWith("package:")) {
            cleanPackageName = cleanPackageName.substring(8);
        }

        PackageManager pm = service.getPackageManager();

        try {
            // 判断输入的是应用包名还是Activity完整类名
            if (cleanPackageName.contains("/")) {
                // 格式: com.example.app/com.example.app.MainActivity
                String[] parts = cleanPackageName.split("/");
                if (parts.length == 2) {
                    String appPackage = parts[0];
                    String activityName = parts[1];

                    Intent intent = new Intent();
                    intent.setClassName(appPackage, activityName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);

                    service.startActivity(intent);
                    Log.i("openApp", "通过Activity类名启动: " + cleanPackageName);
                    return;
                }
            } else if (cleanPackageName.contains(".") && cleanPackageName.split("\\.").length > 3) {
                // 可能是完整的Activity类名，尝试提取包名
                String[] parts = cleanPackageName.split("\\.");
                if (parts.length > 3) {
                    // 假设前3部分是包名，剩余部分是Activity名
                    StringBuilder packageBuilder = new StringBuilder();
                    for (int i = 0; i < 3; i++) {
                        if (i > 0) packageBuilder.append(".");
                        packageBuilder.append(parts[i]);
                    }
                    String appPackage = packageBuilder.toString();

                    Intent intent = new Intent();
                    intent.setClassName(appPackage, cleanPackageName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);

                    service.startActivity(intent);
                    Log.i("openApp", "通过完整Activity类名启动: " + cleanPackageName);
                    return;
                }
            }

            // 作为普通应用包名处理
            Intent launchIntent = pm.getLaunchIntentForPackage(cleanPackageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                service.startActivity(launchIntent);
                Log.i("openApp", "通过包名启动应用: " + cleanPackageName);
                return;
            }

            // 如果getLaunchIntentForPackage失败，尝试查找LAUNCHER Activity
            Intent mainIntent = new Intent(Intent.ACTION_MAIN);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mainIntent.setPackage(cleanPackageName);

            List<ResolveInfo> activities = pm.queryIntentActivities(mainIntent, 0);
            if (!activities.isEmpty()) {
                ResolveInfo resolveInfo = activities.get(0);
                String activityName = resolveInfo.activityInfo.name;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClassName(cleanPackageName, activityName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                service.startActivity(intent);
                Log.i("openApp", "通过查找Activity启动: " + cleanPackageName + "/" + activityName);
                return;
            }

            Log.e("openApp", "无法启动应用: " + cleanPackageName);

        } catch (Exception e) {
            Log.e("openApp", "启动应用失败: " + cleanPackageName + ", 错误: " + e.getMessage());
        }
    }


    private boolean executeGesture(GestureDescription.StrokeDescription strokeDescription) {
        try {

            GestureDescription build = new GestureDescription
                    .Builder()
                    .addStroke(strokeDescription)
                    .build();

            return service.dispatchGesture(build, new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    // Log.d("【无障碍服务】自定义手势动作", "执行完成...");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    // Log.d("【无障碍服务】自定义手势动作", "执行取消...");
                }
            }, null);
        } catch (Exception e) {
            Log.e("【无障碍服务-错误】自定义手势动作", "执行错误", e);
        }
        return false;
    }


    /**
     * 执行屏幕截图并显示
     */
    public String screenShot() throws InterruptedException {
        SystemClock.sleep(2000);
        //这种方式是直接通过系统的截图，不能放回图片的数据
        // service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT);
        // 调用screenShotWithBase64方法来获取截图内容并转化为Bitmap
        return screenShotWithBase64();
    }

    /**
     * 执行屏幕截图并返回URL
     *
     * @return 图片URL，如果截图失败则返回null
     */
    public String screenShotWithBase64() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> resultUrl = new AtomicReference<>(); // 使用数组来存储结果，因为内部类需要final变量
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            service.takeScreenshot(0, executorService, new AccessibilityService.TakeScreenshotCallback() {
                @Override
                public void onSuccess(@NonNull AccessibilityService.ScreenshotResult screenshot) {
                    try {
                        HardwareBuffer hardwareBuffer = screenshot.getHardwareBuffer();
                        Bitmap bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, screenshot.getColorSpace());
                        // 保存图片到相册
                        Uri uri = saveImageInQ(bitmap, service);
                        Log.d("【无障碍服务】截图", "截图成功：" + uri);

                        // 这里把图片转换成File
                        File imageFile = bitmapToFile(bitmap, service);
                        if (imageFile != null) {
                            // 调用http接口上传图片文件（使用multipart/form-data格式）
                            HttpResult response = BFHttpUtils.postFile(URLConst.BF_UPLOAD_FILE, imageFile, null);

                            if (response.isSuccess()) {
                                Object obj = response.getData();

                                if (obj != null) {
                                    HttpResult httpResult = gson.fromJson(obj.toString(), HttpResult.class);
                                    Object data = httpResult.getData();

                                    // 将LinkedTreeMap转换为OssUploadResult对象
                                    OssUploadResult ossResult = gson.fromJson(gson.toJson(data), OssUploadResult.class);
                                    String url = ossResult.getUrl();
                                    Log.d("【无障碍服务-成功】上传", "文件URL: " + url);

                                    // 设置返回结果
                                    resultUrl.compareAndSet(resultUrl.get(), url);
                                } else {
                                    Log.e("【无障碍服务-错误】上传", "图片文件上传失败: " + response.getMsg());
                                }
                            } else {
                                Log.e("【无障碍服务-错误】上传", "上传请求失败: " + response.getMsg());
                            }

                            // 上传完成后删除临时文件
                            if (imageFile.delete()) {
                                Log.d("【无障碍服务】清理", "临时文件已删除: " + imageFile.getAbsolutePath());
                            }

                            // 释放硬件缓冲区
                            hardwareBuffer.close();
                            // 在界面上显示截图
                            showBitmap(bitmap);
                        } else {
                            Log.e("【无障碍服务-错误】截图", "无法创建图片文件");
                        }
                    } finally {
                        // 无论成功还是失败，都要释放CountDownLatch
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(int errorCode) {
                    Log.e("【无障碍服务-错误】截图", "截图失败，错误码：" + errorCode);
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);

        return resultUrl.get();
    }


    public Uri saveImageInQ(Bitmap bitmap, Context context) {
        String filename = "IMG_" + System.currentTimeMillis() + ".jpg";
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);

        ContentResolver contentResolver = context.getContentResolver();
        Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri == null) {
            return null;
        }

        try (OutputStream fos = contentResolver.openOutputStream(imageUri)) {
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                fos.flush();
            }
        } catch (IOException e) {
            Log.e("ImageSave", "io error：", e);
        }

        contentValues.clear();
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
        int rowsUpdated = contentResolver.update(imageUri, contentValues, null, null);

        if (rowsUpdated <= 0) {
            Log.w("ImageSave", "Failed to mark image as non-pending");
        }

        return imageUri;
    }


    /**
     * 截图后弹窗在界面显示
     * 描述：在截图后，用户感知不到截图的过程，所以需要最终展示出截图的效果，在界面上显示出来。
     *
     * @param bitmap 需要显示的位图
     */
    private void showBitmap(Bitmap bitmap) {
        // 需要在主线程中执行UI操作
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.post(() -> {
            try {
                // 缩小图片到原始尺寸的四分之一
                int originalWidth = bitmap.getWidth();
                int originalHeight = bitmap.getHeight();
                int newWidth = originalWidth / 4;
                int newHeight = originalHeight / 4;
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                // 创建自定义布局
                android.widget.LinearLayout layout = new android.widget.LinearLayout(service);
                layout.setOrientation(android.widget.LinearLayout.VERTICAL);
                layout.setBackgroundColor(0xFF000000); // 黑色背景
                layout.setPadding(20, 20, 20, 20);

                // 创建标题
                android.widget.TextView titleView = new android.widget.TextView(service);
                titleView.setText("截图预览");
                titleView.setTextColor(0xFFFFFFFF); // 白色文字
                titleView.setTextSize(18);
                titleView.setGravity(android.view.Gravity.CENTER);
                titleView.setPadding(0, 0, 0, 15);
                layout.addView(titleView);

                // 创建图片视图
                android.widget.ImageView imageView = new android.widget.ImageView(service);
                imageView.setImageBitmap(scaledBitmap);
                imageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
                layout.addView(imageView);

                // 创建按钮容器
                android.widget.LinearLayout buttonLayout = new android.widget.LinearLayout(service);
                buttonLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(android.view.Gravity.CENTER);
                buttonLayout.setPadding(0, 15, 0, 0);

                // 创建关闭按钮
                android.widget.Button closeButton = new android.widget.Button(service);
                closeButton.setText("✕ 关闭");
                closeButton.setTextColor(0xFFFF4444); // 红色文字
                closeButton.setTextSize(16);
                closeButton.setBackgroundColor(0xFF333333); // 深灰色背景
                closeButton.setPadding(30, 15, 30, 15);
                closeButton.setTypeface(null, android.graphics.Typeface.BOLD);
                android.widget.LinearLayout.LayoutParams closeParams = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                closeParams.setMargins(0, 0, 20, 0);
                closeButton.setLayoutParams(closeParams);

                // 创建保存按钮
                android.widget.Button saveButton = new android.widget.Button(service);
                saveButton.setText("✓ 已保存");
                saveButton.setTextColor(0xFF44FF44); // 绿色文字
                saveButton.setTextSize(16);
                saveButton.setBackgroundColor(0xFF333333); // 深灰色背景
                saveButton.setPadding(30, 15, 30, 15);
                saveButton.setTypeface(null, android.graphics.Typeface.BOLD);
                android.widget.LinearLayout.LayoutParams saveParams = new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                );
                saveButton.setLayoutParams(saveParams);

                buttonLayout.addView(closeButton);
                buttonLayout.addView(saveButton);
                layout.addView(buttonLayout);

                // 创建悬浮窗口
                android.view.WindowManager windowManager = (android.view.WindowManager) service.getSystemService(android.content.Context.WINDOW_SERVICE);
                android.view.WindowManager.LayoutParams params = new android.view.WindowManager.LayoutParams(
                        newWidth + 40, // 宽度
                        newHeight + 150, // 高度
                        android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        android.graphics.PixelFormat.TRANSLUCENT
                );

                // 获取屏幕尺寸并设置位置（左下角）
                android.util.DisplayMetrics displayMetrics = service.getResources().getDisplayMetrics();
                int screenHeight = displayMetrics.heightPixels;
                params.gravity = android.view.Gravity.TOP | android.view.Gravity.LEFT;
                params.x = 20; // 左下角X坐标，留20px边距
                params.y = screenHeight - params.height - 50; // 左下角Y坐标，留边距

                // 添加到窗口管理器
                windowManager.addView(layout, params);

                // 设置按钮点击事件
                closeButton.setOnClickListener(v -> {
                    try {
                        windowManager.removeView(layout);
                    } catch (Exception e) {
                        Log.e("【无障碍服务-错误】截图预览", "关闭窗口失败", e);
                    }
                });

                saveButton.setOnClickListener(v -> {
                    try {
                        windowManager.removeView(layout);
                    } catch (Exception e) {
                        Log.e("【无障碍服务-错误】截图预览", "关闭窗口失败", e);
                    }
                });

                // 2秒后自动关闭
                handler.postDelayed(() -> {
                    try {
                        windowManager.removeView(layout);
                    } catch (Exception e) {
                        Log.e("【无障碍服务-错误】截图预览", "自动关闭窗口失败", e);
                    }
                }, 2000);

            } catch (Exception e) {
                Log.e("【无障碍服务-错误】截图预览", "显示截图预览失败", e);
            }
        });
    }


    public void home() {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }


    public void back() {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /**
     * 将Bitmap转换为临时文件
     *
     * @param bitmap  需要转换的位图
     * @param context 上下文
     * @return 转换后的文件，失败时返回null
     */
    private File bitmapToFile(Bitmap bitmap, Context context) {
        try {
            // 创建临时文件
            File tempDir = new File(context.getCacheDir(), "screenshots");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            String filename = "screenshot_" + System.currentTimeMillis() + ".jpg";
            File tempFile = new File(tempDir, filename);

            // 将Bitmap写入文件
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                Log.d("【无障碍服务】文件转换", "Bitmap转换为文件成功: " + tempFile.getAbsolutePath());
                return tempFile;
            }
        } catch (Exception e) {
            Log.e("【无障碍服务-错误】文件转换", "Bitmap转换为文件失败", e);
            return null;
        }
    }


}
