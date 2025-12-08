package com.incognitalk.app.ui.chat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.incognitalk.app.data.repository.ChatRepository

class ChatViewModelFactory(private val application: Application, private val chatRepository: ChatRepository, private val chatName: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(application, chatRepository, chatName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
