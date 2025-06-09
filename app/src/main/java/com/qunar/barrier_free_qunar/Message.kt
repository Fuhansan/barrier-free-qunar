package com.qunar.barrier_free_qunar

data class Message(
    val id: String,
    val content: String,
    val senderId: String,
    val senderName: String,
    val timestamp: Long,
    val isSentByMe: Boolean,
    val avatarUrl: String? = null,
    // 多媒体相关字段
    val imageData: String? = null,
    val videoData: String? = null,
    val audioData: String? = null,
    val multimediaType: String? = null,
    val messageType: String? = null
) {
    
    // 判断消息类型的辅助方法
    fun isTextMessage(): Boolean = messageType == "text" || messageType == null
    fun isImageMessage(): Boolean = messageType == "image" || (!imageData.isNullOrEmpty())
    fun isVideoMessage(): Boolean = messageType == "video" || (!videoData.isNullOrEmpty())
    fun isAudioMessage(): Boolean = messageType == "audio" || (!audioData.isNullOrEmpty())
    fun isMultimediaMessage(): Boolean = messageType == "multimedia"
    fun hasMultimediaContent(): Boolean = !imageData.isNullOrEmpty() || !videoData.isNullOrEmpty() || !audioData.isNullOrEmpty()
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
        
        // 创建多媒体消息
        fun createMultimediaMessage(
            content: String,
            senderName: String,
            senderId: String,
            isSentByMe: Boolean,
            imageData: String? = null,
            videoData: String? = null,
            audioData: String? = null,
            multimediaType: String? = null,
            messageType: String? = null
        ): Message {
            return Message(
                id = System.currentTimeMillis().toString(),
                content = content,
                senderId = senderId,
                senderName = senderName,
                timestamp = System.currentTimeMillis(),
                isSentByMe = isSentByMe,
                imageData = imageData,
                videoData = videoData,
                audioData = audioData,
                multimediaType = multimediaType,
                messageType = messageType
            )
        }
    }
}