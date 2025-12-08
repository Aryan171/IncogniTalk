package com.incognitalk.app.ui.chat

import androidx.lifecycle.ViewModel
import com.incognitalk.app.ui.model.MessageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatScreenViewModel : ViewModel() {

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
}
