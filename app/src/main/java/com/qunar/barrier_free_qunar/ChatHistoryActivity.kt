package com.qunar.barrier_free_qunar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatHistoryActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var rvChatHistory: RecyclerView
    private lateinit var chatHistoryAdapter: ChatHistoryAdapter
    private val chatHistoryList = mutableListOf<ChatHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)
        
        initViews()
        setupClickListeners()
        setupRecyclerView()
        loadChatHistory()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        tvTitle = findViewById(R.id.tv_title)
        rvChatHistory = findViewById(R.id.rv_chat_history)
        
        tvTitle.text = "对话历史"
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        chatHistoryAdapter = ChatHistoryAdapter(chatHistoryList) { chatHistoryItem ->
            // 点击历史记录项，跳转到对应的聊天页面
            openChatDetail(chatHistoryItem)
        }
        rvChatHistory.apply {
            layoutManager = LinearLayoutManager(this@ChatHistoryActivity)
            adapter = chatHistoryAdapter
        }
    }

    private fun loadChatHistory() {
        // 模拟加载对话历史数据
        // TODO: 从数据库或SharedPreferences加载真实的对话历史
        val sampleHistory = listOf(
            ChatHistoryItem(
                id = "1",
                title = "android studio 怎么打包成一个apk?",
                lastMessage = "你可以通过Build菜单中的Generate Signed Bundle/APK选项来打包...",
                timestamp = System.currentTimeMillis() - 3600000, // 1小时前
                messageCount = 8
            ),
            ChatHistoryItem(
                id = "2",
                title = "我在android studio创建了一个项目，项目当中有要调用http请求，但是网络请求不通?",
                lastMessage = "这个问题通常是因为Android 9.0以上版本默认不允许HTTP请求...",
                timestamp = System.currentTimeMillis() - 7200000, // 2小时前
                messageCount = 12
            ),
            ChatHistoryItem(
                id = "3",
                title = "dnf 90 点智力登录多少增益?",
                lastMessage = "90点智力在DNF中可以提供约18%的魔法攻击力加成...",
                timestamp = System.currentTimeMillis() - 86400000, // 1天前
                messageCount = 5
            ),
            ChatHistoryItem(
                id = "4",
                title = "mack book pro 能玩游戏?",
                lastMessage = "MacBook Pro可以玩游戏，但游戏选择相对有限...",
                timestamp = System.currentTimeMillis() - 172800000, // 2天前
                messageCount = 15
            ),
            ChatHistoryItem(
                id = "5",
                title = "HttpClient 请求一个 stream 类型的接口",
                lastMessage = "使用HttpClient处理stream类型接口需要注意几个要点...",
                timestamp = System.currentTimeMillis() - 259200000, // 3天前
                messageCount = 20
            )
        )
        
        chatHistoryList.clear()
        chatHistoryList.addAll(sampleHistory)
        chatHistoryAdapter.notifyDataSetChanged()
    }

    private fun openChatDetail(chatHistoryItem: ChatHistoryItem) {
        // 跳转到聊天详情页面，传递对话ID
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chat_history_id", chatHistoryItem.id)
        intent.putExtra("chat_title", chatHistoryItem.title)
        startActivity(intent)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChatHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }
}