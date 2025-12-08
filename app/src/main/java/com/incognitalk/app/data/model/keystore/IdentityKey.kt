package com.incognitalk.app.data.model.keystore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class IdentityKey(
    @PrimaryKey val id: Int = 0,
    val keyPair: ByteArray,
    var registrationId: Int = 0
)
