package com.incognitalk.app.data.repository

import com.incognitalk.app.data.model.WebSocketMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Base64

object ChatSocketRepository {

    private val client = HttpClient {
        install(WebSockets)
        install(ContentNegotiation) {
            json()
        }
    }

    private var session: DefaultClientWebSocketSession? = null
    private var connectionJob: Job? = null

    private val _incomingMessages = MutableSharedFlow<WebSocketMessage>()
    val incomingMessages: Flow<WebSocketMessage> = _incomingMessages.asSharedFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _connectionError = MutableStateFlow(false)
    val connectionError: StateFlow<Boolean> = _connectionError.asStateFlow()

    fun start() {
        if (connectionJob?.isActive == true) return

        connectionJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    _isConnecting.value = true
                    _connectionError.value = false

                    client.webSocket(host = "10.0.2.2", port = 8080, path = "/chat") {
                        session = this
                        _isConnecting.value = false

                        // Listen for incoming messages
                        for (frame in incoming) {
                            val message = receiveDeserialized<WebSocketMessage>()
                            _incomingMessages.emit(message)
                        }
                    }
                } catch (e: Exception) {
                    // Error occurred, will retry after delay
                } finally {
                    session = null
                    _isConnecting.value = false
                    if (isActive) { // Don't flag error if the job was cancelled intentionally
                        _connectionError.value = true
                        delay(5000) // Wait 5 seconds before retrying
                    }
                }
            }
        }
    }

    fun stop() {
        connectionJob?.cancel()
        connectionJob = null
        CoroutineScope(Dispatchers.IO).launch {
            session?.close()
            session = null
        }
        _connectionError.value = false
        _isConnecting.value = false
    }

    suspend fun sendMessage(senderId: String, receiverId: String, message: ByteArray) {
        val webSocketMessage = WebSocketMessage(
            senderId = senderId,
            receiverId = receiverId,
            message = Base64.getEncoder().encodeToString(message)
        )
        session?.sendSerialized(webSocketMessage)
    }
}