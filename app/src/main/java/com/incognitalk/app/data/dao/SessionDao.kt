package com.incognitalk.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.incognitalk.app.data.model.keystore.Session

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: Session)

    @Query("SELECT * FROM session WHERE recipientId = :recipientId")
    suspend fun getSession(recipientId: String): Session?

    @Query("DELETE FROM session WHERE recipientId = :recipientId")
    suspend fun deleteSession(recipientId: String)

    @Query("DELETE FROM session WHERE recipientId LIKE :name || ':%'")
    suspend fun deleteAllSessionsForUser(name: String)

    @Query("SELECT recipientId FROM session WHERE recipientId LIKE :name || ':%'")
    suspend fun getSubDeviceSessionRecipients(name: String): List<String>
}
