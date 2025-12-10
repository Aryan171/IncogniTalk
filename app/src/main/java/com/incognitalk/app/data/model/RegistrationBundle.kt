package com.incognitalk.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationBundle(
    val identityKey: String,
    val registrationId: Int,
    val preKeys: Map<String, String>,
    val signedPreKey: SignedPreKeySummary,
    val deviceId: Int // Added deviceId
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
