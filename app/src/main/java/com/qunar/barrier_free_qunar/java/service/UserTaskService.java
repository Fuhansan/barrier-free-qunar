package com.qunar.barrier_free_qunar.java.service;


import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.util.Log;


import com.qunar.barrier_free_qunar.java.sdk.BarrierFreeBuilder;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BroadcastSender;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.model.MessageRequest;
import com.qunar.barrier_free_qunar.java.sdk.consts.URLConst;
import com.qunar.barrier_free_qunar.java.sdk.gesture.GestureApi;
import com.qunar.barrier_free_qunar.java.sdk.model.BarrierFreeException;
import com.qunar.barrier_free_qunar.java.sdk.model.StreamEventData;
import com.qunar.barrier_free_qunar.java.sdk.model.UserTask;
import com.qunar.barrier_free_qunar.java.sdk.util.BFHttpUtils;
import com.qunar.barrier_free_qunar.java.sdk.util.JsonUtil;
import com.qunar.barrier_free_qunar.java.sdk.util.LlmUtil;
import com.qunar.barrier_free_qunar.java.sdk.util.StreamEventParser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 执行用户命令：一次性只能执行一个，如若要执行下一个命令；必须终止
 */
public class UserTaskService {

    private GestureApi gestureApi;

    private AccessibilityService accessibilityService;

    private BroadcastSender broadcastSender;

    public UserTaskService(AccessibilityService accessibilityService) {
        this.accessibilityService = accessibilityService;
        this.gestureApi = BarrierFreeBuilder.buildGestureApi(this.accessibilityService);
        this.broadcastSender = new BroadcastSender(this.accessibilityService);
    }

    /**
     * 执行
     */
    @SuppressLint("RestrictedApi")
    public void run(UserTask userTask) {
        try {

            userTask.checkArgs();

            // 当前执行步数
            int currentStep = userTask.getCurrentStep();
            // 最大执行步数
            int limitStep = userTask.getLimitStep();

            // 这里先执行一波返回主页
            gestureApi.home();
            // 截个屏幕给模型
            String screenShotUrl = gestureApi.screenShot();
            Log.d("【UserTaskService】", "截图返回URL：" + screenShotUrl + ",执行参数,currentStep:" + currentStep + ", 最大执行步长：" + limitStep);
            String sessionId = UUID.randomUUID().toString().substring(0, 10);
            LlmUtil.LlmRequest llmRequest = LlmUtil.newRequest(sessionId, new String[]{"instruct_extractor"}, true);

            llmRequest.addUserText(userTask.getInstruction())
                    .addAssistantMessage(userTask.getThrough())
                    .addUserImage(screenShotUrl);

            Map<String, String> headers = llmRequest.getHeaders();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            for (; currentStep < 1; currentStep++) {
                // 每次需要重置
                AtomicBoolean isBroad = new AtomicBoolean(false);
                Map<String, Object> requestBody = llmRequest.getRequestBody();


                Log.d("【执行操作指令】", "requestBody：" + JsonUtil.toJson(requestBody));

                // 调用大LLM
                BFHttpUtils.postObjectStream(URLConst.LLM_STREAM_URL, requestBody, headers, new BFHttpUtils.StreamCallback() {
                    private StringBuilder responseBuilder = new StringBuilder();

                    @Override
                    public void onChunk(String chunk) {
                        Log.d("【UserTaskService】", "接收消息：" + chunk);
                        responseBuilder.append(chunk);
                        // 使用StreamEventParser解析数据
                        StreamEventData eventDatum = StreamEventParser.parseChunk(chunk);
                        if (eventDatum != null) {
                            broadcastSender.handleStreamEvent(eventDatum, chunk, sessionId, isBroad);
                        }
                    }

                    @Override
                    public void onComplete() {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(String error) {
                        countDownLatch.countDown();
                    }
                });

                countDownLatch.await();
            }


        } catch (BarrierFreeException bfe) {
            Log.w("【Barrier-Free】执行用户命令", "执行命令失败", bfe);
        } catch (Exception e) {
            Log.d("【Barrier-Free】执行用户命令", "执行命令错误", e);
        }
    }


}
