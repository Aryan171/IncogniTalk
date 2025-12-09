package com.incognitalk.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketMessage(
    val senderId: String,
    val receiverId: String,
    val message: String,
    val deviceId: Int
)
