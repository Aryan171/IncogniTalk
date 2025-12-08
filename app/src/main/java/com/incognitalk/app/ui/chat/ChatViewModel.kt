package com.incognitalk.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.incognitalk.app.data.repository.SignalRepository
import com.incognitalk.app.ui.model.MessageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    // A dummy list of messages for now.
    private val _messages = MutableStateFlow(listOf(
        MessageItem("Hey!", "10:00 AM", isFromMe = true),
        MessageItem("Hi there!", "10:01 AM", isFromMe = false),
        MessageItem("How are you?", "10:01 AM", isFromMe = true),
    ))
    val messages: StateFlow<List<MessageItem>> = _messages

    private val _newMessageText = MutableStateFlow("")
    val newMessageText: StateFlow<String> = _newMessageText

    fun onNewMessageChange(text: String) {
        _newMessageText.value = text
    }

    fun sendMessage() {
        if (_newMessageText.value.isNotBlank()) {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val currentTime = sdf.format(Date())

            val newMessage = MessageItem(
                content = _newMessageText.value,
                formattedTimestamp = currentTime,
                isFromMe = true
            )
            val currentMessages = _messages.value.toMutableList()
            currentMessages.add(newMessage)
            _messages.value = currentMessages
            _newMessageText.value = ""
        }
    }

    private val signalRepository = SignalRepository(application)

    init {
        viewModelScope.launch {
            signalRepository.initializeKeys()
        }
    }

    fun sendMessage(recipientId: String, message: String) {
        viewModelScope.launch {
            val encryptedMessage = signalRepository.encrypt(message, recipientId, 1) // Using a fixed device ID for now
            // TODO: Send the encrypted message to the server
        }
    }

    fun receiveMessage(senderId: String, encryptedMessage: ByteArray) {
        viewModelScope.launch {
            val decryptedMessage = signalRepository.decrypt(encryptedMessage, senderId, 1) // Using a fixed device ID for now
            // TODO: Display the decrypted message in the UI
        }
    }
}
