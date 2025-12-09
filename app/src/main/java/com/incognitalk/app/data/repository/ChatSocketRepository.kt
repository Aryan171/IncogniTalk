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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Base64

object ChatSocketRepository {
    private val client = KtorClient.client.config {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }
    private var session: DefaultClientWebSocketSession? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        if (session != null) return
        scope.launch {
            currentUserId = userRepository.getUser().first()?.username ?: return@launch
            _isConnecting.value = true
            _connectionError.value = false
            try {
                client.webSocket(
                    method = io.ktor.http.HttpMethod.Get,
                    host = "10.0.2.2",
                    port = 8080,
                    path = "/chat/$currentUserId"
                ) {
                    session = this
                    _isConnecting.value = false

                    incoming.receiveAsFlow().collect { frame ->
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            if (text == "KEYS_DEPLETED") {
                                val newKeys = signalRepository.replenishKeys()
                                currentUserId?.let { authRepository.replenishKeys(it, newKeys) }
                            } else {
                                val webSocketMessage = Json.decodeFromString<WebSocketMessage>(text)
                                if (webSocketMessage.receiverId == currentUserId) {
                                    val encryptedMessage = Base64.getDecoder().decode(webSocketMessage.message)
                                    val decryptedMessage = signalRepository.decrypt(encryptedMessage, webSocketMessage.senderId, 1)

                                    val message = Message(
                                        chatOwnerName = webSocketMessage.senderId,
                                        content = decryptedMessage,
                                        timestamp = System.currentTimeMillis(),
                                        sender = webSocketMessage.senderId
                                    )
                                    chatRepository.insertMessage(message)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _connectionError.value = true
            } finally {
                _isConnecting.value = false
                session = null
            }
        }
    }

    fun sendMessage(senderId: String, receiverId: String, message: ByteArray) {
        scope.launch {
            if (session == null) {
                _connectionError.value = true
                return@launch
            }
            try {
                val webSocketMessage = WebSocketMessage(
                    senderId = senderId,
                    receiverId = receiverId,
                    message = Base64.getEncoder().encodeToString(message)
                )
                session?.sendSerialized(webSocketMessage)
            } catch (e: Exception) {
                _connectionError.value = true
            }
        }
    }

    fun stop() {
        scope.launch {
            session?.close()
            session = null
        }
    }
}
