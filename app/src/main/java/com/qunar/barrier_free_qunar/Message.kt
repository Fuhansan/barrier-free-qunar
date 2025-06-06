package com.qunar.barrier_free_qunar

data class Message(
    val id: String,
    val content: String,
    val senderId: String,
    val senderName: String,
    val timestamp: Long,
    val isSentByMe: Boolean,
    val avatarUrl: String? = null
) {
    companion object {
        fun createSentMessage(content: String): Message {
            return Message(
                id = System.currentTimeMillis().toString(),
                content = content,
                senderId = "me",
                senderName = "我",
                timestamp = System.currentTimeMillis(),
                isSentByMe = true
            )
        }
        
        fun createReceivedMessage(content: String, senderName: String, senderId: String): Message {
            return Message(
                id = System.currentTimeMillis().toString(),
                content = content,
                senderId = senderId,
                senderName = senderName,
                timestamp = System.currentTimeMillis(),
                isSentByMe = false
            )
        }
    }
}