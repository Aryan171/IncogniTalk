package com.incognitalk.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.incognitalk.app.data.model.keystore.KeyGenerationState

@Dao
interface KeyGenerationStateDao {
    @Query("SELECT * FROM key_generation_state WHERE id = 1")
    suspend fun getKeyGenerationState(): KeyGenerationState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: KeyGenerationState)
}
