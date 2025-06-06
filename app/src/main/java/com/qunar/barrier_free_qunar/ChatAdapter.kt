package com.qunar.barrier_free_qunar

import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: MutableList<Message>) : 
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private var recyclerView: RecyclerView? = null
    
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }
    
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }
    
    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
    
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByMe) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent_modern, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received_modern, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> {
                // 检查是否是当前正在流式显示的消息
                val isCurrentStreamMessage = (position == getCurrentStreamMessageIndex())
                if (isCurrentStreamMessage) {
                    holder.bindWithTypewriterEffect(message, false)
                } else {
                    holder.bind(message)
                }
            }
        }
    }
    
    override fun getItemCount(): Int = messages.size
    
    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
    
    fun updateMessage(position: Int, message: Message) {
        if (position >= 0 && position < messages.size) {
            messages[position] = message
            notifyItemChanged(position)
        }
    }
    
    fun appendToMessage(position: Int, additionalContent: String) {
        if (position >= 0 && position < messages.size) {
            val existingMessage = messages[position]
            val updatedMessage = existingMessage.copy(
                content = existingMessage.content + additionalContent
            )
            messages[position] = updatedMessage
            notifyItemChanged(position)
        }
    }
    
    fun appendToMessageWithEffect(position: Int, additionalContent: String) {
        // Log.d("ChatAdapter", "appendToMessageWithEffect: position=$position, content='$additionalContent'")
        if (position >= 0 && position < messages.size) {
            val existingMessage = messages[position]
            val updatedMessage = existingMessage.copy(
                content = existingMessage.content + additionalContent
            )
            messages[position] = updatedMessage
            // Log.d("ChatAdapter", "更新消息内容: '${existingMessage.content}' -> '${updatedMessage.content}'")
            
            // 获取对应的ViewHolder并应用打字机效果
            val viewHolder = getCurrentViewHolder(position)
            // Log.d("ChatAdapter", "获取到的ViewHolder类型: ${viewHolder?.javaClass?.simpleName}")
            if (viewHolder is ReceivedMessageViewHolder) {
                // Log.d("ChatAdapter", "调用appendTextWithEffect: '$additionalContent'")
                viewHolder.appendTextWithEffect(additionalContent)
            } else {
                // Log.d("ChatAdapter", "ViewHolder不是ReceivedMessageViewHolder，使用notifyItemChanged")
                notifyItemChanged(position)
            }
        }
    }
    
    private fun getCurrentViewHolder(position: Int): RecyclerView.ViewHolder? {
        return recyclerView?.findViewHolderForAdapterPosition(position)
    }
    
    // 用于获取当前流式消息的索引，需要从ChatActivity传递
    private var currentStreamMessageIndex: Int = -1
    
    fun setCurrentStreamMessageIndex(index: Int) {
        currentStreamMessageIndex = index
    }
    
    fun getCurrentStreamMessageIndex(): Int {
        return currentStreamMessageIndex
    }
    
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        
        fun bind(message: Message) {
            tvMessage.text = message.content
            tvTime.text = dateFormat.format(Date(message.timestamp))
            // TODO: 加载头像
            ivAvatar.setImageResource(R.drawable.ic_person)
        }
    }
    
    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSenderName: TextView = itemView.findViewById(R.id.tv_username)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        
        private var currentAnimator: ValueAnimator? = null
        private var isStreamingMessage = false
        private var fullText: String = "" // 维护完整的文本内容
        
        fun bind(message: Message) {
            tvSenderName.text = message.senderName
            tvTime.text = dateFormat.format(Date(message.timestamp))
            // TODO: 加载头像
            ivAvatar.setImageResource(R.drawable.ic_person)
            
            // 直接设置文本内容，不使用动画
            fullText = message.content
            tvMessage.text = message.content
        }
        
        fun bindWithTypewriterEffect(message: Message, isNewContent: Boolean = false) {
            tvSenderName.text = message.senderName
            tvTime.text = dateFormat.format(Date(message.timestamp))
            ivAvatar.setImageResource(R.drawable.ic_person)
            
            fullText = message.content // 更新完整文本
            if (isNewContent || message.content.isNotEmpty()) {
                // 如果是新内容或者有内容，使用打字机效果
                startTypewriterEffect(message.content)
            } else {
                // 如果没有内容，直接显示空
                tvMessage.text = message.content
            }
        }
        
        private fun startTypewriterEffect(fullText: String) {
            // 取消之前的动画
            currentAnimator?.cancel()
            
            if (fullText.isEmpty()) {
                tvMessage.text = ""
                return
            }
            

            
            isStreamingMessage = true
            val duration = Math.max(fullText.length * 50L, 500L) // 每个字符50ms，最少500ms
            
            currentAnimator = ValueAnimator.ofInt(0, fullText.length).apply {
                this.duration = duration
                addUpdateListener { animator ->
                    val currentLength = animator.animatedValue as Int
                    if (currentLength <= fullText.length) {
                        tvMessage.text = fullText.substring(0, currentLength)
                    }
                }
                addListener(object : android.animation.Animator.AnimatorListener {
                    override fun onAnimationStart(animation: android.animation.Animator) {

                    }
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        isStreamingMessage = false
                        tvMessage.text = fullText
                    }
                    override fun onAnimationCancel(animation: android.animation.Animator) {
                        isStreamingMessage = false
                        tvMessage.text = fullText
                    }
                    override fun onAnimationRepeat(animation: android.animation.Animator) {}
                })
                start()
            }
        }
        
        fun appendTextWithEffect(additionalText: String) {
            // Log.d("ChatAdapter", "ReceivedMessageViewHolder.appendTextWithEffect: '$additionalText'")
            if (additionalText.isEmpty()) return
            
            // 使用维护的完整文本，而不是从TextView获取
            val currentText = fullText
            val newFullText = currentText + additionalText
            fullText = newFullText // 更新完整文本
            // Log.d("ChatAdapter", "当前文本: '$currentText', 新文本: '$newFullText'")
            
            // 如果当前正在播放动画，先取消
            currentAnimator?.cancel()
            
            // 从当前显示的文本长度开始，添加新文本的打字机效果
            val startLength = currentText.length
            val duration = Math.max(additionalText.length * 50L, 200L) // 每个字符50ms，最少200ms
            
            currentAnimator = ValueAnimator.ofInt(startLength, newFullText.length).apply {
                this.duration = duration
                addUpdateListener { animator ->
                    val currentLength = animator.animatedValue as Int
                    if (currentLength <= newFullText.length) {
                        tvMessage.text = newFullText.substring(0, currentLength)
                    }
                }
                addListener(object : android.animation.Animator.AnimatorListener {
                    override fun onAnimationStart(animation: android.animation.Animator) {

                    }
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        tvMessage.text = newFullText
                    }
                    override fun onAnimationCancel(animation: android.animation.Animator) {
                        tvMessage.text = newFullText
                    }
                    override fun onAnimationRepeat(animation: android.animation.Animator) {}
                })
                start()
            }
        }
    }
}