package com.qunar.barrier_free_qunar.java.sdk.ui;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.qunar.barrier_free_qunar.java.service.UserTaskService;

/**
 * 悬浮按钮服务类
 * 当应用在后台运行时显示悬浮按钮，提供快速操作入口
 */
public class FloatingButtonService {
    
    private static final String TAG = "FloatingButtonService";
    
    private AccessibilityService accessibilityService;
    private WindowManager windowManager;
    private View floatingButton;
    private LinearLayout actionMenu;
    private WindowManager.LayoutParams buttonParams;
    private WindowManager.LayoutParams menuParams;
    private boolean isButtonVisible = false;
    private boolean isMenuVisible = false;
    private Handler handler;
    private UserTaskService userTaskService;
    
    // 按钮拖拽相关
    private float initialX, initialY;
    private float initialTouchX, initialTouchY;
    
    public FloatingButtonService(AccessibilityService service, UserTaskService userTaskService) {
        this.accessibilityService = service;
        this.userTaskService = userTaskService;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        this.handler = new Handler(Looper.getMainLooper());
        
        initFloatingButton();
        initActionMenu();
    }
    
    /**
     * 初始化悬浮按钮
     */
    private void initFloatingButton() {
        // 创建现代化的圆形悬浮按钮
        floatingButton = new LinearLayout(accessibilityService);
        
        // 创建渐变圆形背景
        android.graphics.drawable.GradientDrawable gradientDrawable = new android.graphics.drawable.GradientDrawable();
        gradientDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        
        // 设置渐变色彩（从蓝色到紫色）
        int[] colors = {0xFF2196F3, 0xFF9C27B0}; // 蓝色到紫色渐变
        gradientDrawable.setColors(colors);
        gradientDrawable.setGradientType(android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TL_BR);
        
        // 添加阴影效果（通过描边模拟）
        gradientDrawable.setStroke(dpToPx(2), 0x30000000); // 半透明黑色描边
        
        floatingButton.setBackground(gradientDrawable);
        
        // 创建按钮图标
        ImageView icon = new ImageView(accessibilityService);
        icon.setImageResource(android.R.drawable.ic_menu_manage);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setColorFilter(0xFFFFFFFF); // 白色图标
        
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
            dpToPx(32), dpToPx(32));
        iconParams.gravity = Gravity.CENTER;
        
        ((LinearLayout) floatingButton).addView(icon, iconParams);
        ((LinearLayout) floatingButton).setGravity(Gravity.CENTER);
        
        // 添加轻微的阴影效果（API 21+）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            floatingButton.setElevation(dpToPx(8));
            floatingButton.setTranslationZ(dpToPx(4));
        }
        
        // 设置悬浮窗参数
        buttonParams = new WindowManager.LayoutParams(
            dpToPx(64), dpToPx(64), // 增大尺寸以适应现代设计
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        
        buttonParams.gravity = Gravity.TOP | Gravity.LEFT;
        buttonParams.x = 0; // 紧贴左边缘
        buttonParams.y = dpToPx(100);
        
        // 设置触摸监听
        floatingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = buttonParams.x;
                        initialY = buttonParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        // 计算新位置
                        int newX = (int) (initialX + (event.getRawX() - initialTouchX));
                        int newY = (int) (initialY + (event.getRawY() - initialTouchY));
                        
                        // 边界检查
                        int screenWidth = accessibilityService.getResources().getDisplayMetrics().widthPixels;
                        int screenHeight = accessibilityService.getResources().getDisplayMetrics().heightPixels;
                        int buttonWidth = dpToPx(2);
                        int buttonHeight = dpToPx(2);
                        
                        // 限制X轴范围
                        newX = Math.max(0, Math.min(newX, screenWidth - buttonWidth));
                        // 限制Y轴范围（留出状态栏和导航栏空间）
                        newY = Math.max(dpToPx(2), Math.min(newY, screenHeight - buttonHeight - dpToPx(2)));
                        
                        buttonParams.x = newX;
                        buttonParams.y = newY;
                        windowManager.updateViewLayout(floatingButton, buttonParams);
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        // 判断是点击还是拖拽
                        float deltaX = Math.abs(event.getRawX() - initialTouchX);
                        float deltaY = Math.abs(event.getRawY() - initialTouchY);
                        
                        if (deltaX < dpToPx(5) && deltaY < dpToPx(5)) {
                            // 点击事件
                            onFloatingButtonClick();
                        } else {
                            // 拖拽结束，执行自动贴边动画
                            autoSnapToEdge();
                        }
                        return true;
                }
                return false;
            }
        });
    }
    
    /**
     * 初始化操作菜单
     */
    private void initActionMenu() {
        actionMenu = new LinearLayout(accessibilityService);
        actionMenu.setOrientation(LinearLayout.VERTICAL);
        
        // 创建现代化的卡片式背景
        android.graphics.drawable.GradientDrawable cardBackground = new android.graphics.drawable.GradientDrawable();
        cardBackground.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        cardBackground.setColor(0xFFFFFFFF); // 白色背景
        cardBackground.setCornerRadius(dpToPx(16)); // 圆角
        cardBackground.setStroke(dpToPx(1), 0x1F000000); // 轻微边框
        
        actionMenu.setBackground(cardBackground);
        actionMenu.setPadding(dpToPx(16), dpToPx(20), dpToPx(16), dpToPx(20));
        
        // 添加标题
        android.widget.TextView titleView = new android.widget.TextView(accessibilityService);
        titleView.setText("快捷操作");
        titleView.setTextSize(18);
        titleView.setTextColor(0xFF333333);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 0, 0, dpToPx(16));
        actionMenu.addView(titleView);
        
        // 创建现代化的菜单按钮
        LinearLayout openAppButton = createMenuButton("🚀 打开应用", android.R.drawable.ic_menu_view);
        openAppButton.setOnClickListener(v -> {
            openMainApp();
            hideActionMenu();
        });
        
        // 继续执行按钮
        LinearLayout continueButton = createMenuButton("▶️ 继续执行", android.R.drawable.ic_media_play);
        continueButton.setOnClickListener(v -> {
            continueTask();
            hideActionMenu();
        });
        
        // 关闭菜单按钮
        LinearLayout closeButton = createMenuButton("❌ 关闭菜单", android.R.drawable.ic_menu_close_clear_cancel);
        closeButton.setOnClickListener(v -> hideActionMenu());
        
        actionMenu.addView(openAppButton);
        
        // 添加分隔线
        View divider = new View(accessibilityService);
        divider.setBackgroundColor(0x1F000000);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
        dividerParams.setMargins(0, dpToPx(8), 0, dpToPx(8));
        actionMenu.addView(divider, dividerParams);
        
        actionMenu.addView(continueButton);
        actionMenu.addView(closeButton);
        
        // 设置菜单悬浮窗参数
        menuParams = new WindowManager.LayoutParams(
            dpToPx(200), WindowManager.LayoutParams.WRAP_CONTENT, // 增加宽度以适应现代设计
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        
        menuParams.gravity = Gravity.TOP | Gravity.LEFT;
        
        // 添加阴影效果（API 21+）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            actionMenu.setElevation(dpToPx(12));
            actionMenu.setTranslationZ(dpToPx(6));
        }
    }
    
    /**
     * 创建现代化菜单按钮
     */
    @SuppressLint("ClickableViewAccessibility")
    private LinearLayout createMenuButton(String text, int iconRes) {
        LinearLayout button = new LinearLayout(accessibilityService);
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));
        
        // 创建现代化的按钮背景
        android.graphics.drawable.GradientDrawable buttonBackground = new android.graphics.drawable.GradientDrawable();
        buttonBackground.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        buttonBackground.setColor(0xFFF8F9FA); // 浅灰色背景
        buttonBackground.setCornerRadius(dpToPx(12)); // 圆角
        buttonBackground.setStroke(dpToPx(1), 0x1A000000); // 轻微边框
        
        button.setBackground(buttonBackground);
        
        // 创建图标（如果需要的话）
        if (iconRes != 0) {
            ImageView icon = new ImageView(accessibilityService);
            icon.setImageResource(iconRes);
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            icon.setColorFilter(0xFF666666); // 灰色图标
            
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                dpToPx(20), dpToPx(20));
            iconParams.gravity = Gravity.CENTER_VERTICAL;
            iconParams.setMargins(0, 0, dpToPx(12), 0);
            
            button.addView(icon, iconParams);
        }
        
        // 创建文本
        android.widget.TextView textView = new android.widget.TextView(accessibilityService);
        textView.setText(text);
        textView.setTextSize(16);
        textView.setTextColor(0xFF333333);
        textView.setTypeface(null, android.graphics.Typeface.NORMAL);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        textParams.gravity = Gravity.CENTER_VERTICAL;
        
        button.addView(textView, textParams);
        
        // 添加点击效果和触摸反馈
        button.setClickable(true);
        button.setFocusable(true);
        
        // 添加触摸反馈效果
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    // 按下时变暗
                    buttonBackground.setColor(0xFFE9ECEF);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    // 松开时恢复
                    buttonBackground.setColor(0xFFF8F9FA);
                    break;
            }
            return false; // 继续传递事件
        });
        
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(48));
        buttonParams.setMargins(0, dpToPx(4), 0, dpToPx(4));
        button.setLayoutParams(buttonParams);
        
        return button;
    }
    
    /**
     * 悬浮按钮点击事件
     */
    private void onFloatingButtonClick() {
        if (isMenuVisible) {
            hideActionMenu();
        } else {
            showActionMenu();
        }
    }
    
    /**
     * 显示操作菜单
     */
    private void showActionMenu() {
        if (!isMenuVisible) {
            try {
                // 设置菜单位置（在按钮旁边）
                menuParams.x = buttonParams.x + dpToPx(60);
                menuParams.y = buttonParams.y;
                
                windowManager.addView(actionMenu, menuParams);
                isMenuVisible = true;
                
                // 3秒后自动隐藏菜单
                handler.postDelayed(this::hideActionMenu, 3000);
                
                Log.d(TAG, "显示操作菜单");
            } catch (Exception e) {
                Log.e(TAG, "显示操作菜单失败", e);
            }
        }
    }
    
    /**
     * 隐藏操作菜单
     */
    private void hideActionMenu() {
        if (isMenuVisible) {
            try {
                windowManager.removeView(actionMenu);
                isMenuVisible = false;
                Log.d(TAG, "隐藏操作菜单");
            } catch (Exception e) {
                Log.e(TAG, "隐藏操作菜单失败", e);
            }
        }
    }
    
    /**
     * 打开主应用
     */
    private void openMainApp() {
        try {
            Intent intent = accessibilityService.getPackageManager()
                .getLaunchIntentForPackage(accessibilityService.getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                accessibilityService.startActivity(intent);
                Log.d(TAG, "打开主应用");
            }
        } catch (Exception e) {
            Log.e(TAG, "打开主应用失败", e);
            Toast.makeText(accessibilityService, "打开应用失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 继续执行任务
     */
    private void continueTask() {
        try {
            if (userTaskService != null) {
                // 这里可以添加继续执行任务的逻辑
                // 比如恢复之前暂停的任务或执行预设的操作
                Log.d(TAG, "继续执行任务");
                Toast.makeText(accessibilityService, "继续执行任务", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "继续执行任务失败", e);
        }
    }
    
    /**
     * 显示悬浮按钮
     */
    public void showFloatingButton() {
        Log.d(TAG, "尝试显示悬浮按钮 - isButtonVisible: " + isButtonVisible + ", windowManager: " + (windowManager != null));
        
        if (isButtonVisible) {
            Log.d(TAG, "悬浮按钮已经在显示中");
            return;
        }
        
        if (windowManager == null) {
            Log.e(TAG, "WindowManager为空，无法显示悬浮按钮");
            return;
        }
        
        try {
            windowManager.addView(floatingButton, buttonParams);
            isButtonVisible = true;
            Log.i(TAG, "悬浮按钮显示成功");
        } catch (Exception e) {
            Log.e(TAG, "显示悬浮按钮失败", e);
            isButtonVisible = false;
        }
    }
    
    /**
     * 隐藏悬浮按钮
     */
    public void hideFloatingButton() {
        if (isButtonVisible) {
            try {
                windowManager.removeView(floatingButton);
                isButtonVisible = false;
                hideActionMenu(); // 同时隐藏菜单
                Log.d(TAG, "隐藏悬浮按钮");
            } catch (Exception e) {
                Log.e(TAG, "隐藏悬浮按钮失败", e);
            }
        }
    }
    
    /**
     * 检查悬浮按钮是否可见
     */
    public boolean isFloatingButtonVisible() {
        return isButtonVisible;
    }
    
    /**
     * 清理资源
     */
    public void destroy() {
        hideFloatingButton();
        handler.removeCallbacksAndMessages(null);
    }
    
    /**
     * dp转px
     */
    private int dpToPx(int dp) {
        float density = accessibilityService.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
    
    /**
      * 自动贴边动画
      * 当悬浮按钮拖拽结束后，自动移动到最近的左右边缘
      */
     private void autoSnapToEdge() {
         if (!isButtonVisible || windowManager == null) {
             return;
         }
         
         // 获取屏幕尺寸
         int screenWidth = accessibilityService.getResources().getDisplayMetrics().widthPixels;
         int screenHeight = accessibilityService.getResources().getDisplayMetrics().heightPixels;
         int buttonWidth = dpToPx(50);
         int buttonHeight = dpToPx(50);
         
         // 计算当前按钮中心位置
         int currentCenterX = buttonParams.x + buttonWidth / 2;
         
         // 判断应该贴到左边还是右边
         int targetX;
         if (currentCenterX < screenWidth / 2) {
             // 贴到左边，紧贴边缘
             targetX = 0;
         } else {
             // 贴到右边，紧贴边缘
             targetX = screenWidth - buttonWidth;
         }
         
         // 确保Y坐标在合理范围内
         int targetY = buttonParams.y;
         int minY = dpToPx(50); // 状态栏下方
         int maxY = screenHeight - buttonHeight - dpToPx(100); // 导航栏上方
         targetY = Math.max(minY, Math.min(targetY, maxY));
         
         // 执行动画
         animateToPosition(targetX, targetY);
         
         Log.d(TAG, "自动贴边：从 (" + buttonParams.x + ", " + buttonParams.y + ") 移动到 (" + targetX + ", " + targetY + ")");
     }
    
    /**
     * 动画移动到指定位置
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     */
    private void animateToPosition(int targetX, int targetY) {
        final int startX = buttonParams.x;
        final int startY = buttonParams.y;
        final int deltaX = targetX - startX;
        final int deltaY = targetY - startY;
        
        // 动画持续时间（毫秒）
        final int animationDuration = 300;
        final int frameRate = 16; // 约60fps
        final int totalFrames = animationDuration / frameRate;
        
        // 使用Handler执行动画
        for (int i = 0; i <= totalFrames; i++) {
            final int frame = i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isButtonVisible) {
                        return;
                    }
                    
                    // 计算当前帧的位置（使用缓动函数）
                    float progress = (float) frame / totalFrames;
                    // 使用ease-out缓动效果
                    float easedProgress = 1 - (1 - progress) * (1 - progress);
                    
                    int currentX = (int) (startX + deltaX * easedProgress);
                    int currentY = (int) (startY + deltaY * easedProgress);
                    
                    // 更新按钮位置
                    buttonParams.x = currentX;
                    buttonParams.y = currentY;
                    
                    try {
                        windowManager.updateViewLayout(floatingButton, buttonParams);
                    } catch (Exception e) {
                        Log.e(TAG, "更新悬浮按钮位置失败", e);
                    }
                }
            }, frame * frameRate);
        }
    }
}