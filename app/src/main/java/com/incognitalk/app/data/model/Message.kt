package com.incognitalk.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = Chat::class,
        parentColumns = ["chatName"],
        childColumns = ["chatOwnerName"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["chatOwnerName"])]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val messageId: Int = 0,
    val chatOwnerName: String,
    val content: String,
    val timestamp: Long,
    val sender: String // e.g., "me" or the other person's name
)
