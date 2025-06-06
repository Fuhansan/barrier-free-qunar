package com.qunar.barrier_free_qunar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatHistoryAdapter(
    private val chatHistoryList: List<ChatHistoryItem>,
    private val onItemClick: (ChatHistoryItem) -> Unit
) : RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_history, parent, false)
        return ChatHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatHistoryViewHolder, position: Int) {
        val item = chatHistoryList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = chatHistoryList.size

    inner class ChatHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_chat_title)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvMessageCount: TextView = itemView.findViewById(R.id.tv_message_count)

        fun bind(item: ChatHistoryItem) {
            tvTitle.text = item.title
            tvLastMessage.text = item.lastMessage
            tvTime.text = item.getFormattedTime()
            tvMessageCount.text = "${item.messageCount}条消息"

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}