package com.incognitalk.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incognitalk.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// This UI model can be moved to its own file if it becomes more complex.
data class Chat(val id: String, val name: String, val lastMessage: String, val isNewUser: Boolean = false)

class HomeScreenViewModel(chatRepository: ChatRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _previousChats: StateFlow<List<Chat>> = chatRepository.getAllChats()
        .map { chatsWithMessagesList ->
            chatsWithMessagesList.map { chatWithMessages ->
                val lastMessage = chatWithMessages.messages.maxByOrNull { it.timestamp }?.content ?: ""
                Chat(
                    id = chatWithMessages.chat.chatName,
                    name = chatWithMessages.chat.chatName,
                    lastMessage = lastMessage,
                    isNewUser = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun findNewPeople(query: String): List<Chat> {
        if (query.equals("Charlie", ignoreCase = true)) {
            // Using the name as the ID for consistency with chats from the database
            return listOf(Chat(id = "Charlie", name = "Charlie", lastMessage = "New user", isNewUser = true))
        }
        return emptyList()
    }

    val searchResults: StateFlow<List<Chat>> =
        searchQuery.combine(_previousChats) { query, chats ->
            if (query.isBlank()) {
                chats
            } else {
                val newPeople = findNewPeople(query)
                val filteredChats = chats.filter {
                    it.name.contains(query, ignoreCase = true)
                }

                (newPeople + filteredChats).distinctBy { it.id }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // _previousChats will provide the initial value
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
