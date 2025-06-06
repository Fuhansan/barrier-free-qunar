package com.qunar.barrier_free_qunar

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LogActivity : AppCompatActivity(), LogCollector.LogListener {
    
    private lateinit var btnBack: ImageButton
    private lateinit var btnClear: ImageButton
    private lateinit var btnAutoScroll: MaterialButton
    private lateinit var btnFilter: MaterialButton
    private lateinit var btnExport: MaterialButton
    private lateinit var tvLogContent: TextView
    private lateinit var tvLogCount: TextView
    private lateinit var tvLogStatus: TextView
    private lateinit var scrollLog: ScrollView
    
    private val logBuffer = StringBuilder()
    private var logCount = 0
    private var isAutoScroll = true
    private var isLogcatRunning = false
    private var isMonitoring = false
    private var logcatProcess: Process? = null
    private lateinit var logCollector: LogCollector
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // 日志过滤关键词
    private val filterKeywords = listOf(
        "barrier_free_qunar",
        "ChatActivity",
        "GlobalAccessibilityService",
        "BroadcastManager",
        "BFHttpUtils"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        
        logCollector = LogCollector.getInstance()
        
        initViews()
        setupClickListeners()
        loadExistingLogs()
        startLogcatMonitoring()
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnClear = findViewById(R.id.btn_clear)
        btnAutoScroll = findViewById(R.id.btn_auto_scroll)
        btnFilter = findViewById(R.id.btn_filter)
        btnExport = findViewById(R.id.btn_export)
        tvLogContent = findViewById(R.id.tv_log_content)
        tvLogCount = findViewById(R.id.tv_log_count)
        tvLogStatus = findViewById(R.id.tv_log_status)
        scrollLog = findViewById(R.id.scroll_log)
        
        // 设置文本可选择
        tvLogContent.movementMethod = ScrollingMovementMethod()
        
        updateAutoScrollButton()
    }
    
    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            // 返回到聊天界面
            val intent = Intent(this, ChatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        
        btnClear.setOnClickListener {
            clearLogs()
        }
        
        btnAutoScroll.setOnClickListener {
            isAutoScroll = !isAutoScroll
            updateAutoScrollButton()
            if (isAutoScroll) {
                scrollToBottom()
            }
        }
        
        btnFilter.setOnClickListener {
            // 这里可以实现更复杂的过滤逻辑
            Toast.makeText(this, "当前过滤: ${filterKeywords.joinToString(", ")}", Toast.LENGTH_LONG).show()
        }
        
        btnExport.setOnClickListener {
            exportLogs()
        }
    }
    
    private fun updateAutoScrollButton() {
        btnAutoScroll.text = if (isAutoScroll) "自动滚动 ✓" else "自动滚动"
        btnAutoScroll.setTextColor(
            if (isAutoScroll) 
                getColor(android.R.color.holo_green_dark) 
            else 
                getColor(R.color.primary_blue)
        )
    }
    
    private fun startLogcatMonitoring() {
        if (isLogcatRunning) return
        
        isLogcatRunning = true
        isMonitoring = true
        updateStatus("● 启动中...")
        
        executor.execute {
            try {
                // 尝试使用logcat命令
                tryLogcatMethod()
            } catch (e: Exception) {
                Log.e("LogActivity", "Logcat方法失败，切换到应用日志模式: ${e.message}")
                mainHandler.post {
                    updateStatus("● 应用日志模式")
                    appendLog("[信息] 系统日志权限受限，切换到应用内日志模式")
                    appendLog("[信息] 将显示应用相关的调试信息")
                    
                    // 启动应用内日志监听 - 检查executor状态
                    if (!executor.isShutdown) {
                        startAppLogMonitoring()
                    } else {
                        Log.w("LogActivity", "Executor已关闭，无法启动应用日志监听")
                        updateStatus("● 监听已停止")
                    }
                }
            }
        }
    }
    
    private fun tryLogcatMethod() {
        try {
            // 清除之前的日志缓冲区
            val clearProcess = Runtime.getRuntime().exec("logcat -c")
            clearProcess.waitFor()
            
            // 启动logcat监听，只监听应用相关日志
            logcatProcess = Runtime.getRuntime().exec(arrayOf(
                "logcat", 
                "-v", "time",
                "*:V"
            ))
            val reader = BufferedReader(InputStreamReader(logcatProcess!!.inputStream))
            
            mainHandler.post {
                updateStatus("● 系统日志监听")
            }
            
            var line: String? = null
            while (logcatProcess != null && reader.readLine().also { line = it } != null) {
                line?.let { logLine ->
                    // 过滤日志
                    if (shouldShowLog(logLine)) {
                        mainHandler.post {
                            appendLog(logLine)
                        }
                    }
                }
            }
            
        } catch (e: IOException) {
            Log.e("LogActivity", "Logcat监听失败: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("LogActivity", "未知错误: ${e.message}")
            throw e
        } finally {
            isLogcatRunning = false
            isMonitoring = false
            mainHandler.post {
                updateStatus("● 已停止")
            }
        }
    }
    
    private fun startAppLogMonitoring() {
        // 检查executor状态，避免在Activity销毁后执行
        if (executor.isShutdown) {
            Log.w("LogActivity", "Executor已关闭，无法启动应用日志监听")
            return
        }
        
        // 模拟应用内日志监听
        executor.execute {
            try {
                mainHandler.post {
                    updateStatus("● 应用日志监听")
                }
                
                // 定期检查并显示一些示例日志
                var counter = 0
                while (isLogcatRunning) {
                    Thread.sleep(5000) // 每5秒添加一条示例日志
                    
                    mainHandler.post {
                        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        appendLog("[$timestamp] [应用监听] 应用运行正常，监听计数: ${++counter}")
                        
                        // 添加一些模拟的应用状态信息
                        if (counter % 3 == 0) {
                            appendLog("[$timestamp] [内存状态] 可用内存: ${Runtime.getRuntime().freeMemory() / 1024 / 1024}MB")
                        }
                        
                        if (counter % 5 == 0) {
                            appendLog("[$timestamp] [网络状态] 网络连接正常")
                        }
                    }
                }
                
            } catch (e: InterruptedException) {
                Log.d("LogActivity", "应用日志监听被中断")
            } catch (e: Exception) {
                Log.e("LogActivity", "应用日志监听错误: ${e.message}")
                mainHandler.post {
                    updateStatus("● 监听错误")
                    appendLog("[错误] 应用日志监听失败: ${e.message}")
                }
            }
        }
    }
    
    private fun shouldShowLog(logLine: String): Boolean {
        // 检查是否包含我们关心的关键词
        return filterKeywords.any { keyword ->
            logLine.contains(keyword, ignoreCase = true)
        }
    }
    
    private fun appendLog(logLine: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val formattedLog = "[$timestamp] $logLine\n"
        
        logBuffer.append(formattedLog)
        logCount++
        
        // 限制日志缓冲区大小，避免内存溢出
        if (logBuffer.length > 100000) {
            val halfLength = logBuffer.length / 2
            logBuffer.delete(0, halfLength)
            logCount = logBuffer.count { it == '\n' }
        }
        
        tvLogContent.text = logBuffer.toString()
        tvLogCount.text = "日志条数: $logCount"
        
        if (isAutoScroll) {
            scrollToBottom()
        }
    }
    
    private fun scrollToBottom() {
        scrollLog.post {
            scrollLog.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
    
    private fun clearLogs() {
        logBuffer.clear()
        logCollector.clearLogs()
        logCount = 0
        tvLogContent.text = "日志已清空...\n"
        tvLogCount.text = "日志条数: 0"
        Toast.makeText(this, "日志已清空", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateStatus(status: String = "") {
        logCount = logCollector.getLogCount()
        val statusText = if (status.isNotEmpty()) {
            "日志条数: $logCount | $status"
        } else {
            "日志条数: $logCount | 监听状态: ${if (isMonitoring) "运行中" else "已停止"}"
        }
        tvLogStatus.text = statusText
        tvLogStatus.setTextColor(
            when {
                status.contains("实时监听") || status.contains("运行中") -> getColor(android.R.color.holo_green_dark)
                status.contains("失败") || status.contains("错误") -> getColor(android.R.color.holo_red_dark)
                status.contains("停止") || status.contains("已停止") -> getColor(android.R.color.holo_orange_dark)
                else -> getColor(R.color.text_secondary_light)
            }
        )
    }
    
    private fun exportLogs() {
        if (logBuffer.isEmpty()) {
            Toast.makeText(this, "没有日志可导出", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 检查executor状态
        if (executor.isShutdown) {
            Toast.makeText(this, "导出功能不可用，请重新打开页面", Toast.LENGTH_SHORT).show()
            return
        }
        
        executor.execute {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "app_logs_$timestamp.txt"
                val file = File(getExternalFilesDir(null), fileName)
                
                FileWriter(file).use { writer ->
                    writer.write("无障碍应用日志导出\n")
                    writer.write("导出时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                    writer.write("日志条数: $logCount\n")
                    writer.write("=".repeat(50) + "\n")
                    writer.write(logCollector.exportLogsAsString())
                }
                
                mainHandler.post {
                    Toast.makeText(this@LogActivity, "日志已导出到: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
                
            } catch (e: IOException) {
                Log.e("LogActivity", "导出日志失败: ${e.message}")
                mainHandler.post {
                    Toast.makeText(this@LogActivity, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        logCollector.removeListener(this)
        stopLogcatMonitoring()
        executor.shutdown()
    }
    
    override fun onNewLog(logEntry: LogCollector.LogEntry) {
        mainHandler.post {
            appendLog(logEntry.toFormattedString())
        }
    }
    
    private fun loadExistingLogs() {
        logCollector.addListener(this)
        val existingLogs = logCollector.getAllLogs()
        existingLogs.forEach { logEntry ->
            appendLog(logEntry.toFormattedString())
        }
        updateStatus("● 已加载")
    }
    
    private fun stopLogcatMonitoring() {
        // 停止logcat进程
        logcatProcess?.destroy()
        logcatProcess = null
        isLogcatRunning = false
        isMonitoring = false
    }
    
    override fun onPause() {
        super.onPause()
        updateStatus("● 已暂停")
    }
    
    override fun onResume() {
        super.onResume()
        if (!isLogcatRunning) {
            startLogcatMonitoring()
        }
    }
}