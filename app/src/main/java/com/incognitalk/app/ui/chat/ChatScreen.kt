package com.incognitalk.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.incognitalk.app.ui.model.MessageItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatName: String,
    onBackClick: () -> Unit,
    chatViewModel: ChatViewModel
) {
    val messages by chatViewModel.messages.collectAsState()
    val newMessageText by chatViewModel.newMessageText.collectAsState()
    val isConnecting by chatViewModel.isConnecting.collectAsState()
    val connectionError by chatViewModel.connectionError.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(chatName)
                        if (connectionError) {
                            Text(" - Server Down!", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "back button")
                    }
                },
                actions = {
                    if (isConnecting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(messages) { message ->
                    MessageBox(message)
                }
            }
            TextField(
                value = newMessageText,
                onValueChange = chatViewModel::onNewMessageChange,
                placeholder = { Text("Type a message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(32.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    if(newMessageText.isNotBlank()) {
                        IconButton(
                            onClick = { chatViewModel.sendMessage() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "send button",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MessageBox(
    message: MessageItem
) {
    val round = 24.dp
    val sharp = 4.dp

    val messageBoxShape = RoundedCornerShape(
        topStart = if (!message.isFromMe) sharp else round,
        topEnd = if (message.isFromMe) sharp else round,
        bottomStart = round,
        bottomEnd = round
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .shadow(elevation = 6.dp, shape = messageBoxShape)
                .background(
                    color = if (message.isFromMe) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.tertiaryContainer,
                    shape = messageBoxShape
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = if (message.isFromMe) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
