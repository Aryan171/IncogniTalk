package com.incognitalk.app.data.model.keystore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RemoteIdentity(
    @PrimaryKey
    val address: String, // "recipientId:deviceId"
    val identityKey: ByteArray
)
