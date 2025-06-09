package com.qunar.barrier_free_qunar

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Markdown功能测试Activity
 * 用于演示聊天界面的Markdown渲染功能
 */
class MarkdownTestActivity : AppCompatActivity() {
    
    private lateinit var rvMessages: RecyclerView
    private lateinit var btnAddMarkdown: Button
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_markdown_test)
        
        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadSampleMessages()
    }
    
    private fun initViews() {
        rvMessages = findViewById(R.id.rv_messages)
        btnAddMarkdown = findViewById(R.id.btn_add_markdown)
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        rvMessages.apply {
            layoutManager = LinearLayoutManager(this@MarkdownTestActivity)
            adapter = chatAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnAddMarkdown.setOnClickListener {
            addMarkdownSample()
        }
    }
    
    private fun loadSampleMessages() {
        // 添加一些示例消息
        val welcomeMessage = Message.createReceivedMessage(
            content = "欢迎使用Markdown聊天功能！下面是一些示例：",
            senderName = "系统",
            senderId = "system"
        )
        chatAdapter.addMessage(welcomeMessage)
        
        // 基本Markdown示例
        val basicMarkdown = Message.createReceivedMessage(
            content = """# 标题示例
## 二级标题
### 三级标题

**粗体文本** 和 *斜体文本*

~~删除线文本~~

`行内代码` 示例

```kotlin
// 代码块示例
fun hello() {
    println("Hello, Markdown!")
}
```

> 这是一个引用块
> 可以包含多行内容

- 无序列表项1
- 无序列表项2
- 无序列表项3

1. 有序列表项1
2. 有序列表项2
3. 有序列表项3

[链接示例](https://www.example.com)

---

表格示例：

| 列1 | 列2 | 列3 |
|-----|-----|-----|
| 数据1 | 数据2 | 数据3 |
| 数据4 | 数据5 | 数据6 |""",
            senderName = "AI助手",
            senderId = "assistant"
        )
        chatAdapter.addMessage(basicMarkdown)
        
        // 普通文本示例
        val plainText = Message.createSentMessage("这是普通文本，不包含Markdown语法")
        chatAdapter.addMessage(plainText)
        
        // 滚动到最新消息
        rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
    }
    
    private fun addMarkdownSample() {
        val samples = listOf(
            "**新的粗体消息** 刚刚添加！",
            "# 动态标题\n这是一个动态添加的标题消息",
            "`代码示例`: println(\"动态添加\")",
            "> 动态引用\n> 这是一个动态添加的引用块",
            "- 动态列表项1\n- 动态列表项2\n- 动态列表项3",
            "[动态链接](https://www.example.com) 刚刚创建",
            "~~这条消息被删除了~~ 但实际上没有",
            "*斜体强调*：Markdown功能正常工作！"
        )
        
        val randomSample = samples.random()
        val newMessage = Message.createReceivedMessage(
            content = randomSample,
            senderName = "AI助手",
            senderId = "assistant"
        )
        
        chatAdapter.addMessage(newMessage)
        rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
    }
}