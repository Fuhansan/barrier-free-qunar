package com.qunar.barrier_free_qunar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DrawerChatHistoryAdapter(
    private val chatHistoryList: List<ChatHistoryItem>,
    private val onItemClick: (ChatHistoryItem) -> Unit
) : RecyclerView.Adapter<DrawerChatHistoryAdapter.DrawerChatHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerChatHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drawer_chat_history, parent, false)
        return DrawerChatHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrawerChatHistoryViewHolder, position: Int) {
        val item = chatHistoryList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = chatHistoryList.size

    inner class DrawerChatHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_chat_title)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvMessageCount: TextView = itemView.findViewById(R.id.tv_message_count)

        fun bind(item: ChatHistoryItem) {
            tvTitle.text = item.title
            tvTime.text = item.getFormattedTime()
            tvMessageCount.text = "${item.messageCount}条"

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}