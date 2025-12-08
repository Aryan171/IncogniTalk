package com.incognitalk.app.data.repository

import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.model.keystore.RemoteIdentity
import kotlinx.coroutines.runBlocking
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.InvalidKeyIdException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.*
import java.io.IOException
import com.incognitalk.app.data.model.keystore.Session
import com.incognitalk.app.data.model.keystore.PreKey
import com.incognitalk.app.data.model.keystore.SignedPreKey

class RoomSignalProtocolStore(private val database: IncogniTalkDatabase) : SignalProtocolStore {

    override fun getIdentityKeyPair(): IdentityKeyPair = runBlocking {
        database.identityKeyDao().getIdentityKey()?.let {
            IdentityKeyPair(it.keyPair)
        } ?: throw IOException("Identity key pair not found")
    }

    override fun getLocalRegistrationId(): Int = runBlocking {
        database.identityKeyDao().getRegistrationId() ?: 0
    }

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean = runBlocking {
        val oldIdentity = database.remoteIdentityDao().getRemoteIdentity(address.toString())
        val newSerializedIdentity = identityKey.serialize()

        val identityChanged = if (oldIdentity == null) {
            true // It's a new identity
        } else {
            // Check if the keys are different
            !oldIdentity.identityKey.contentEquals(newSerializedIdentity)
        }

        if (identityChanged) {
            database.remoteIdentityDao().insert(RemoteIdentity(address.toString(), newSerializedIdentity))
        }

        identityChanged
    }

    override fun isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey, direction: IdentityKeyStore.Direction): Boolean = runBlocking {
        val remoteIdentity = database.remoteIdentityDao().getRemoteIdentity(address.toString())
        if (remoteIdentity == null) {
            true // Trust on first use
        } else {
            IdentityKey(remoteIdentity.identityKey, 0) == identityKey
        }
    }

    override fun getIdentity(address: SignalProtocolAddress): IdentityKey? = runBlocking {
        database.remoteIdentityDao().getRemoteIdentity(address.toString())?.let {
             IdentityKey(it.identityKey, 0)
        }
    }

    override fun loadPreKey(preKeyId: Int): PreKeyRecord = runBlocking {
        database.preKeyDao().getPreKey(preKeyId)?.let {
            PreKeyRecord(it.record)
        } ?: throw InvalidKeyIdException("PreKey with ID $preKeyId not found")
    }

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) = runBlocking {
        database.preKeyDao().insert(PreKey(preKeyId, record.serialize()))
    }

    override fun containsPreKey(preKeyId: Int): Boolean = runBlocking {
        database.preKeyDao().getPreKey(preKeyId) != null
    }

    override fun removePreKey(preKeyId: Int) = runBlocking {
        database.preKeyDao().deletePreKey(preKeyId)
    }

    override fun loadSession(address: SignalProtocolAddress): SessionRecord = runBlocking {
        database.sessionDao().getSession(address.toString())?.let {
            SessionRecord(it.record)
        } ?: SessionRecord()
    }

    override fun getSubDeviceSessions(name: String): List<Int> = runBlocking {
        val recipientIds = database.sessionDao().getSubDeviceSessionRecipients(name)
        recipientIds.mapNotNull {
            // Extracts the string after the last ':', which is the deviceId, then safely converts to Int.
            it.substringAfterLast(':', "").toIntOrNull()
        }
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) = runBlocking {
        database.sessionDao().insert(Session(address.toString(), record.serialize()))
    }

    override fun containsSession(address: SignalProtocolAddress): Boolean = runBlocking {
        database.sessionDao().getSession(address.toString()) != null
    }

    override fun deleteSession(address: SignalProtocolAddress) = runBlocking {
        database.sessionDao().deleteSession(address.toString())
    }

    override fun deleteAllSessions(name: String) = runBlocking {
        database.sessionDao().deleteAllSessionsForUser(name)
    }

    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord = runBlocking {
        database.signedPreKeyDao().getSignedPreKey(signedPreKeyId)?.let {
            SignedPreKeyRecord(it.record)
        } ?: throw InvalidKeyIdException("SignedPreKey with ID $signedPreKeyId not found")
    }

    override fun loadSignedPreKeys(): List<SignedPreKeyRecord> = runBlocking {
        database.signedPreKeyDao().getAllSignedPreKeys().map { SignedPreKeyRecord(it.record) }
    }

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) = runBlocking {
        database.signedPreKeyDao().insert(SignedPreKey(signedPreKeyId, record.serialize()))
    }

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean = runBlocking {
        database.signedPreKeyDao().getSignedPreKey(signedPreKeyId) != null
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) = runBlocking {
        database.signedPreKeyDao().deleteSignedPreKey(signedPreKeyId)
    }
}
