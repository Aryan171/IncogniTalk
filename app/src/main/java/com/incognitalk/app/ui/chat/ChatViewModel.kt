package com.incognitalk.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.model.Message
import com.incognitalk.app.data.model.User
import com.incognitalk.app.data.repository.ChatRepository
import com.incognitalk.app.data.repository.ChatSocketRepository
import com.incognitalk.app.data.repository.SignalRepository
import com.incognitalk.app.data.repository.UserRepository
import com.incognitalk.app.ui.model.MessageItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    application: Application,
    private val chatRepository: ChatRepository,
    private val chatName: String
) : AndroidViewModel(application) {

    private val signalRepository = SignalRepository(application)
    private val userRepository: UserRepository

    private val _newMessageText = MutableStateFlow("")
    val newMessageText: StateFlow<String> = _newMessageText.asStateFlow()

    val isConnecting: StateFlow<Boolean> = ChatSocketRepository.isConnecting
    val connectionError: StateFlow<Boolean> = ChatSocketRepository.connectionError

    private val user: StateFlow<User?>

    val messages: StateFlow<List<MessageItem>>

    init {
        val userDao = IncogniTalkDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)

        user = userRepository.getUser().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        messages = user.filterNotNull().flatMapLatest { currentUser ->
            chatRepository.getChatWithMessages(chatName).map { chatWithMessages ->
                chatWithMessages.messages.map {
                    MessageItem(
                        content = it.content,
                        isFromMe = it.sender == currentUser.username
                    )
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun onNewMessageChange(text: String) {
        _newMessageText.value = text
    }

    fun sendMessage() {
        val textToSend = _newMessageText.value
        val currentSenderId = user.value?.username

        if (textToSend.isBlank() || currentSenderId == null) return

        viewModelScope.launch {
            // FLAW: Device ID is hardcoded. This needs to be handled properly for multi-device support.
            val deviceId = 1
            val encryptedMessage = signalRepository.encrypt(textToSend, chatName, deviceId)

            val message = Message(
                chatOwnerName = chatName,
                content = textToSend,
                timestamp = System.currentTimeMillis(),
                sender = currentSenderId
            )
            chatRepository.insertMessage(message)

            _newMessageText.value = ""

            ChatSocketRepository.sendMessage(currentSenderId, chatName, encryptedMessage, deviceId)
        }
    }
}
