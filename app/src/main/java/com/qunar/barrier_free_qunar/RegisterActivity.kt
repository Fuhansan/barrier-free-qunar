package com.qunar.barrier_free_qunar

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.qunar.barrier_free_qunar.java.sdk.consts.URLConst
import com.qunar.barrier_free_qunar.java.sdk.util.BFHttpUtils
import com.qunar.barrier_free_qunar.kotlin.utils.HttpResponseParser

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var btnBack: ImageButton
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvAlreadyHaveAccount: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        tilPhone = findViewById(R.id.til_phone)
        tilPassword = findViewById(R.id.til_password)
        tilConfirmPassword = findViewById(R.id.til_confirm_password)
        etPhone = findViewById(R.id.et_phone)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnRegister = findViewById(R.id.btn_register)
        tvAlreadyHaveAccount = findViewById(R.id.tv_already_have_account)
    }
    
    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        
        btnRegister.setOnClickListener {
            performRegister()
        }
        
        tvAlreadyHaveAccount.setOnClickListener {
            finish()
        }
    }
    
    private fun performRegister() {
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        
        // 清除之前的错误信息
        tilPhone.error = null
        tilPassword.error = null
        tilConfirmPassword.error = null
        
        // 验证输入
        if (!validateInput(phone, password, confirmPassword)) {
            return
        }

        // 显示加载状态
        btnRegister.isEnabled = false
        btnRegister.text = "注册中..."
        
        // 在后台线程执行网络请求
        Thread {
            try {
                // 构建请求体
                val requestBody = mapOf(
                    "rePhone" to phone,
                    "password" to password,
                    "userName" to "用户_${phone.takeLast(4)}", // 使用手机号后4位作为默认用户名
                    "email" to "${phone}@example.com" // 使用手机号生成默认邮箱
                )
                

                // 调用注册API
                val result = BFHttpUtils.postObject(URLConst.BF_SIGN_UP, requestBody, null)

                
                // 解析注册响应数据
                val registerInfo = HttpResponseParser.parseRegisterInfo(result.data, phone)
                Log.d("注册解析", "解析后的注册信息: $registerInfo")
                
                // 在主线程处理结果
                runOnUiThread {
                    btnRegister.isEnabled = true
                    btnRegister.text = "注册"
                    
                    if (result.isSuccess) {
                        // 注册成功
                        Toast.makeText(this@RegisterActivity, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        // 注册失败
                        val errorMessage = when (result.code) {
                            409 -> "该手机号已被注册"
                            400 -> "注册信息格式错误"
                            500 -> "服务器错误，请稍后重试"
                            else -> "注册失败：${result.msg ?: "未知错误"}"
                        }
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                // 网络异常处理
                runOnUiThread {
                    btnRegister.isEnabled = true
                    btnRegister.text = "注册"
                    Toast.makeText(this@RegisterActivity, "网络连接失败，请检查网络设置", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    
    private fun validateInput(phone: String, password: String, confirmPassword: String): Boolean {
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
        
        // 验证确认密码
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.error = getString(R.string.password_empty)
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmPassword.error = getString(R.string.password_not_match)
            isValid = false
        }
        
        return isValid
    }
    
    private fun isValidPhone(phone: String): Boolean {
        return phone.length == 11 && phone.startsWith("1")
    }
}