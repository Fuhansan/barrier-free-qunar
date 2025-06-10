package com.qunar.barrier_free_qunar.java.sdk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qunar.barrier_free_qunar.R;
import com.qunar.barrier_free_qunar.java.GlobalAccessibilityService;

/**
 * 悬浮按钮测试Activity
 * 用于调试和测试悬浮按钮功能
 */
public class FloatingButtonTestActivity extends Activity {
    private static final String TAG = "FloatingButtonTestActivity";
    private TextView statusText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建简单的测试界面
        createTestLayout();
        updateStatus();
    }
    
    private int createTestLayout() {
        // 创建现代化的Material Design风格界面
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.setBackgroundColor(0xFFF5F5F5); // 浅灰背景
        
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));
        
        // 标题卡片
        android.widget.LinearLayout titleCard = createCard();
        titleCard.setBackgroundColor(0xFF2196F3); // Material Blue
        
        TextView titleText = new TextView(this);
        titleText.setText("🚀 悬浮按钮测试工具");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(10));
        titleCard.addView(titleText);
        
        TextView subtitleText = new TextView(this);
        subtitleText.setText("专业的悬浮窗功能调试平台");
        subtitleText.setTextSize(14);
        subtitleText.setTextColor(0xFFE3F2FD);
        subtitleText.setPadding(dpToPx(20), 0, dpToPx(20), dpToPx(20));
        titleCard.addView(subtitleText);
        
        mainLayout.addView(titleCard);
        
        // 状态信息卡片
        android.widget.LinearLayout statusCard = createCard();
        statusText = new TextView(this);
        statusText.setId(View.generateViewId());
        statusText.setText("悬浮按钮测试工具");
        statusText.setTextSize(16);
        statusText.setTextColor(0xFF333333);
        statusText.setLineSpacing(dpToPx(4), 1.0f);
        statusText.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));
        statusCard.addView(statusText);
        mainLayout.addView(statusCard);
        
        // 权限管理卡片
        android.widget.LinearLayout permissionCard = createCard();
        TextView permissionTitle = new TextView(this);
        permissionTitle.setText("🔐 权限管理");
        permissionTitle.setTextSize(18);
        permissionTitle.setTextColor(0xFF333333);
        permissionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        permissionTitle.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(10));
        permissionCard.addView(permissionTitle);
        
        // 检查权限按钮
        Button checkPermissionBtn = createModernButton("检查悬浮窗权限", 0xFF4CAF50, "🔍");
        checkPermissionBtn.setOnClickListener(v -> checkOverlayPermission());
        permissionCard.addView(checkPermissionBtn);
        
        // 请求权限按钮
        Button requestPermissionBtn = createModernButton("请求悬浮窗权限", 0xFFFF9800, "⚙️");
        requestPermissionBtn.setOnClickListener(v -> requestOverlayPermission());
        permissionCard.addView(requestPermissionBtn);
        
        mainLayout.addView(permissionCard);
        
        // 功能测试卡片
        android.widget.LinearLayout testCard = createCard();
        TextView testTitle = new TextView(this);
        testTitle.setText("🧪 功能测试");
        testTitle.setTextSize(18);
        testTitle.setTextColor(0xFF333333);
        testTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        testTitle.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(10));
        testCard.addView(testTitle);
        
        // 测试悬浮按钮按钮
        Button testFloatingBtn = createModernButton("强制显示悬浮按钮", 0xFF9C27B0, "🎯");
        testFloatingBtn.setOnClickListener(v -> testFloatingButton());
        testCard.addView(testFloatingBtn);
        
        // 检查应用状态按钮
        Button checkStateBtn = createModernButton("检查应用状态", 0xFF607D8B, "📊");
        checkStateBtn.setOnClickListener(v -> checkAppState());
        testCard.addView(checkStateBtn);
        
        // 返回桌面按钮
        Button goHomeBtn = createModernButton("返回桌面测试", 0xFFF44336, "🏠");
        goHomeBtn.setOnClickListener(v -> goToHome());
        testCard.addView(goHomeBtn);
        
        mainLayout.addView(testCard);
        
        // 添加底部间距
        View bottomSpace = new View(this);
        bottomSpace.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(20)));
        mainLayout.addView(bottomSpace);
        
        scrollView.addView(mainLayout);
        setContentView(scrollView);
        return 0;
    }
    
    /**
     * 创建卡片布局
     */
    private android.widget.LinearLayout createCard() {
        android.widget.LinearLayout card = new android.widget.LinearLayout(this);
        card.setOrientation(android.widget.LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        
        // 设置卡片阴影效果（通过边框模拟）
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(0xFFFFFFFF);
        drawable.setCornerRadius(dpToPx(12));
        drawable.setStroke(1, 0xFFE0E0E0);
        card.setBackground(drawable);
        
        // 设置卡片间距
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dpToPx(16));
        card.setLayoutParams(params);
        
        // 添加轻微的阴影效果
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(dpToPx(4));
        }
        
        return card;
    }
    
    /**
     * 创建现代化按钮
     */
    private Button createModernButton(String text, int color, String emoji) {
        Button button = new Button(this);
        button.setText(emoji + " " + text);
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(16);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // 创建圆角背景
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dpToPx(25)); // 圆角按钮
        button.setBackground(drawable);
        
        // 设置按钮布局参数
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(50));
        params.setMargins(dpToPx(20), dpToPx(8), dpToPx(20), dpToPx(8));
        button.setLayoutParams(params);
        
        // 添加点击效果
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.7f);
                    v.setScaleX(0.95f);
                    v.setScaleY(0.95f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1.0f);
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                        v.performClick();
                    }
                    break;
            }
            return true;
        });
        
        return button;
    }
    
    /**
     * dp转px
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
    
    private void checkOverlayPermission() {
        boolean hasPermission = FloatingButtonDebugHelper.checkOverlayPermission(this);
        String message = hasPermission ? "已有悬浮窗权限" : "缺少悬浮窗权限";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        updateStatus();
    }
    
    private void requestOverlayPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "已有悬浮窗权限", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void testFloatingButton() {
        // 这里需要获取GlobalAccessibilityService实例
        // 由于无法直接获取，我们通过日志提示用户
        Log.d(TAG, "用户点击测试悬浮按钮");
        Toast.makeText(this, "请查看日志输出，或者返回桌面测试", Toast.LENGTH_LONG).show();
    }
    
    private void checkAppState() {
        GlobalAccessibilityService service = GlobalAccessibilityService.getInstance();
        if (service != null) {
            service.checkAppState();
            updateStatus();
        } else {
            updateStatus();
        }
    }
    
    private void goToHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(this, "已返回桌面，请观察悬浮按钮是否显示", Toast.LENGTH_LONG).show();
    }
    
    private void updateStatus() {
        boolean hasPermission = FloatingButtonDebugHelper.checkOverlayPermission(this);
        String status = "悬浮按钮测试工具\n\n";
        status += "悬浮窗权限: " + (hasPermission ? "✓ 已授权" : "✗ 未授权") + "\n";
        status += "\n使用说明:\n";
        status += "1. 首先检查并请求悬浮窗权限\n";
        status += "2. 点击'返回桌面测试'按钮\n";
        status += "3. 观察是否显示悬浮按钮\n";
        status += "4. 查看logcat日志获取详细信息";
        
        if (statusText != null) {
            statusText.setText(status);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}