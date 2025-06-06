package com.qunar.barrier_free_qunar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DrawerActivity : AppCompatActivity() {

    private lateinit var ivUserAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvUserStatus: TextView
    private lateinit var llProfileEdit: LinearLayout
    private lateinit var llWallet: LinearLayout
    private lateinit var llSettings: LinearLayout
    private lateinit var rvChatHistory: RecyclerView
    private lateinit var chatHistoryAdapter: DrawerChatHistoryAdapter
    private val chatHistoryList = mutableListOf<ChatHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)
        
        initViews()
        setupClickListeners()
        setupChatHistory()
        loadUserData()
        loadChatHistory()
        
        // 添加从左边滑入的动画
        val drawerContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.drawer_container)
        drawerContainer.translationX = -280f * resources.displayMetrics.density
        drawerContainer.animate()
            .translationX(0f)
            .setDuration(300)
            .start()
            
        // 点击透明区域关闭抽屉
        val rootLayout = findViewById<FrameLayout>(android.R.id.content)
        rootLayout.setOnClickListener {
            finish()
        }
        
        // 防止点击抽屉内容区域时关闭
        drawerContainer.setOnClickListener {
            // 阻止事件传播
        }
    }

    private fun initViews() {
        ivUserAvatar = findViewById(R.id.iv_user_avatar)
        tvUsername = findViewById(R.id.tv_username)
        tvUserStatus = findViewById(R.id.tv_user_status)
        llProfileEdit = findViewById(R.id.ll_profile_edit)
        llWallet = findViewById(R.id.ll_wallet)
        llSettings = findViewById(R.id.ll_settings)
        rvChatHistory = findViewById(R.id.rv_chat_history)
    }

    private fun setupClickListeners() {
        // 头像点击事件
        ivUserAvatar.setOnClickListener {
            showAvatarDialog()
        }

        // 个人信息编辑
        llProfileEdit.setOnClickListener {
            openProfileEdit()
        }

        // 钱包
        llWallet.setOnClickListener {
            openWallet()
        }

        // 设置
        llSettings.setOnClickListener {
            openSettings()
        }

        // 对话历史现在通过RecyclerView的item点击处理

        // 点击背景区域关闭抽屉
        findViewById<FrameLayout>(android.R.id.content).setOnClickListener {
            closeDrawer()
        }
        
        // 防止点击抽屉内容区域时关闭
        findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.drawer_container).setOnClickListener {
            // 阻止事件传播
        }
    }

    private fun loadUserData() {
        // 从SharedPreferences或数据库加载用户数据
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "用户_jPUiSX")
        val isOnline = sharedPref.getBoolean("is_online", true)
        
        tvUsername.text = username
        tvUserStatus.text = if (isOnline) "在线" else "离线"
        
        // TODO: 加载用户头像
        // Glide.with(this).load(avatarUrl).into(ivUserAvatar)
    }

    private fun showAvatarDialog() {
        // TODO: 显示头像操作对话框（查看大图、更换头像等）
        Toast.makeText(this, "头像功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun openProfileEdit() {
        // 先关闭抽屉，然后跳转到个人信息编辑页面
        val drawerContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.drawer_container)
        drawerContainer.animate()
            .translationX(-280f * resources.displayMetrics.density)
            .setDuration(300)
            .withEndAction {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
            .start()
    }

    private fun openWallet() {
        // TODO: 跳转到钱包页面
        Toast.makeText(this, "钱包功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun openSettings() {
        // TODO: 跳转到设置页面
        Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun setupChatHistory() {
        chatHistoryAdapter = DrawerChatHistoryAdapter(chatHistoryList) { chatHistoryItem ->
            openChatDetail(chatHistoryItem)
        }
        rvChatHistory.apply {
            layoutManager = LinearLayoutManager(this@DrawerActivity)
            adapter = chatHistoryAdapter
        }
    }

    private fun loadChatHistory() {
        // 模拟加载对话历史数据（限制显示最近5条）
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
        finish() // 关闭抽屉页面
    }

    private fun closeDrawer() {
        val drawerContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.drawer_container)
        drawerContainer.animate()
            .translationX(-280f * resources.displayMetrics.density)
            .setDuration(300)
            .withEndAction {
                finish()
            }
            .start()
    }

    override fun finish() {
        super.finish()
        // 移除默认的Activity转场动画
        overridePendingTransition(0, 0)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DrawerActivity::class.java)
            context.startActivity(intent)
        }
    }
}