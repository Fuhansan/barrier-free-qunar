package com.qunar.barrier_free_qunar


import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.LinearLayoutManager


import com.qunar.barrier_free_qunar.java.sdk.consts.BroadcastConst
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BroadcastManager

import android.database.ContentObserver

import android.os.Handler
import android.os.Looper
import com.qunar.barrier_free_qunar.java.sdk.broadcast.ChatBroadcastReceiver
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BaseBroadcastReceiver

class ChatActivity : AppCompatActivity() {
    

    private lateinit var btnLog: ImageButton
    private lateinit var btnProfile: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvAccessibilityStatus: TextView
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    
    private var currentStreamMessageIndex: Int = -1
    
    private var accessibilitySettingsObserver: ContentObserver? = null
    
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    
    // 广播管理器
    private lateinit var broadcastManager: BroadcastManager
    
    // 聊天广播接收器
    private lateinit var chatBroadcastReceiver: ChatBroadcastReceiver
    
    // 广播接收器ID
    private val CHAT_RECEIVER_ID = "chat_activity_receiver"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        // Log.d("ChatActivity", "ChatActivity开始创建")
        
        initViews()
        setupClickListeners()
        setupRecyclerView()
        loadInitialMessages()
        
        // 检查无障碍服务状态
        updateAccessibilityStatus()
        
        // 初始化广播管理器
        initializeBroadcastManager()
        
        registerBroadcastReceiver()
        setupAccessibilityObserver()
        
        // Log.d("ChatActivity", "ChatActivity创建完成")
    }
    
    /**
     * 初始化广播管理器
     */
    private fun initializeBroadcastManager() {
        try {
            // 获取广播管理器实例
            broadcastManager = BroadcastManager.getInstance(this)
            
            // 创建聊天广播接收器
            chatBroadcastReceiver = ChatBroadcastReceiver(object : ChatBroadcastReceiver.ChatBroadcastListener {
                override fun onMessageReceived(messageData: BaseBroadcastReceiver.MessageData) {
                    // 检查消息是否为空
                    if (!messageData.reply.isNullOrEmpty()) {
                        removeThinkingMessage()
                        // 在主线程中更新UI
                        runOnUiThread {
                            displayMessage(
                                messageData.originalMessage ?: "",
                                messageData.reply,
                                messageData.senderName ?: "未知",
                                messageData.senderId ?: "unknown",
                                messageData.timestamp,
                                messageData.conversationId ?: "",
                                messageData.messageType ?: BroadcastConst.MessageType.TEXT
                            )
                        }
                    } else {
                        Log.w("【ChatActivity】广播", "接收到空消息，忽略处理")
                    }
                }
                
                override fun onError(intent: Intent?, error: Exception) {
                    Log.e("【ChatActivity】错误", "处理广播消息时出错: ${error.message}")
                    error.printStackTrace()
                }
            })
            
            // Log.d("【ChatActivity】广播", "广播管理器初始化成功")
        } catch (e: Exception) {
            Log.e("【ChatActivity】错误", "初始化广播管理器失败: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 注册广播接收器
     */
    private fun registerBroadcastReceiver() {
        try {
            // 注册聊天广播接收器
            val success = broadcastManager.registerReceiver(
                CHAT_RECEIVER_ID,
                chatBroadcastReceiver,
                BroadcastConst.Action.SIMULATE_MESSAGE
            )
            
            if (success) {
                // Log.d("【ChatActivity】广播", "广播接收器注册成功")
            } else {
                Log.w("【ChatActivity】广播", "广播接收器注册失败")
            }
        } catch (e: Exception) {
             Log.e("【ChatActivity】错误", "注册广播接收器失败: ${e.message}")
             e.printStackTrace()
         }
     }
    

    
    override fun onResume() {
        super.onResume()
        // Log.d("ChatActivity", "Activity恢复")
        // onCreate中已经注册了广播接收器，这里不需要重复注册
        
        // 更新无障碍服务状态
        updateAccessibilityStatus()
    }
    
    override fun onPause() {
        super.onPause()
        // Log.d("ChatActivity", "Activity暂停")
        // 保持广播接收器注册状态，以便在后台也能接收消息
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Log.d("ChatActivity", "Activity销毁")
        // 确保广播接收器被注销
        try {
            if (::broadcastManager.isInitialized) {
                val success = broadcastManager.unregisterReceiver(CHAT_RECEIVER_ID)
                if (success) {
                    // Log.d("【ChatActivity】广播", "广播接收器注销成功")
                } else {
                    Log.w("【ChatActivity】广播", "广播接收器注销失败")
                }
            } else {
                Log.w("【ChatActivity】广播", "广播管理器未初始化，无法注销接收器")
            }
        } catch (e: Exception) {
            Log.e("【ChatActivity】错误", "注销广播接收器时出错: ${e.message}")
            e.printStackTrace()
        }
        accessibilitySettingsObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
    }
    
    private fun initViews() {
        // Log.d("ChatActivity", "chat 界面开始初始化")
        try {

            btnLog = findViewById(R.id.btn_log)
            btnProfile = findViewById(R.id.btn_profile)
            tvTitle = findViewById(R.id.tv_title)
            tvAccessibilityStatus = findViewById(R.id.tv_accessibility_status)
            rvMessages = findViewById(R.id.rv_messages)
            etMessage = findViewById(R.id.et_message)
            btnSend = findViewById(R.id.btn_send)
        } catch (e: Exception) {
            Log.e("ChatActivity", "视图初始化失败: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
    }
    
    private fun setupClickListeners() {
        // Log.d("ChatActivity", "开始设置点击监听器")
        try {

            btnLog.setOnClickListener {
                android.widget.Toast.makeText(this, "打开系统日志", android.widget.Toast.LENGTH_SHORT).show()
                Log.d("日志系统", "打开")
                try {
                    val intent = Intent(this, LogActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(this, "无法打开日志页面: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
            
            btnProfile.setOnClickListener {
                // Log.d("ChatActivity", "头像按钮被点击")
                try {
                    DrawerActivity.start(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 添加Toast提示用户
                    android.widget.Toast.makeText(this, "无法打开侧边栏: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }

            btnSend.setOnClickListener {
               Log.d("ChatActivity", "发送按钮被点击")
                sendMessage()
            }


        } catch (e: Exception) {
            Log.e("ChatActivity", "设置点击监听器失败: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        
        if (TextUtils.isEmpty(messageText)) {
            // 如果输入框为空，显示提示信息
            return
        }
        
        // 创建发送的消息
        val sentMessage = Message.createSentMessage(messageText)
        chatAdapter.addMessage(sentMessage)
        
        // 清空输入框
        etMessage.text.clear()
        
        // 滚动到最新消息
        rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
        
        // 添加思考消息
        addThinkingMessage()
        
        // 等待真实回复消息
    }
    
    /**
     * 添加思考消息
     */
    private fun addThinkingMessage() {
        val thinkingMessage = Message.createReceivedMessage(
            content = "正在思考...",
            senderName = "小助手",
            senderId = "system_thinking"
        )
        chatAdapter.addMessage(thinkingMessage)
        
        // 滚动到最新消息
        rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
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
     * 添加回复消息
     */
    private fun addReplyMessage(replyContent: String) {
        // 先移除思考消息
        removeThinkingMessage()
        
        // 然后添加回复消息
        val replyMessage = Message.createReceivedMessage(
            content = replyContent,
            senderName = "小助手",
            senderId = "assistant"
        )
        chatAdapter.addMessage(replyMessage)
        
        // 滚动到最新消息
        rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun loadInitialMessages() {
        // 添加一些初始消息
        val welcomeMessage = Message.createReceivedMessage(
            content = "欢迎使用聊天应用！有什么可以帮助你的吗？",
            senderName = "小助手",
            senderId = "assistant"
        )
        chatAdapter.addMessage(welcomeMessage)
    }
    
    /**
     * 显示接收到的消息
     */
    private fun displayMessage(originalMessage: String, reply: String, senderName: String, senderId: String, timestamp: Long, conversationId: String, messageType: String) {
        try {
            // 根据消息类型处理不同的显示逻辑
            when (messageType) {
                BroadcastConst.MessageType.TEXT -> {
                    // 普通文本消息 - 添加回复消息
                    addReplyMessage(reply)
                }
                BroadcastConst.MessageType.STREAM_CHUNK -> {
                    // 流式消息块，使用conversationId来区分不同的对话
                    // Log.d("ChatActivity", "接收到流式消息块: '$reply', 对话ID: $conversationId")
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
                        currentStreamConversationId = null
                        currentStreamMessageIndex = -1
                        // 通知ChatAdapter重置流式消息索引
                        chatAdapter.setCurrentStreamMessageIndex(-1)

                        // 不显示完成标记消息，只重置状态
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
            }
            
            // 滚动到最新消息
            if (chatAdapter.itemCount > 0) {
                rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
            }
            

        } catch (e: Exception) {
            Log.e("【ChatActivity】错误", "显示消息时出错: ${e.message}")
            e.printStackTrace()
        }
    }

    // 用于跟踪当前流式消息的对话ID和消息索引
    private var currentStreamConversationId: String? = null

    
    /**
     * 处理流式消息块
     */
    private fun handleStreamChunk(chunk: String, senderName: String, senderId: String, conversationId: String) {
        // Log.d("ChatActivity", "处理流式消息块: chunk='$chunk', conversationId='$conversationId', currentConversationId='$currentStreamConversationId'")
        // 检查是否是新的流式对话
        if (currentStreamConversationId != conversationId) {
            // 新的流式对话，创建新消息（初始为空，然后用打字机效果显示第一个chunk）
            val streamMessage = Message.createReceivedMessage(
                content = "",
                senderName = senderName,
                senderId = senderId
            )
            chatAdapter.addMessage(streamMessage)
            currentStreamConversationId = conversationId
            currentStreamMessageIndex = messages.size - 1
            
            // 通知ChatAdapter当前流式消息的索引
            chatAdapter.setCurrentStreamMessageIndex(currentStreamMessageIndex)
            

            
            // 添加第一个chunk并应用打字机效果
            if (currentStreamMessageIndex >= 0 && currentStreamMessageIndex < messages.size) {
                chatAdapter.appendToMessageWithEffect(currentStreamMessageIndex, chunk)
            }
        } else {
            // 同一对话，追加到现有消息并应用打字机效果
            if (currentStreamMessageIndex >= 0 && currentStreamMessageIndex < messages.size) {
                chatAdapter.appendToMessageWithEffect(currentStreamMessageIndex, chunk)

            }
        }
        
        // 滚动到最新消息
        if (chatAdapter.itemCount > 0) {
            rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }
    
    /**
     * 检查并更新无障碍服务状态
     */
    private fun updateAccessibilityStatus() {
        val isAccessibilityEnabled = isAccessibilityServiceEnabled()
        
        if (isAccessibilityEnabled) {
            tvAccessibilityStatus.text = "无障碍服务已开启"
            tvAccessibilityStatus.setTextColor(getColor(android.R.color.holo_green_light))
        } else {
            tvAccessibilityStatus.text = "无障碍服务未开启"
            tvAccessibilityStatus.setTextColor(getColor(android.R.color.holo_red_light))
        }
    }
    
    /**
     * 检查无障碍服务是否已启用
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        
        val packageName = packageName
        val serviceName = "$packageName/.java.GlobalAccessibilityService"
        
        return enabledServices?.contains(serviceName) == true && accessibilityManager.isEnabled
    }
    
    /**
     * 设置无障碍服务设置监听器
     */
    private fun setupAccessibilityObserver() {
        accessibilitySettingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                // 当无障碍服务设置发生变化时，更新状态显示
                updateAccessibilityStatus()
            }
        }
        
        // 注册监听无障碍服务设置的变化
        val uri = android.provider.Settings.Secure.getUriFor(
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        contentResolver.registerContentObserver(uri, false, accessibilitySettingsObserver!!)
        
        // 同时监听无障碍服务总开关的变化
        val accessibilityEnabledUri = android.provider.Settings.Secure.getUriFor(
            android.provider.Settings.Secure.ACCESSIBILITY_ENABLED
        )
        contentResolver.registerContentObserver(accessibilityEnabledUri, false, accessibilitySettingsObserver!!)
    }
}