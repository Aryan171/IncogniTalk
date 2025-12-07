package com.incognitalk.app.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithMessages(
    @Embedded val chat: Chat,
    @Relation(
        parentColumn = "chatName",
        entityColumn = "chatOwnerName"
    )
    val messages: List<Message>
)
