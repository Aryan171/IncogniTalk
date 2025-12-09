package com.incognitalk.app.data.repository

import android.content.Context
import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.model.Message
import com.incognitalk.app.data.model.WebSocketMessage
import com.incognitalk.app.data.network.ApiServiceImpl
import com.incognitalk.app.data.network.KtorClient
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Base64

object ChatSocketRepository {
    private const val WS_HOST = "10.0.2.2"
    private const val WS_PORT = 8080
    private const val MSG_KEYS_DEPLETED = "KEYS_DEPLETED"
    private const val RECONNECT_DELAY_MS = 5000L

    private val client = KtorClient.client.config {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }
    private var session: DefaultClientWebSocketSession? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var connectionJob: Job? = null

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val _connectionError = MutableStateFlow(false)
    val connectionError = _connectionError.asStateFlow()

    private lateinit var signalRepository: SignalRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var chatRepository: ChatRepository
    private var currentUserId: String? = null

    fun init(context: Context) {
        if (this::signalRepository.isInitialized) return // Avoid re-initialization

        val application = context.applicationContext
        signalRepository = SignalRepository(application)
        authRepository = AuthRepository(ApiServiceImpl(KtorClient.client))

        val db = IncogniTalkDatabase.getDatabase(application)
        userRepository = UserRepository(db.userDao())
        chatRepository = ChatRepository(db.chatDao(), db.messageDao())
    }

    fun start() {
        synchronized(this) {
            if (connectionJob?.isActive == true) return

            connectionJob = scope.launch {
                currentUserId = userRepository.getUser().first()?.username ?: return@launch

                while (isActive) {
                    try {
                        _isConnecting.value = true
                        _connectionError.value = false
                        client.webSocket(
                            method = io.ktor.http.HttpMethod.Get,
                            host = WS_HOST,
                            port = WS_PORT,
                            path = "/chat/$currentUserId"
                        ) {
                            session = this
                            _isConnecting.value = false
                            listenForMessages()
                        }
                    } catch (e: Exception) {
                        // Likely a connection error
                        _connectionError.value = true
                    } finally {
                        _isConnecting.value = false
                        session = null
                        delay(RECONNECT_DELAY_MS)
                    }
                }
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.listenForMessages() {
        try {
            incoming.receiveAsFlow().collect { frame ->
                if (frame is Frame.Text) {
                    try {
                        val text = frame.readText()
                        if (text == MSG_KEYS_DEPLETED) {
                            handleKeysDepleted()
                        } else {
                            handleIncomingMessage(text)
                        }
                    } catch (e: Exception) {
                        // Error processing a single message, but don't kill the connection
                    }
                }
            }
        } catch (e: Exception) {
            // Error receiving from the channel, triggers reconnection
            throw e
        }
    }

    private suspend fun handleKeysDepleted() {
        val newKeys = signalRepository.replenishKeys()
        currentUserId?.let { authRepository.replenishKeys(it, newKeys) }
    }

    private suspend fun handleIncomingMessage(text: String) {
        val webSocketMessage = Json.decodeFromString<WebSocketMessage>(text)
        if (webSocketMessage.receiverId == currentUserId) {
            val encryptedMessage = Base64.getDecoder().decode(webSocketMessage.message)
            val decryptedMessage = signalRepository.decrypt(encryptedMessage, webSocketMessage.senderId, webSocketMessage.deviceId)

            val message = Message(
                chatOwnerName = webSocketMessage.senderId,
                content = decryptedMessage,
                timestamp = System.currentTimeMillis(),
                sender = webSocketMessage.senderId
            )
            chatRepository.insertMessage(message)
        }
    }

    fun sendMessage(senderId: String, receiverId: String, message: ByteArray, deviceId: Int) {
        scope.launch {
            if (session == null) {
                _connectionError.value = true
                return@launch
            }
            try {
                val webSocketMessage = WebSocketMessage(
                    senderId = senderId,
                    receiverId = receiverId,
                    message = Base64.getEncoder().encodeToString(message),
                    deviceId = deviceId
                )
                session?.sendSerialized(webSocketMessage)
            } catch (e: Exception) {
                _connectionError.value = true
            }
        }
    }

    fun stop() {
        connectionJob?.cancel()
        connectionJob = null
        scope.launch {
            session?.close()
            session = null
        }
    }
}
