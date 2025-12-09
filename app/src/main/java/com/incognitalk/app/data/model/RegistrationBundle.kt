package com.incognitalk.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationBundle(
    val identityKey: String,
    val registrationId: Int,
    val preKeys: Map<String, String>, // Changed from a list to a map
    val signedPreKey: SignedPreKeySummary
)

@Serializable
data class PreKeySummary(
    val id: Int,
    val publicKey: String
)

@Serializable
data class SignedPreKeySummary(
    val id: Int,
    val publicKey: String,
    val signature: String
)
