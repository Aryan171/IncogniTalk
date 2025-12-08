package com.incognitalk.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.incognitalk.app.data.model.keystore.RemoteIdentity

@Dao
interface RemoteIdentityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteIdentity: RemoteIdentity)

    @Query("SELECT * FROM remoteidentity WHERE address = :address")
    suspend fun getRemoteIdentity(address: String): RemoteIdentity?
}
