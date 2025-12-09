package com.incognitalk.app.data.model.keystore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "key_generation_state")
data class KeyGenerationState(
    @PrimaryKey val id: Int = 1, // Singleton entry
    val lastPreKeyId: Int,
    val lastSignedPreKeyId: Int
)
