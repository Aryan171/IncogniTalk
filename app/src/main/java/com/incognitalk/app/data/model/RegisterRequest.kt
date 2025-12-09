package com.incognitalk.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val registrationBundle: RegistrationBundle
)
