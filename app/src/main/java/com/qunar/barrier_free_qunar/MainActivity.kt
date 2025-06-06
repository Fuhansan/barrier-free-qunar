package com.qunar.barrier_free_qunar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREF_NAME = "user_credentials"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"
        private const val LOGIN_EXPIRE_DAYS = 7L
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // 检查用户登录状态
        checkLoginStatus()
    }
    
    /**
     * 检查登录状态并决定跳转到哪个界面
     */
    private fun checkLoginStatus() {
        if (isUserLoggedIn()) {
            // 用户已登录且未过期，更新最后使用时间并跳转到聊天界面
            updateLastActiveTime()
            startChatActivity()
        } else {
            // 用户未登录或登录已过期，跳转到登录界面
            startLoginActivity()
        }
    }
    
    /**
     * 检查用户是否已登录且未过期
     */
    private fun isUserLoggedIn(): Boolean {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        return isLoggedIn && !isLoginExpired()
    }
    
    /**
     * 检查登录是否过期（7天未使用）
     */
    private fun isLoginExpired(): Boolean {
        val lastLoginTime = sharedPreferences.getLong(KEY_LAST_LOGIN_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        val daysDiff = (currentTime - lastLoginTime) / (1000 * 60 * 60 * 24)
        return daysDiff >= LOGIN_EXPIRE_DAYS
    }
    
    /**
     * 更新最后使用时间
     */
    private fun updateLastActiveTime() {
        val editor = sharedPreferences.edit()
        editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
        editor.apply()
    }
    
    /**
     * 跳转到登录界面
     */
    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    /**
     * 跳转到聊天界面
     */
    private fun startChatActivity() {
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
        finish()
    }
}