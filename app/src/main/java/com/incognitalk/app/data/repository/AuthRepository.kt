package com.incognitalk.app.data.repository

import com.incognitalk.app.data.model.RegistrationBundle
import com.incognitalk.app.data.model.RegisterRequest
import com.incognitalk.app.data.model.ReplenishKeysRequest
import com.incognitalk.app.data.network.ApiService

class AuthRepository(private val apiService: ApiService) {

    suspend fun checkUsernameAvailability(username: String): Boolean {
        return apiService.checkUsernameAvailability(username)
    }

    suspend fun register(username: String, registrationBundle: RegistrationBundle) {
        val request = RegisterRequest(username, registrationBundle)
        apiService.registerUser(request)
    }

    suspend fun replenishKeys(userId: String, registrationBundle: RegistrationBundle) {
        val request = ReplenishKeysRequest(
            userId = userId,
            preKeys = registrationBundle.preKeys,
            signedPreKeys = registrationBundle.signedPreKeys
        )
        apiService.replenishKeys(request)
    }
}
