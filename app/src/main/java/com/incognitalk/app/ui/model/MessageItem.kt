package com.incognitalk.app.ui.model

data class MessageItem(
    val content: String,
    val formattedTimestamp: String,
    val isFromMe: Boolean
)
