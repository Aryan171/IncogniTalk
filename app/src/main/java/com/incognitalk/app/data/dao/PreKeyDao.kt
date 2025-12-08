package com.incognitalk.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.incognitalk.app.data.model.keystore.PreKey

@Dao
interface PreKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preKey: PreKey)

    @Query("SELECT * FROM prekey WHERE preKeyId = :preKeyId")
    suspend fun getPreKey(preKeyId: Int): PreKey?

    @Query("DELETE FROM prekey WHERE preKeyId = :preKeyId")
    suspend fun deletePreKey(preKeyId: Int)
}
