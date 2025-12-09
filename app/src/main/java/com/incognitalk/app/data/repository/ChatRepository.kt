package com.incognitalk.app.data.repository

import com.incognitalk.app.data.dao.ChatDao
import com.incognitalk.app.data.dao.MessageDao
import com.incognitalk.app.data.model.Chat
import com.incognitalk.app.data.model.ChatWithMessages
import com.incognitalk.app.data.model.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao, private val messageDao: MessageDao) {

    fun getAllChats(): Flow<List<ChatWithMessages>> = chatDao.getAllChats()

    fun getChatWithMessages(chatName: String): Flow<ChatWithMessages> {
        return chatDao.getChatWithMessages(chatName)
    }

    suspend fun insertChat(chat: Chat) {
        chatDao.insertChat(chat)
    }

    suspend fun insertMessage(message: Message) {
        // Ensure the parent chat exists before inserting the message.
        // Since the DAO uses OnConflictStrategy.IGNORE, this is safe to call every time.
        chatDao.insertChat(Chat(name = message.chatOwnerName))
        messageDao.insertMessage(message)
    }
}
