package com.qunar.barrier_free_qunar.java.sdk.broadcast;

import android.content.Context;
import android.util.Log;

import com.qunar.barrier_free_qunar.LogCollector;
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.MessageRequest;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.sender.MessageSender;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.sender.StreamingMessageSender;
import com.qunar.barrier_free_qunar.java.sdk.broadcast.sender.ShortConnectionMessageSender;
import com.qunar.barrier_free_qunar.java.sdk.model.StreamEventData;
import com.qunar.barrier_free_qunar.java.sdk.util.StreamEventParser;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 广播发送器 - 重构后的统一消息发送入口
 * 使用多态设计，根据发送模式选择具体的发送策略
 */
public class BroadcastSender {
    private static final String TAG = "BroadcastSender";
    private static final int QUEUE_CAPACITY = 1000; // 消息队列容量
    private static final int CONSUMER_THREAD_COUNT = 2; // 消费者线程数量
    
    private final BroadcastManager broadcastManager;
    private final StreamingMessageSender streamingSender;
    private final ShortConnectionMessageSender shortConnectionSender;

    private LogCollector logCollector;
    
    // 消息队列管理
    private final Map<String, BlockingQueue<MessageRequest>> broadcastQueues = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> queueConsumers = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> queueActiveFlags = new ConcurrentHashMap<>();
    private final Object queueLock = new Object();


    public BroadcastSender(Context context) {
        this.broadcastManager = BroadcastManager.getInstance(context);

        // 初始化发送器
        this.streamingSender = new StreamingMessageSender();
        this.streamingSender.setBroadcastManager(broadcastManager);

        this.shortConnectionSender = new ShortConnectionMessageSender();
        this.logCollector = LogCollector.Companion.getInstance();
        this.shortConnectionSender.setBroadcastManager(broadcastManager);
    }

    /**
     * 统一的消息发送入口（新架构）
     * 根据MessageRequest中的发送模式选择对应的发送策略
     *
     * @param request 消息发送请求
     * @return 是否发送成功
     */
    public boolean send(MessageRequest request) {
        if (request == null) {
            Log.e(TAG, "消息请求不能为空");
            return false;
        }

        if (request.getSendMode() == null) {
            Log.e(TAG, "发送模式不能为空");
            return false;
        }

        try {
            // 对于流式消息，使用消息队列机制
            if (request.getSendMode() == MessageRequest.SendMode.STREAMING && 
                request.getConversationId() != null && !request.getConversationId().trim().isEmpty()) {
                
                String broadcastId = request.getConversationId();
                
                // 将消息放入对应的队列
                return enqueueMessage(broadcastId, request);
            }
            
            // 根据发送模式选择对应的发送器
            MessageSender sender = getSenderByMode(request.getSendMode());

            Log.d(TAG, "使用" + sender.getSenderType() + "发送消息");

            // 执行发送
            boolean success = sender.send(request);

            if (success) {
                Log.d(TAG, "消息发送成功 - 模式: " + request.getSendMode() +
                        ", 类型: " + request.getMessageType() +
                        ", 角色: " + request.getSenderRole());
            } else {
                Log.w(TAG, "消息发送失败 - 模式: " + request.getSendMode() +
                        ", 类型: " + request.getMessageType() +
                        ", 角色: " + request.getSenderRole());
            }

            return success;
        } catch (Exception e) {
            Log.e(TAG, "发送消息异常", e);
            return false;
        }
    }

    /**
     * 根据发送模式获取对应的发送器
     */
    private MessageSender getSenderByMode(MessageRequest.SendMode sendMode) {
        switch (sendMode) {
            case STREAMING:
                return streamingSender;
            case SHORT_CONNECTION:
                return shortConnectionSender;
            default:
                throw new IllegalArgumentException("不支持的发送模式: " + sendMode);
        }
    }

    /**
     * 处理流式事件数据
     * 配合新的状态机解析器处理SSE事件流
     *
     * @param eventData   解析后的事件数据
     * @param userMessage 用户原始消息
     */
    public String handleStreamEvent(StreamEventData eventData, String userMessage, String currentSessionId, AtomicBoolean isBroadcastingStarted) {
        String result = "";
        try {
            logCollector.d(TAG, "处理流式事件: " + eventData.getEventType() + ", 解析器状态: " + StreamEventParser.getCurrentState());
            switch (eventData.getEventType()) {
                case START_OF_LLM:
                    logCollector.i(TAG, "LLM流程开始");
                    // 重置广播状态
                    isBroadcastingStarted.set(false);
                    break;

                case AGENT_NAME:
                    String agentName = eventData.getAgentName();
                    StreamEventData.AgentType agentType = eventData.getAgentType();
                    logCollector.i(TAG, "检测到代理: " + agentName + ", 类型: " + agentType);

                    // 如果是需要广播的代理类型，准备开始广播
                    if (eventData.isBroadcastAgent()) {
                        logCollector.i(TAG, "代理 " + agentName + " 需要广播，准备开始广播模式");
                        // 可以在这里显示"正在思考..."等状态提示
                    } else {
                        logCollector.d(TAG, "代理 " + agentName + " 不需要广播");
                    }
                    break;

                case MESSAGE:
                    // 根据解析器状态决定是否开始广播
                    if (eventData.isShouldStartBroadcast()) {
                        isBroadcastingStarted.set(true);
                        logCollector.i(TAG, "接收到message事件，开始广播模式");
                    } else {
                        logCollector.d(TAG, "接收到message事件，但不满足广播条件" + userMessage);
                    }
                    break;

                case DELTA:
                    // 只有在广播已开始的情况下才处理delta内容
                    if (isBroadcastingStarted.get() && eventData.getContent() != null && !eventData.getContent().trim().isEmpty()) {
                        String conversationId = eventData.getMessageId();

                        if(conversationId == null || conversationId.isBlank()){
                            logCollector.d(TAG, "接收到delta事件，但是messageId为空，跳过处理: " + eventData.getContent());
                            break;
                        }

                        // 额外验证内容不为空
                        String content = eventData.getContent().trim();
                        if (content.isEmpty()) {
                            logCollector.d(TAG, "接收到delta事件，但是内容为空，跳过处理: messageId=" + conversationId);
                            break;
                        }

                        MessageRequest streamRequest = new MessageRequest.Builder()
                                .broadCastKey(MessageRequest.BroadCastListenerKey.CHAT_MESSAGE_BROAD_CAST)
                                .sendMode(MessageRequest.SendMode.STREAMING)
                                .messageType(MessageRequest.MessageType.TEXT)
                                .senderRole(MessageRequest.SenderRole.SYSTEM)
                                .content(content)
                                .originalMessage(userMessage)
                                .conversationId(conversationId)
                                .build();

                        result = content;
                        send(streamRequest);
                        logCollector.d(TAG, "发送流式消息内容: " + content + ",messageId=" + conversationId);
                    } else if (!isBroadcastingStarted.get()) {
                        logCollector.d(TAG, "广播未开始，跳过delta内容: " + eventData.getContent());
                    } else {
                        logCollector.d(TAG, "delta内容为空或无效，跳过处理: " + eventData.getContent());
                    }
                    break;

                case END_OF_LLM:
                    logCollector.i(TAG, "LLM流程结束，停止广播模式");
                    isBroadcastingStarted.set(false);
                    // 停止当前会话的消息队列消费
                    if (currentSessionId != null && !currentSessionId.trim().isEmpty()) {
                        stopQueueConsumer(currentSessionId);
                    }
                    // 可以在这里发送流程结束的通知
                    break;

                case UNKNOWN:
                default:
                    logCollector.w(TAG, "未知或未处理的事件类型: " + eventData.getEventType());
                    break;
            }
        } catch (Exception e) {
            logCollector.e(TAG, "处理流式事件时出错: " + e.getMessage(), e);
        }

        return result;
    }
    
    /**
     * 将消息放入队列
     * @param broadcastId 广播唯一标识
     * @param request 消息请求
     * @return 是否成功入队
     */
    private boolean enqueueMessage(String broadcastId, MessageRequest request) {
        synchronized (queueLock) {
            // 获取或创建消息队列
            BlockingQueue<MessageRequest> queue = broadcastQueues.computeIfAbsent(
                broadcastId, 
                k -> new LinkedBlockingQueue<>(QUEUE_CAPACITY)
            );
            
            // 启动消费者（如果还没有启动）
            if (!queueActiveFlags.containsKey(broadcastId)) {
                startQueueConsumer(broadcastId, queue);
            }
            
            try {
                // 非阻塞入队，如果队列满了则返回false
                boolean success = queue.offer(request);
                if (!success) {
                    logCollector.w(TAG, "消息队列已满，丢弃消息: " + broadcastId);
                }
                return success;
            } catch (Exception e) {
                logCollector.e(TAG, "消息入队失败: " + broadcastId, e);
                return false;
            }
        }
    }
    
    /**
     * 启动队列消费者
     * @param broadcastId 广播唯一标识
     * @param queue 消息队列
     */
    private void startQueueConsumer(String broadcastId, BlockingQueue<MessageRequest> queue) {
        ExecutorService consumer = Executors.newFixedThreadPool(CONSUMER_THREAD_COUNT, r -> {
            Thread t = new Thread(r, "BroadcastConsumer-" + broadcastId);
            t.setDaemon(true);
            return t;
        });
        
        queueConsumers.put(broadcastId, consumer);
        queueActiveFlags.put(broadcastId, new AtomicBoolean(true));
        
        // 启动消费者线程
        for (int i = 0; i < CONSUMER_THREAD_COUNT; i++) {
            final int consumerId = i;
            consumer.submit(() -> {
                logCollector.d(TAG, "启动消费者线程: " + broadcastId + "-" + consumerId);
                
                while (queueActiveFlags.get(broadcastId).get()) {
                    try {
                        // 从队列中取消息，设置超时避免无限等待
                        MessageRequest request = queue.poll(1, TimeUnit.SECONDS);
                        
                        if (request != null) {
                            // 处理消息
                            processQueuedMessage(request, broadcastId, consumerId);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logCollector.d(TAG, "消费者线程被中断: " + broadcastId + "-" + consumerId);
                        break;
                    } catch (Exception e) {
                        logCollector.e(TAG, "消费者处理消息异常: " + broadcastId + "-" + consumerId, e);
                    }
                }
                
                logCollector.d(TAG, "消费者线程结束: " + broadcastId + "-" + consumerId);
            });
        }
        
        logCollector.i(TAG, "队列消费者已启动: " + broadcastId + ", 线程数: " + CONSUMER_THREAD_COUNT);
    }
    
    /**
     * 处理队列中的消息
     * @param request 消息请求
     * @param broadcastId 广播标识
     * @param consumerId 消费者ID
     */
    private void processQueuedMessage(MessageRequest request, String broadcastId, int consumerId) {
        try {
            logCollector.d(TAG, "消费者 " + consumerId + " 处理消息: " + broadcastId);
            
            // 使用原有的发送逻辑
            MessageSender sender = getSenderByMode(request.getSendMode());
            boolean success = sender.send(request);
            
            if (success) {
                logCollector.d(TAG, "队列消息发送成功: " + broadcastId + " by consumer-" + consumerId);
            } else {
                logCollector.w(TAG, "队列消息发送失败: " + broadcastId + " by consumer-" + consumerId);
            }
        } catch (Exception e) {
            logCollector.e(TAG, "处理队列消息异常: " + broadcastId + " by consumer-" + consumerId, e);
        }
    }
    
    /**
     * 停止队列消费者
     * @param broadcastId 广播唯一标识
     */
    private void stopQueueConsumer(String broadcastId) {
        synchronized (queueLock) {
            AtomicBoolean activeFlag = queueActiveFlags.get(broadcastId);
            if (activeFlag != null) {
                activeFlag.set(false);
            }
            
            ExecutorService consumer = queueConsumers.remove(broadcastId);
            if (consumer != null) {
                consumer.shutdown();
                try {
                    if (!consumer.awaitTermination(5, TimeUnit.SECONDS)) {
                        consumer.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    consumer.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                logCollector.i(TAG, "队列消费者已停止: " + broadcastId);
            }
            
            // 清理队列（可选，根据需求决定是否保留未处理的消息）
            BlockingQueue<MessageRequest> queue = broadcastQueues.remove(broadcastId);
            if (queue != null && !queue.isEmpty()) {
                logCollector.w(TAG, "清理队列，丢弃 " + queue.size() + " 条未处理消息: " + broadcastId);
            }
            
            queueActiveFlags.remove(broadcastId);
        }
    }
    
    /**
     * 强制清理所有队列状态（用于异常情况）
     */
    public void clearAllQueueStates() {
        synchronized (queueLock) {
            // 停止所有消费者
            for (String broadcastId : queueConsumers.keySet()) {
                stopQueueConsumer(broadcastId);
            }
            
            broadcastQueues.clear();
            queueConsumers.clear();
            queueActiveFlags.clear();
            logCollector.i(TAG, "已清理所有队列状态");
        }
    }

}