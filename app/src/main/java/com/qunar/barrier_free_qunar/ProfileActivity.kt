package com.qunar.barrier_free_qunar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    
    // 顶部导航
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    
    // 头像区域
    private lateinit var ivAvatar: ImageView
    
    // 功能区域的LinearLayout容器
    private lateinit var cardNickname: LinearLayout  // 消息通知
    private lateinit var cardPhone: LinearLayout     // 模式选择
    private lateinit var cardEmail: LinearLayout     // 个性装扮与特权外显
    
    // 隐私区域的LinearLayout容器
    private lateinit var cardPrivacy: LinearLayout   // 隐私设置
    
    // 其他功能区域
    private lateinit var cardAbout: LinearLayout     // 关于QQ与帮助
    private lateinit var cardLogout: LinearLayout    // 退出当前账号
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // android.util.Log.d("ProfileActivity", "ProfileActivity onCreate 开始")
        Toast.makeText(this, "ProfileActivity 正在启动", Toast.LENGTH_SHORT).show()
        
        setContentView(R.layout.activity_profile)
        // android.util.Log.d("ProfileActivity", "布局文件设置完成")
        
        initViews()
        // android.util.Log.d("ProfileActivity", "视图初始化完成")
        
        setupClickListeners()
        // android.util.Log.d("ProfileActivity", "点击监听器设置完成")
        
        loadUserData()
        // android.util.Log.d("ProfileActivity", "用户数据加载完成")
        
        Toast.makeText(this, "ProfileActivity 启动完成", Toast.LENGTH_SHORT).show()
    }
    
    private fun initViews() {
        try {
            // 顶部导航
            btnBack = findViewById(R.id.btn_back)
            tvTitle = findViewById(R.id.tv_title)
            
            // 头像区域
            ivAvatar = findViewById(R.id.iv_avatar)
            
            // 功能区域 - 使用XML中实际定义的ID
            cardNickname = findViewById(R.id.card_nickname)  // 消息通知
            cardPhone = findViewById(R.id.card_phone)        // 模式选择
            cardEmail = findViewById(R.id.card_email)        // 个性装扮与特权外显
            
            // 隐私区域
            cardPrivacy = findViewById(R.id.card_privacy)    // 隐私设置
            
            // 其他功能
            cardAbout = findViewById(R.id.card_about)         // 关于QQ与帮助
            cardLogout = findViewById(R.id.card_logout)       // 退出当前账号
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "初始化视图失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun setupClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener {
            finish()
        }
        
        // 头像点击事件
        ivAvatar.setOnClickListener {
            showAvatarDialog()
        }
        
        // 消息通知
        cardNickname.setOnClickListener {
            showNotificationSettings()
        }
        
        // 模式选择
        cardPhone.setOnClickListener {
            showModeSelection()
        }
        
        // 个性装扮与特权外显
        cardEmail.setOnClickListener {
            showPersonalizationSettings()
        }
        
        // 隐私设置
        cardPrivacy.setOnClickListener {
            showPrivacySettings()
        }
        
        // 关于QQ与帮助
        cardAbout.setOnClickListener {
            showAboutAndHelp()
        }
        
        // 退出当前账号
        cardLogout.setOnClickListener {
            showLogoutDialog()
        }
    }
    
    private fun loadUserData() {
        // 设置标题
        tvTitle.text = "个人资料"
        
        // TODO: 从SharedPreferences或数据库加载用户数据
        // 这里可以加载用户头像、用户名等信息
    }
    
    private fun showAvatarDialog() {
        // 显示头像选择对话框
        val options = arrayOf("查看头像", "更换头像", "拍照")
        AlertDialog.Builder(this)
            .setTitle("头像操作")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewAvatar()
                    1 -> changeAvatar()
                    2 -> takePhoto()
                }
            }
            .show()
    }
    
    private fun viewAvatar() {
        Toast.makeText(this, "查看头像功能待实现", Toast.LENGTH_SHORT).show()
    }
    
    private fun changeAvatar() {
        Toast.makeText(this, "更换头像功能待实现", Toast.LENGTH_SHORT).show()
    }
    
    private fun takePhoto() {
        Toast.makeText(this, "拍照功能待实现", Toast.LENGTH_SHORT).show()
    }
    
    private fun showNotificationSettings() {
        Toast.makeText(this, "消息通知设置功能待实现", Toast.LENGTH_SHORT).show()
        // TODO: 跳转到消息通知设置页面
    }
    
    private fun showModeSelection() {
        Toast.makeText(this, "模式选择功能待实现", Toast.LENGTH_SHORT).show()
        // TODO: 显示模式选择对话框或跳转到模式设置页面
    }
    
    private fun showPersonalizationSettings() {
        Toast.makeText(this, "个性装扮与特权外显功能待实现", Toast.LENGTH_SHORT).show()
        // TODO: 跳转到个性化设置页面
    }
    
    private fun showPrivacySettings() {
        Toast.makeText(this, "隐私设置功能待实现", Toast.LENGTH_SHORT).show()
        // TODO: 跳转到隐私设置页面
    }
    
    private fun showAboutAndHelp() {
        Toast.makeText(this, "关于QQ与帮助功能待实现", Toast.LENGTH_SHORT).show()
        // TODO: 跳转到关于页面
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("确定要退出当前账号吗？")
            .setPositiveButton("确定") { _, _ ->
                logout()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun logout() {
        // 清除用户数据和登录状态
        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(userPrefs.edit()) {
            clear()
            apply()
        }
        
        // 清除登录凭据缓存（与LoginActivity保持一致）
        val credentialsPrefs = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        with(credentialsPrefs.edit()) {
            clear()
            apply()
        }
        
        // 显示退出成功提示
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
        
        // 跳转到登录页面
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    companion object {
        /**
         * 启动ProfileActivity的便捷方法
         */
        fun start(context: Context) {
            val intent = Intent(context, ProfileActivity::class.java)
            context.startActivity(intent)
        }
    }
}