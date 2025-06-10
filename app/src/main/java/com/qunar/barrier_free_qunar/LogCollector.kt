package com.qunar.barrier_free_qunar

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 应用内日志收集器
 * 用于收集和管理应用运行时的日志信息
 */
class LogCollector private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: LogCollector? = null
        
        fun getInstance(): LogCollector {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LogCollector().also { INSTANCE = it }
            }
        }
    }
    
    // 日志条目数据类
    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val tag: String,
        val message: String
    ) {
        fun toFormattedString(): String {
            val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
            return "[$timeStr] [$level] $tag: $message"
        }
    }
    
    // 日志监听器接口
    interface LogListener {
        fun onNewLog(logEntry: LogEntry)
    }
    
    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private val listeners = mutableSetOf<LogListener>()
    private val maxLogCount = 1000 // 最大保存日志条数
    
    /**
     * 添加日志监听器
     */
    fun addListener(listener: LogListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }
    
    /**
     * 移除日志监听器
     */
    fun removeListener(listener: LogListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }
    
    /**
     * 记录调试日志
     */
    fun d(tag: String, message: String) {
        addLog("DEBUG", tag, message)
        Log.d(tag, message)
    }
    
    /**
     * 记录信息日志
     */
    fun i(tag: String, message: String) {
        addLog("INFO", tag, message)
        Log.i(tag, message)
    }
    
    /**
     * 记录警告日志
     */
    fun w(tag: String, message: String) {
        addLog("WARN", tag, message)
        //Log.w(tag, message)
    }
    
    /**
     * 记录错误日志
     */
    fun e(tag: String, message: String) {
        addLog("ERROR", tag, message)
        ////Log.tag, message)
    }
    
    /**
     * 记录错误日志（带异常）
     */
    fun e(tag: String, message: String, throwable: Throwable) {
        val fullMessage = "$message\n${Log.getStackTraceString(throwable)}"
        addLog("ERROR", tag, fullMessage)
        ////Log.tag, message, throwable)
    }
    
    /**
     * 添加日志条目
     */
    private fun addLog(level: String, tag: String, message: String) {
        val logEntry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message
        )
        
        // 添加到队列
        logQueue.offer(logEntry)
        
        // 限制队列大小
        while (logQueue.size > maxLogCount) {
            logQueue.poll()
        }
        
        // 通知监听器
        synchronized(listeners) {
            listeners.forEach { listener ->
                try {
                    listener.onNewLog(logEntry)
                } catch (e: Exception) {
                    //Log."LogCollector", "通知监听器失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 获取所有日志
     */
    fun getAllLogs(): List<LogEntry> {
        return logQueue.toList()
    }
    
    /**
     * 清空日志
     */
    fun clearLogs() {
        logQueue.clear()
    }
    
    /**
     * 获取日志数量
     */
    fun getLogCount(): Int {
        return logQueue.size
    }
    
    /**
     * 导出日志为字符串
     */
    fun exportLogsAsString(): String {
        val sb = StringBuilder()
        sb.append("无障碍应用日志导出\n")
        sb.append("导出时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("日志条数: ${logQueue.size}\n")
        sb.append("=".repeat(50) + "\n")
        
        logQueue.forEach { logEntry ->
            sb.append(logEntry.toFormattedString()).append("\n")
        }
        
        return sb.toString()
    }
}