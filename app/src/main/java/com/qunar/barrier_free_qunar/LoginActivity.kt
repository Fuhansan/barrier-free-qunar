package com.qunar.barrier_free_qunar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.qunar.barrier_free_qunar.java.sdk.consts.URLConst
import com.qunar.barrier_free_qunar.java.sdk.util.BFHttpUtils
import com.qunar.barrier_free_qunar.kotlin.utils.HttpResponseParser

class LoginActivity : AppCompatActivity() {

    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegister: TextView
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREF_NAME = "user_credentials"
        private const val KEY_PHONE = "saved_phone"
        private const val KEY_PASSWORD = "saved_password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_CODE = "user_code"
        private const val KEY_ID_CARD = "id_card"
        private const val KEY_EMAIL = "email"
        private const val KEY_REG_PHONE = "reg_phone"
        private const val LOGIN_EXPIRE_DAYS = 7L // 7天后登录过期
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupClickListeners()
        loadSavedCredentials()
    }

    private fun initViews() {
        tilPhone = findViewById(R.id.til_phone)
        tilPassword = findViewById(R.id.til_password)
        etPhone = findViewById(R.id.et_phone)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvRegister = findViewById(R.id.tv_register)
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<android.widget.TextView>(R.id.tv_forgot_password).setOnClickListener {
            // TODO: 实现忘记密码功能
            Toast.makeText(this, "忘记密码功能待实现", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin() {
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // 清除之前的错误信息
        tilPhone.error = null
        tilPassword.error = null

        // 验证输入
        if (!validateInput(phone, password)) {
            return
        }

        // 显示加载状态
        btnLogin.isEnabled = false
        btnLogin.text = "登录中..."

        // 在后台线程执行网络请求
        Thread {
            try {
                // 构建请求体
                val requestBody = mapOf(
                    "rePhone" to phone,
                    "password" to password,
                    "loginType" to "PASSWORD"
                )

                // 调用登录API
                val result = BFHttpUtils.postObject(URLConst.BF_LOG_IN, requestBody, null)

                // 将接口返回的数据赋值给user
                val user = HttpResponseParser.parseUserInfo(result.data, phone)

                // 在主线程处理结果
                runOnUiThread {
                    btnLogin.isEnabled = true
                    btnLogin.text = "登录"

                    if (result.isSuccess) {
                        // 登录成功，保存用户信息
                        val userId = user["userId"] as? String
                        var userCode = user["userCode"] as? String
                        var userName = user["userName"] as? String

                        saveUserInfo(phone, password, userId, userCode, userName)
                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()

                        // 跳转到聊天界面
                        val intent = Intent(this@LoginActivity, ChatActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // 登录失败
                        val errorMessage = when (result.code) {
                            20000 -> "密码错误"
                            20001 -> "用户未注册"
                            else -> "服务器错误，请稍后重试"
                        }
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                // 网络异常处理
                runOnUiThread {
                    btnLogin.isEnabled = true
                    btnLogin.text = "登录"
                    Toast.makeText(this@LoginActivity, "网络连接失败，请检查网络设置", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun validateInput(phone: String, password: String): Boolean {
        var isValid = true

        // 验证手机号
        if (TextUtils.isEmpty(phone)) {
            tilPhone.error = getString(R.string.phone_empty)
            isValid = false
        } else if (!isValidPhone(phone)) {
            tilPhone.error = getString(R.string.phone_invalid)
            isValid = false
        }

        // 验证密码
        if (TextUtils.isEmpty(password)) {
            tilPassword.error = getString(R.string.password_empty)
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = getString(R.string.password_too_short)
            isValid = false
        }

        return isValid
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length == 11 && phone.startsWith("1")
    }



    /**
     * 保存用户信息到本地
     */
    private fun saveUserInfo(phone: String, password: String, userId: String?, userCode: String?, userName: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_PHONE, phone)
        editor.putString(KEY_PASSWORD, password)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
        if (!userId.isNullOrEmpty()) {
            editor.putString(KEY_USER_ID, userId)
        }

        if (!userCode.isNullOrEmpty()) {
            editor.putString(KEY_USER_CODE, userCode)
        }

        if (!userName.isNullOrEmpty()) {
            editor.putString(KEY_USER_NAME, userName)
        }

        editor.apply()
    }

    /**
     * 加载已保存的用户凭据
     */
    private fun loadSavedCredentials() {
        val savedPhone = sharedPreferences.getString(KEY_PHONE, "")
        val savedPassword = sharedPreferences.getString(KEY_PASSWORD, "")
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)

        // 检查登录是否过期
        if (isLoggedIn && isLoginExpired()) {
            // 登录已过期，清除凭据
            clearSavedCredentials()
            Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_LONG).show()
            return
        }

        // 如果用户之前已登录且未过期，自动填充账号密码
        if (isLoggedIn && !savedPhone.isNullOrEmpty()) {
            etPhone.setText(savedPhone)
            etPassword.setText(savedPassword)
        }
    }

    /**
     * 清除保存的用户凭据（用于退出登录）
     */
    fun clearSavedCredentials() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    /**
     * 检查用户是否已登录
     */
    fun isUserLoggedIn(): Boolean {
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
     * 更新最后使用时间（在应用启动时调用）
     */
    fun updateLastActiveTime() {
        if (isUserLoggedIn()) {
            val editor = sharedPreferences.edit()
            editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            editor.apply()
        }
    }
}