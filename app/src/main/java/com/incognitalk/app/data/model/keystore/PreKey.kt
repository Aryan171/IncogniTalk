package com.incognitalk.app.data.model.keystore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PreKey(
    @PrimaryKey
    val preKeyId: Int,
    val record: ByteArray
)
