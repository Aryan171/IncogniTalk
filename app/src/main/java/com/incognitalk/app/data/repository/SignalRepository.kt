package com.incognitalk.app.data.repository

import android.content.Context
import android.util.Base64
import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.model.keystore.IdentityKey
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.util.KeyHelper
import kotlin.text.Charsets

@Serializable
data class PreKeyBundleDto(
    val registrationId: Int,
    val deviceId: Int,
    val preKeyId: Int,
    val preKeyPublic: String, // Base64 encoded
    val signedPreKeyId: Int,
    val signedPreKeyPublic: String, // Base64 encoded
    val signedPreKeySignature: String, // Base64 encoded
    val identityKey: String // Base64 encoded
)

class SignalRepository(private val context: Context) {
    private val database = IncogniTalkDatabase.getDatabase(context)
    private val store = RoomSignalProtocolStore(database)

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun initializeKeys() {
        withContext(Dispatchers.IO) {
            if (database.identityKeyDao().getIdentityKey() == null) {
                val registrationId = KeyHelper.generateRegistrationId(false)
                val identityKeyPair = KeyHelper.generateIdentityKeyPair()
                val preKeys = KeyHelper.generatePreKeys(0, 100)
                val signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 0)

                database.identityKeyDao().insert(
                    IdentityKey(
                        keyPair = identityKeyPair.serialize(),
                        registrationId = registrationId
                    )
                )

                preKeys.forEach { preKey ->
                    store.storePreKey(preKey.id, preKey)
                }

                store.storeSignedPreKey(signedPreKey.id, signedPreKey)
            }
        }
    }

    suspend fun encrypt(message: String, recipientId: String, deviceId: Int): ByteArray = withContext(Dispatchers.IO) {
        val address = SignalProtocolAddress(recipientId, deviceId)
        val sessionCipher = SessionCipher(store, address)

        if (!store.containsSession(address)) {
            val preKeyBundle = getPreKeyBundleFromServer(recipientId, deviceId)
            val sessionBuilder = SessionBuilder(store, address)
            sessionBuilder.process(preKeyBundle)
        }

        val ciphertext = sessionCipher.encrypt(message.toByteArray(Charsets.UTF_8))
        ciphertext.serialize()
    }

    suspend fun decrypt(message: ByteArray, senderId: String, deviceId: Int): String = withContext(Dispatchers.IO) {
        val address = SignalProtocolAddress(senderId, deviceId)
        val sessionCipher = SessionCipher(store, address)

        val decryptedMessage = try {
            // First, try to decrypt as a PreKeySignalMessage (for session setup)
            val preKeySignalMessage = PreKeySignalMessage(message)
            sessionCipher.decrypt(preKeySignalMessage)
        } catch (e: InvalidMessageException) {
            // If that fails, it's likely a regular SignalMessage (for an established session)
            val signalMessage = SignalMessage(message)
            sessionCipher.decrypt(signalMessage)
        }

        String(decryptedMessage, Charsets.UTF_8)
    }

    private suspend fun getPreKeyBundleFromServer(recipientId: String, deviceId: Int): PreKeyBundle {
        // 10.0.2.2 is the special address for Android Emulator to connect to the host machine's localhost
        val dto = client.get("http://10.0.2.2:8080/keys/$recipientId/$deviceId").body<PreKeyBundleDto>()

        val preKeyPublic = Base64.decode(dto.preKeyPublic, Base64.DEFAULT)
        val signedPreKeyPublic = Base64.decode(dto.signedPreKeyPublic, Base64.DEFAULT)
        val signedPreKeySignature = Base64.decode(dto.signedPreKeySignature, Base64.DEFAULT)
        val identityKey = Base64.decode(dto.identityKey, Base64.DEFAULT)

        return PreKeyBundle(
            dto.registrationId,
            dto.deviceId,
            dto.preKeyId,
            Curve.decodePoint(preKeyPublic, 0),
            dto.signedPreKeyId,
            Curve.decodePoint(signedPreKeyPublic, 0),
            signedPreKeySignature,
            org.whispersystems.libsignal.IdentityKey(identityKey, 0)
        )
    }
}