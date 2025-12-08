package com.incognitalk.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.incognitalk.app.data.model.keystore.SignedPreKey

@Dao
interface SignedPreKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signedPreKey: SignedPreKey)

    @Query("SELECT * FROM signedprekey WHERE preKeyId = :preKeyId")
    suspend fun getSignedPreKey(preKeyId: Int): SignedPreKey?
    
    @Query("SELECT * FROM signedprekey")
    suspend fun getAllSignedPreKeys(): List<SignedPreKey>

    @Query("DELETE FROM signedprekey WHERE preKeyId = :preKeyId")
    suspend fun deleteSignedPreKey(preKeyId: Int)
}
