package com.incognitalk.app.data.repository

import android.content.Context
import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.model.keystore.IdentityKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.util.KeyHelper
import kotlin.text.Charsets


class SignalRepository(private val context: Context) {
    private val database = IncogniTalkDatabase.getDatabase(context)
    private val store = RoomSignalProtocolStore(database)

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
    /*
    * TODO
    * This function must be completed for the app to run properly
    * */
    private fun getPreKeyBundleFromServer(recipientId: String, deviceId: Int): PreKeyBundle {
        val identityKey = KeyHelper.generateIdentityKeyPair()
        return PreKeyBundle(0, deviceId, 0, identityKey.publicKey.publicKey, 0, identityKey.publicKey.publicKey, ByteArray(0), identityKey.publicKey)
    }
}