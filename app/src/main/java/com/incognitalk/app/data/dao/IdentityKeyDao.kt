package com.incognitalk.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.incognitalk.app.data.model.keystore.IdentityKey

@Dao
interface IdentityKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(identityKey: IdentityKey)

    @Query("SELECT * FROM identitykey WHERE id = 0")
    suspend fun getIdentityKey(): IdentityKey?

    @Query("SELECT registrationId FROM identitykey WHERE id = 0")
    suspend fun getRegistrationId(): Int?
}
