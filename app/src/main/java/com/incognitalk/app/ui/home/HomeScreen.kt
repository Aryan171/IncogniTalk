package com.incognitalk.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onInfoClick: () -> Unit = {},
    onChatClick: (String) -> Unit = {},
    homeScreenViewModel: HomeScreenViewModel = viewModel()
) {
    val searchQuery by homeScreenViewModel.searchQuery.collectAsState()
    val searchResults by homeScreenViewModel.searchResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IncogniTalk", color = MaterialTheme.colorScheme.primary) },
                actions = {
                    IconButton(onClick = onInfoClick) {
                        Icon(Icons.Outlined.Info, contentDescription = "Info")
                    }
                }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            TextField(
                value = searchQuery,
                onValueChange = homeScreenViewModel::onSearchQueryChange,
                placeholder = { Text("Search or start a new chat") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = "Search Icon")
                }
            )

            if (searchResults.isEmpty()) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Welcome to IncogniTalk! Search for someone to start a conversation.",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Text(
                        text = "No results found.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn {
                    items(searchResults) { chat ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChatClick(chat.name) }
                                .padding(16.dp)
                        ) {
                            if (chat.isNewUser) {
                                Text("Start a new chat", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(text = chat.name)
                            Text(text = chat.lastMessage)
                        }
                    }
                }
            }
        }
    }
}
