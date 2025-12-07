package com.incognitalk.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// A dummy data class for chat items.
// In a real app, you would have a more complex model.
data class Chat(val id: String, val name: String, val lastMessage: String)

class HomeScreenViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // A dummy list of previous chats.
    private val _previousChats = MutableStateFlow(
        listOf(
            Chat("1", "Alice", "Hey!"),
            Chat("2", "Bob", "See you tomorrow.")
        )
    )

    // A dummy function to simulate searching for new people.
    private fun findNewPeople(query: String): List<Chat> {
        if (query.equals("Charlie", ignoreCase = true)) {
            return listOf(Chat("3", "Charlie", "New user"))
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
            initialValue = _previousChats.value
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
