package com.qunar.barrier_free_qunar

import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.qunar.barrier_free_qunar.java.sdk.util.MarkdownUtil
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.linkify.LinkifyPlugin
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: MutableList<Message>) : 
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private var recyclerView: RecyclerView? = null
    
    // Markdown渲染器
    private var markwon: Markwon? = null
    
    private fun getMarkwon(): Markwon {
        if (markwon == null && recyclerView?.context != null) {
            markwon = Markwon.builder(recyclerView!!.context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(recyclerView!!.context))
                .usePlugin(LinkifyPlugin.create())
                .build()
        }
        return markwon ?: throw IllegalStateException("Markwon not initialized")
    }
    
    /**
     * 智能设置文本内容，如果包含Markdown语法则使用Markwon渲染，否则直接设置文本
     */
    private fun setTextContent(textView: TextView, content: String) {
        if (MarkdownUtil.containsMarkdown(content)) {
            try {
                getMarkwon().setMarkdown(textView, content)
            } catch (e: Exception) {
                // 如果Markwon渲染失败，回退到普通文本显示
                Log.w("ChatAdapter", "Markwon渲染失败，回退到普通文本: ${e.message}")
                textView.text = content
            }
        } else {
            textView.text = content
        }
    }
    
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
        Log.d("【ChatAdapter】添加消息", "消息类型: ${message.messageType}, 是否图片: ${message.isImageMessage()}, 图片数据: ${message.imageData}")
        messages.add(message)
        notifyItemInserted(messages.size - 1)
        Log.d("【ChatAdapter】添加消息", "消息已添加，当前消息总数: ${messages.size}")
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
        private val ivMultimedia: ImageView = itemView.findViewById(R.id.iv_multimedia)
        
        fun bind(message: Message) {
            // 智能渲染文本内容（支持Markdown）
            setTextContent(tvMessage, message.content)
            tvTime.text = dateFormat.format(Date(message.timestamp))
            // TODO: 加载头像
            ivAvatar.setImageResource(R.drawable.ic_person)
            
            // 处理多媒体内容
            if (message.isMultimediaMessage() && !message.imageData.isNullOrEmpty()) {
                ivMultimedia.visibility = View.VISIBLE
                // 使用Glide加载图片
                Glide.with(itemView.context)
                    .load(message.imageData)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                            Log.e("【SentViewHolder】Glide", "图片加载失败: ${e?.message}, URL: $model")
                            return false
                        }
                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("【SentViewHolder】Glide", "图片加载成功: URL: $model")
                            return false
                        }
                    })
                    .into(ivMultimedia)
            } else {
                ivMultimedia.visibility = View.GONE
            }
        }
    }
    
    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSenderName: TextView = itemView.findViewById(R.id.tv_username)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        private val ivMultimedia: ImageView = itemView.findViewById(R.id.iv_multimedia)
        
        private var currentAnimator: ValueAnimator? = null
        private var isStreamingMessage = false
        private var fullText: String = "" // 维护完整的文本内容
        
        fun bind(message: Message) {
            Log.d("【ReceivedViewHolder】绑定", "开始绑定消息: id=${message.id}, 类型=${message.messageType}")
            Log.d("【ReceivedViewHolder】绑定", "是否多媒体: ${message.isMultimediaMessage()}, 图片数据: ${message.imageData}")
            
            tvSenderName.text = message.senderName
            tvTime.text = dateFormat.format(Date(message.timestamp))
            // TODO: 加载头像
            ivAvatar.setImageResource(R.drawable.ic_person)
            
            // 直接设置文本内容，不使用动画，智能渲染Markdown
            fullText = message.content
            setTextContent(tvMessage, message.content)
            
            // 处理多媒体内容
            if (message.isMultimediaMessage() && !message.imageData.isNullOrEmpty()) {
                Log.d("【ReceivedViewHolder】多媒体", "显示多媒体内容，图片URL: ${message.imageData}")
                ivMultimedia.visibility = View.VISIBLE
                // 使用Glide加载图片
                Glide.with(itemView.context)
                    .load(message.imageData)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                            Log.e("【ReceivedViewHolder】Glide", "图片加载失败: ${e?.message}, URL: $model")
                            return false
                        }
                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("【ReceivedViewHolder】Glide", "图片加载成功: URL: $model")
                            return false
                        }
                    })
                    .into(ivMultimedia)
            } else {
                Log.d("【ReceivedViewHolder】多媒体", "隐藏多媒体视图，isMultimedia=${message.isMultimediaMessage()}, hasImageData=${!message.imageData.isNullOrEmpty()}")
                ivMultimedia.visibility = View.GONE
            }
        }
        
        fun bindWithTypewriterEffect(message: Message, isNewContent: Boolean = false) {
            tvSenderName.text = message.senderName
            tvTime.text = dateFormat.format(Date(message.timestamp))
            ivAvatar.setImageResource(R.drawable.ic_person)
            
            // 处理多媒体内容
            if (message.isMultimediaMessage() && !message.imageData.isNullOrEmpty()) {
                ivMultimedia.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.imageData)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(ivMultimedia)
            } else {
                ivMultimedia.visibility = View.GONE
            }
            
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
                        // 动画开始
                    }
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        isStreamingMessage = false
                        // 动画结束后智能渲染最终的文本内容（支持Markdown）
                        setTextContent(tvMessage, fullText)
                    }
                    override fun onAnimationCancel(animation: android.animation.Animator) {
                        isStreamingMessage = false
                        // 动画取消后智能渲染最终的文本内容（支持Markdown）
                        setTextContent(tvMessage, fullText)
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
                        // 动画开始
                    }
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        // 动画结束后智能渲染最终的文本内容（支持Markdown）
                        setTextContent(tvMessage, newFullText)
                    }
                    override fun onAnimationCancel(animation: android.animation.Animator) {
                        // 动画取消后智能渲染最终的文本内容（支持Markdown）
                        setTextContent(tvMessage, newFullText)
                    }
                    override fun onAnimationRepeat(animation: android.animation.Animator) {}
                })
                start()
            }
        }
    }
}