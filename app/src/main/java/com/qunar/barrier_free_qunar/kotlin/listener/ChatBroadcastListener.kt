package com.qunar.barrier_free_qunar.kotlin.listener

import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.qunar.barrier_free_qunar.ChatAdapter
import com.qunar.barrier_free_qunar.Message
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BroadcastListener
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.MessageData
import com.qunar.barrier_free_qunar.java.sdk.consts.BroadcastConst

/**
 * 聊天广播监听器
 * 负责处理聊天相关的广播消息
 */
class ChatBroadcastListener(
    private val chatAdapter: ChatAdapter,
    private val rvMessages: RecyclerView,
    private val messages: MutableList<Message>,
    private val processedMessages: MutableSet<String>,
    private val processedStreamChunks: MutableMap<String, StringBuilder>,
    private val onUIUpdate: () -> Unit
) : BroadcastListener {
    
    // 用于跟踪当前流式消息的对话ID和消息索引
    private var currentStreamConversationId: String? = null
    private var currentStreamMessageIndex: Int = -1



    
    override fun onMessageReceived(messageData: MessageData) {
        // 检查消息是否为空或者是多媒体消息
        if (!messageData.reply.isNullOrEmpty() || messageData.hasMultimediaContent()) {
            // 消息去重检查
            val messageKey = generateMessageKey(messageData)
            if (processedMessages.contains(messageKey)) {
                return
            }

            // 标记消息为已处理
            processedMessages.add(messageKey)

            removeThinkingMessage()
            // 在主线程中更新UI
            onUIUpdate.invoke()
            
            val conversationId = messageData.conversationId ?: ""
            val content = messageData.reply ?: ""

            // 如果有conversationId，尝试追加到现有消息
            if (conversationId.isNotEmpty() && content.isNotEmpty()) {
                val appended = chatAdapter.appendToMessageByConversationId(
                    conversationId,
                    content
                )
                if (appended) {
                    // 滚动到最新消息
                    rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
                    return
                }
            }

            // 如果没有找到现有消息，创建新消息
            displayMessage(
                messageData.originalMessage ?: "",
                messageData.reply ?: "",
                messageData.senderName ?: "未知",
                messageData.senderId ?: "unknown",
                messageData.timestamp,
                messageData.conversationId ?: "",
                messageData.messageType ?: BroadcastConst.MessageType.TEXT,
                messageData.imageData,
                messageData.videoData,
                messageData.audioData,
                messageData.multimediaType,
                messageData.extras
            )
        } else {
            Log.w("【ChatBroadcastListener】广播", "接收到空消息，忽略处理")
        }
    }

    override fun onError(intent: Intent?, error: Exception) {
        Log.e("【ChatBroadcastListener】错误", "处理广播消息时出错: ${error.message}")
        error.printStackTrace()
    }

    override fun primaryKey(): String? {
        return "CHAT_MESSAGE_BROAD_CAST";
    }

    /**
     * 生成消息唯一标识符用于去重
     */
    private fun generateMessageKey(messageData: MessageData): String {
        return "${messageData.senderId}_${messageData.timestamp}_${messageData.reply?.hashCode()}_${messageData.conversationId}"
    }
    
    /**
     * 移除思考消息
     */
    private fun removeThinkingMessage() {
        val thinkingIndex = messages.indexOfFirst { message ->
            message.senderId == "system_thinking" && message.content == "正在思考..."
        }

        Log.d("移除thinking", "索引：" + thinkingIndex)

        if (thinkingIndex != -1) {
            messages.removeAt(thinkingIndex)
            chatAdapter.notifyItemRemoved(thinkingIndex)
        }
    }
    
    /**
     * 显示接收到的消息
     */
    private fun displayMessage(
        originalMessage: String, 
        reply: String, 
        senderName: String, 
        senderId: String, 
        timestamp: Long, 
        conversationId: String, 
        messageType: String,
        imageData: String? = null,
        videoData: String? = null,
        audioData: String? = null,
        multimediaType: String? = null,
        extras: Map<String, Any>? = null
    ) {
        try {
            // 根据消息类型处理不同的显示逻辑
            when (messageType) {
                BroadcastConst.MessageType.TEXT -> {
                    // 普通文本消息
                    val message = Message.createReceivedMessage(
                        content = reply,
                        senderName = senderName,
                        senderId = senderId,
                        conversationId = conversationId
                    )
                    chatAdapter.addMessage(message)
                }
                BroadcastConst.MessageType.STREAM_CHUNK -> {
                    // 流式消息块，使用conversationId来区分不同的对话
                    handleStreamChunk(reply, senderName, senderId, conversationId)
                }
                BroadcastConst.MessageType.ERROR -> {
                    // 错误消息，可以用不同的样式显示
                    val errorMessage = Message.createReceivedMessage(
                        content = reply,
                        senderName = senderName,
                        senderId = senderId
                    )
                    chatAdapter.addMessage(errorMessage)
                }
                BroadcastConst.MessageType.SYSTEM -> {
                    // 系统消息
                    if (reply == "[流式响应完成]") {
                        // 流式响应完成，重置流式对话状态
                        if (currentStreamMessageIndex >= 0) {
                            chatAdapter.markStreamCompleted(currentStreamMessageIndex)
                            Log.d("【ChatBroadcastListener】流式完成", "标记流式消息完成: index=$currentStreamMessageIndex")
                        }
                        
                        // 通知ChatAdapter重置流式消息索引
                        chatAdapter.setCurrentStreamMessageIndex(-1)
                        
                        // 清理当前对话的流式消息去重缓存
                        currentStreamConversationId?.let { conversationId ->
                            processedStreamChunks.remove(conversationId)
                            Log.d("【ChatBroadcastListener】流式消息去重", "清理对话缓存: $conversationId")
                        }
                        
                        currentStreamConversationId = null
                        currentStreamMessageIndex = -1
                    } else {
                        // 其他系统消息正常显示
                        val systemMessage = Message.createReceivedMessage(
                            content = reply,
                            senderName = senderName,
                            senderId = senderId
                        )
                        chatAdapter.addMessage(systemMessage)
                    }
                }
                BroadcastConst.MessageType.IMAGE,
                BroadcastConst.MessageType.VIDEO,
                BroadcastConst.MessageType.AUDIO,
                BroadcastConst.MessageType.MULTIMEDIA -> {
                    // 多媒体消息处理
                    val multimediaMessage = Message.createMultimediaMessage(
                        content = reply,
                        senderName = senderName,
                        senderId = senderId,
                        isSentByMe = false,
                        imageData = imageData,
                        videoData = videoData,
                        audioData = audioData,
                        multimediaType = multimediaType,
                        messageType = messageType,
                        conversationId = conversationId
                    )
                    chatAdapter.addMessage(multimediaMessage)
                }
            }
            
            // 滚动到最新消息
            if (chatAdapter.itemCount > 0) {
                rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
            }
        } catch (e: Exception) {
            Log.e("【ChatBroadcastListener】错误", "显示消息时出错: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 处理流式消息块
     */
    private fun handleStreamChunk(chunk: String, senderName: String, senderId: String, conversationId: String) {
        // 检查chunk是否有效
        if (chunk.isBlank()) {
            Log.d("【ChatBroadcastListener】流式消息", "chunk为空，跳过处理: conversationId='$conversationId'")
            return
        }
        
        Log.d("【ChatBroadcastListener】流式消息", "处理流式消息块: chunk='$chunk', conversationId='$conversationId', currentConversationId='$currentStreamConversationId'")
        
        // 流式消息去重检查
        if (conversationId.isNotBlank()) {
            val existingContent = processedStreamChunks.getOrPut(conversationId) { StringBuilder() }
            if (existingContent.contains(chunk)) {
                Log.d("【ChatBroadcastListener】流式消息去重", "检测到重复的流式消息块，跳过处理: chunk='$chunk', conversationId='$conversationId'")
                return
            }
            // 记录已处理的内容
            existingContent.append(chunk)
        }
        
        // 如果conversationId为空，但已经有流式对话在进行，继续追加到当前消息
        if (conversationId.isBlank() && currentStreamConversationId != null && currentStreamMessageIndex >= 0) {
            Log.d("【ChatBroadcastListener】流式消息", "conversationId为空，但继续追加到当前流式消息")
            if (currentStreamMessageIndex < messages.size) {
                chatAdapter.appendToMessageWithEffect(currentStreamMessageIndex, chunk)
            }
            // 滚动到最新消息
            if (chatAdapter.itemCount > 0) {
                rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
            }
            return
        }
        
        // 如果conversationId为空且没有进行中的流式对话，跳过处理
        if (conversationId.isBlank()) {
            Log.w("【ChatBroadcastListener】流式消息", "conversationId为空且无进行中的流式对话，跳过处理: chunk='$chunk'")
            return
        }
        
        // 检查是否是新的流式对话
        if (currentStreamConversationId != conversationId) {
            // 先移除思考消息（如果存在）
            removeThinkingMessage()
            
            // 新的流式对话，创建新消息（初始为空，然后用打字机效果显示第一个chunk）
            val streamMessage = Message.createReceivedMessage(
                content = "",
                senderName = senderName,
                senderId = senderId,
                conversationId = conversationId,
                isStreamCompleted = false // 流式消息创建时标记为未完成
            )
            chatAdapter.addMessage(streamMessage)
            currentStreamConversationId = conversationId
            currentStreamMessageIndex = messages.size - 1
            
            // 通知ChatAdapter当前流式消息的索引
            chatAdapter.setCurrentStreamMessageIndex(currentStreamMessageIndex)
            
            Log.d("【ChatBroadcastListener】流式消息", "创建新的流式消息，索引: $currentStreamMessageIndex")
            
            // 添加第一个chunk并应用打字机效果
            if (currentStreamMessageIndex >= 0 && currentStreamMessageIndex < messages.size) {
                chatAdapter.appendToMessageWithEffect(currentStreamMessageIndex, chunk)
            }
        } else {
            // 同一对话，追加到现有消息并应用打字机效果
            if (currentStreamMessageIndex >= 0 && currentStreamMessageIndex < messages.size) {
                Log.d("【ChatBroadcastListener】流式消息", "追加到现有消息，索引: $currentStreamMessageIndex")
                chatAdapter.appendToMessageWithEffect(currentStreamMessageIndex, chunk)
            } else {
                Log.w("【ChatBroadcastListener】流式消息", "无效的消息索引: $currentStreamMessageIndex, 消息总数: ${messages.size}")
            }
        }
        
        // 滚动到最新消息
        if (chatAdapter.itemCount > 0) {
            rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }
    
    /**
     * 清理消息缓存
     */
    fun clearMessageCache() {
        processedMessages.clear()
        processedStreamChunks.clear()
        currentStreamConversationId = null
        currentStreamMessageIndex = -1
        Log.d("【ChatBroadcastListener】消息去重", "已清理所有消息缓存")
    }
}