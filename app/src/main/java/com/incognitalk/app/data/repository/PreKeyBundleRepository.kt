package com.incognitalk.app.data.repository

import android.util.Base64
import com.incognitalk.app.data.network.ApiService
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.PreKeyBundle

class PreKeyBundleRepository(private val apiService: ApiService) {
    suspend fun getPreKeyBundleFromServer(recipientId: String, deviceId: Int): PreKeyBundle {
        val dto = apiService.getPreKeyBundle(recipientId, deviceId)

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
            IdentityKey(identityKey, 0)
        )
    }
}
