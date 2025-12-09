package com.incognitalk.app.data.repository

import com.incognitalk.app.data.model.PreKeySummary
import com.incognitalk.app.data.model.RegistrationBundle
import com.incognitalk.app.data.model.RegisterRequest
import com.incognitalk.app.data.model.ReplenishPreKeysRequest
import com.incognitalk.app.data.model.RotateSignedPreKeyRequest
import com.incognitalk.app.data.model.SignedPreKeySummary
import com.incognitalk.app.data.network.ApiService

class AuthRepository(private val apiService: ApiService) {

    suspend fun checkUsernameAvailability(username: String): Boolean {
        return apiService.checkUsernameAvailability(username)
    }

    suspend fun register(username: String, registrationBundle: RegistrationBundle) {
        val request = RegisterRequest(username, registrationBundle)
        apiService.registerUser(request)
    }

    suspend fun replenishPreKeys(userId: String, preKeys: List<PreKeySummary>) {
        val request = ReplenishPreKeysRequest(
            userId = userId,
            preKeys = preKeys
        )
        apiService.replenishPreKeys(request)
    }

    suspend fun rotateSignedPreKey(userId: String, signedPreKey: SignedPreKeySummary) {
        val request = RotateSignedPreKeyRequest(
            userId = userId,
            signedPreKey = signedPreKey
        )
        apiService.rotateSignedPreKey(request)
    }
}
