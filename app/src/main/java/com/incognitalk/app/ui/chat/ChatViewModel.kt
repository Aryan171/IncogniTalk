package com.incognitalk.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.model.Message
import com.incognitalk.app.data.network.ApiServiceImpl
import com.incognitalk.app.data.network.KtorClient
import com.incognitalk.app.data.repository.AuthRepository
import com.incognitalk.app.data.repository.ChatRepository
import com.incognitalk.app.data.repository.ChatSocketRepository
import com.incognitalk.app.data.repository.SignalRepository
import com.incognitalk.app.data.repository.UserRepository
import com.incognitalk.app.ui.model.MessageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Base64

class ChatViewModel(
    application: Application,
    private val chatRepository: ChatRepository,
    private val chatName: String
) : AndroidViewModel(application) {

    private val signalRepository = SignalRepository(application)
    private val authRepository = AuthRepository(ApiServiceImpl(KtorClient.client))
    private val userRepository: UserRepository

    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    val messages: StateFlow<List<MessageItem>> = _messages.asStateFlow()

    private val _newMessageText = MutableStateFlow("")
    val newMessageText: StateFlow<String> = _newMessageText.asStateFlow()

    val isConnecting: StateFlow<Boolean> = ChatSocketRepository.isConnecting
    val connectionError: StateFlow<Boolean> = ChatSocketRepository.connectionError

    private val senderId: String

    init {
        val userDao = IncogniTalkDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
        senderId = runBlocking { userRepository.getUser().first()?.username ?: "" }

        ChatSocketRepository.start()

        chatRepository.getChatWithMessages(chatName)
            .map { chatWithMessages ->
                chatWithMessages.messages.map {
                    MessageItem(
                        content = it.content,
                        isFromMe = it.sender == senderId
                    )
                }
            }
            .onEach { _messages.value = it }
            .launchIn(viewModelScope)

        ChatSocketRepository.incomingMessages
            .onEach { webSocketMessage ->
                if (webSocketMessage.message == "KEYS_DEPLETED") {
                    val newKeys = signalRepository.replenishKeys()
                    authRepository.replenishKeys(senderId, newKeys)
                } else if (webSocketMessage.receiverId == senderId && webSocketMessage.senderId == chatName) {
                    val encryptedMessage = Base64.getDecoder().decode(webSocketMessage.message)
                    receiveMessage(webSocketMessage.senderId, encryptedMessage)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onNewMessageChange(text: String) {
        _newMessageText.value = text
    }

    fun sendMessage() {
        val textToSend = _newMessageText.value
        if (textToSend.isBlank()) return

        viewModelScope.launch {
            val encryptedMessage = signalRepository.encrypt(textToSend, chatName, 1) // Using a fixed device ID for now

            val message = Message(
                chatOwnerName = chatName,
                content = textToSend,
                timestamp = System.currentTimeMillis(),
                sender = senderId
            )
            chatRepository.insertMessage(message)
            
            _newMessageText.value = ""

            ChatSocketRepository.sendMessage(senderId, chatName, encryptedMessage)
        }
    }

    private fun receiveMessage(sender: String, encryptedMessage: ByteArray) {
        viewModelScope.launch {
            val decryptedMessage = signalRepository.decrypt(encryptedMessage, sender, 1) // Using a fixed device ID for now

            val message = Message(
                chatOwnerName = chatName,
                content = decryptedMessage,
                timestamp = System.currentTimeMillis(),
                sender = sender
            )
            chatRepository.insertMessage(message)
        }
    }
}
