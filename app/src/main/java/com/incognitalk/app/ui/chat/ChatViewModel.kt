package com.incognitalk.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.incognitalk.app.data.repository.SignalRepository
import com.incognitalk.app.ui.model.MessageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val signalRepository = SignalRepository(application)

    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    val messages: StateFlow<List<MessageItem>> = _messages.asStateFlow()

    private val _newMessageText = MutableStateFlow("")
    val newMessageText: StateFlow<String> = _newMessageText.asStateFlow()

    init {
        viewModelScope.launch {
            signalRepository.initializeKeys()
        }
    }

    fun onNewMessageChange(text: String) {
        _newMessageText.value = text
    }

    fun sendMessage(recipientId: String) {
        val textToSend = _newMessageText.value
        if (textToSend.isBlank()) return

        viewModelScope.launch {
            val encryptedMessage = signalRepository.encrypt(textToSend, recipientId, 1) // Using a fixed device ID for now

            val sentMessage = MessageItem(
                content = textToSend,
                isFromMe = true
            )
            _messages.update { it + sentMessage }
            _newMessageText.value = ""

            // Simulate receiving the message back from the server
            receiveMessage(recipientId, encryptedMessage)
        }
    }

    private fun receiveMessage(senderId: String, encryptedMessage: ByteArray) {
        viewModelScope.launch {
            val decryptedMessage = signalRepository.decrypt(encryptedMessage, senderId, 1) // Using a fixed device ID for now

            val receivedMessage = MessageItem(
                content = "(Decrypted) $decryptedMessage",
                isFromMe = false
            )
            _messages.update { it + receivedMessage }
        }
    }
}