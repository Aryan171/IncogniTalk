package com.incognitalk.app.data.model.keystore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey
    val recipientId: String,
    val record: ByteArray
)
