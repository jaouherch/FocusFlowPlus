package com.example.focusflowplus.data.remote

data class ClaudeRequest(
    val model: String = "claude-sonnet-4-20250514",
    val max_tokens: Int = 1024,
    val system: String = "",
    val messages: List<ClaudeMessage>
)

data class ClaudeMessage(
    val role: String,
    val content: String
)

data class ClaudeResponse(
    val content: List<ClaudeContent>
)

data class ClaudeContent(
    val type: String = "",
    val text: String = ""
)