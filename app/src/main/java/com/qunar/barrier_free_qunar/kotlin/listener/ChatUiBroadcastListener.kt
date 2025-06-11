package com.qunar.barrier_free_qunar.kotlin.listener

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.qunar.barrier_free_qunar.ChatAdapter
import com.qunar.barrier_free_qunar.java.sdk.broadcast.BroadcastListener
import com.qunar.barrier_free_qunar.java.sdk.model.broadcast.MessageData

class ChatUiBroadcastListener(
    private val chatAdapter: ChatAdapter,
    private val rvMessages: RecyclerView
):BroadcastListener {

    private  val TAG = "【ChatUiBroadcastListener】广播"

    override fun onMessageReceived(messageData: MessageData?) {
        Log.w(TAG, "接收到了消息.................................")
    }

    override fun primaryKey(): String? {
       return "chat_ui_broad_cast";
    }
}