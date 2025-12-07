package com.incognitalk.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import com.incognitalk.app.data.model.Message

@Dao
interface MessageDao {

    @Insert
    suspend fun insertMessage(message: Message)
}
