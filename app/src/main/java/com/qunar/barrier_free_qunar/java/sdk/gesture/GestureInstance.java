package com.qunar.barrier_free_qunar.java.sdk.gesture;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
    public Boolean performSwipeGesture(SlidPoint slidPoint) {

        Path dragDownPath = new Path();
        dragDownPath.moveTo(slidPoint.getStart().getX(), slidPoint.getStart().getY());
        dragDownPath.lineTo(slidPoint.getEnd().getY(), slidPoint.getEnd().getY());

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
     * 打开APP
     *
     * @param packageName 安装包名称
     */
    public void openApp(String packageName) {
        Intent intent = service.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            service.startActivity(intent);
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
