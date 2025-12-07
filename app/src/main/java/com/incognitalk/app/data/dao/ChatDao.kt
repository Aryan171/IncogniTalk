package com.incognitalk.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.incognitalk.app.data.model.Chat
import com.incognitalk.app.data.model.ChatWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChat(chat: Chat)

    @Transaction
    @Query("SELECT * FROM chats")
    fun getAllChats(): Flow<List<ChatWithMessages>>

    @Transaction
    @Query("SELECT * FROM chats WHERE chatName = :chatName")
    fun getChatWithMessages(chatName: String): Flow<ChatWithMessages>
}
