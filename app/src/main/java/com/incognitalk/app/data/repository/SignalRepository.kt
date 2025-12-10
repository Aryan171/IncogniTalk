package com.incognitalk.app.data.repository

import android.content.Context
import android.util.Base64
import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.model.PreKeySummary
import com.incognitalk.app.data.model.RegistrationBundle
import com.incognitalk.app.data.model.SignedPreKeySummary
import com.incognitalk.app.data.model.keystore.IdentityKey
import com.incognitalk.app.data.model.keystore.KeyGenerationState
import com.incognitalk.app.data.network.ApiServiceImpl
import com.incognitalk.app.data.network.KtorClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.util.KeyHelper
import kotlin.text.Charsets

class SignalRepository(private val context: Context) {
    private val database = IncogniTalkDatabase.getDatabase(context)
    private val store = RoomSignalProtocolStore(database)
    private val apiService = ApiServiceImpl(KtorClient.client)
    private val preKeyBundleRepository = PreKeyBundleRepository(apiService)
    private val keyGenerationStateDao = database.keyGenerationStateDao()

    suspend fun initializeKeys() {
        withContext(Dispatchers.IO) {
            if (database.identityKeyDao().getIdentityKey() == null) {
                val registrationId = KeyHelper.generateRegistrationId(false)
                val identityKeyPair = KeyHelper.generateIdentityKeyPair()

                val preKeys = KeyHelper.generatePreKeys(0, 100)
                // Generate only one signed pre-key with ID 1
                val signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 1)

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

                keyGenerationStateDao.insertOrUpdate(KeyGenerationState(lastPreKeyId = 100, lastSignedPreKeyId = 1))
            }
        }
    }

    suspend fun replenishPreKeys(): List<PreKeySummary> = withContext(Dispatchers.IO) {
        val keyGenerationState = keyGenerationStateDao.getKeyGenerationState()!!
        val newPreKeys = KeyHelper.generatePreKeys(keyGenerationState.lastPreKeyId, 100)

        newPreKeys.forEach { store.storePreKey(it.id, it) }

        keyGenerationStateDao.insertOrUpdate(
            keyGenerationState.copy(lastPreKeyId = keyGenerationState.lastPreKeyId + 100)
        )

        newPreKeys.map { it.toSummary() }
    }

    suspend fun rotateSignedPreKey(): SignedPreKeySummary = withContext(Dispatchers.IO) {
        val identityKeyPair = IdentityKeyPair(store.identityKeyPair.serialize())
        val keyGenerationState = keyGenerationStateDao.getKeyGenerationState()!!

        val newSignedPreKeyId = keyGenerationState.lastSignedPreKeyId + 1
        val newSignedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, newSignedPreKeyId)

        store.storeSignedPreKey(newSignedPreKey.id, newSignedPreKey)

        // Remove the old one
        store.removeSignedPreKey(keyGenerationState.lastSignedPreKeyId)

        keyGenerationStateDao.insertOrUpdate(
            keyGenerationState.copy(lastSignedPreKeyId = newSignedPreKeyId)
        )

        newSignedPreKey.toSummary()
    }


    suspend fun getRegistrationBundle(): RegistrationBundle = withContext(Dispatchers.IO) {
        val identityKeyRecord = database.identityKeyDao().getIdentityKey()!!
        val identityKeyPair = IdentityKeyPair(identityKeyRecord.keyPair)
        val preKeys = (0..99).map { store.loadPreKey(it) }
        val signedPreKey = store.loadSignedPreKey(1)
        val deviceId = 1 // FLAW: Hardcoded device ID

        RegistrationBundle(
            identityKey = Base64.encodeToString(identityKeyPair.publicKey.serialize(), Base64.NO_WRAP),
            registrationId = identityKeyRecord.registrationId,
            preKeys = preKeys.associate { it.id.toString() to Base64.encodeToString(it.keyPair.publicKey.serialize(), Base64.NO_WRAP) },
            signedPreKey = signedPreKey.toSummary(),
            deviceId = deviceId
        )
    }

    private fun PreKeyRecord.toSummary() = PreKeySummary(
        id = id,
        publicKey = Base64.encodeToString(keyPair.publicKey.serialize(), Base64.NO_WRAP)
    )

    private fun SignedPreKeyRecord.toSummary() = SignedPreKeySummary(
        id = id,
        publicKey = Base64.encodeToString(keyPair.publicKey.serialize(), Base64.NO_WRAP),
        signature = Base64.encodeToString(signature, Base64.NO_WRAP)
    )

    suspend fun encrypt(message: String, recipientId: String, deviceId: Int): ByteArray = withContext(Dispatchers.IO) {
        val address = SignalProtocolAddress(recipientId, deviceId)
        val sessionCipher = SessionCipher(store, address)

        if (!store.containsSession(address)) {
            val preKeyBundle = preKeyBundleRepository.getPreKeyBundleFromServer(recipientId, deviceId)
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
}
