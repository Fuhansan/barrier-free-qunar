package com.qunar.barrier_free_qunar

data class ChatHistoryItem(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Long,
    val messageCount: Int
) {
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60000 -> "刚刚" // 小于1分钟
            diff < 3600000 -> "${diff / 60000}分钟前" // 小于1小时
            diff < 86400000 -> "${diff / 3600000}小时前" // 小于1天
            diff < 604800000 -> "${diff / 86400000}天前" // 小于1周
            else -> {
                val date = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
                date.format(java.util.Date(timestamp))
            }
        }
    }
}