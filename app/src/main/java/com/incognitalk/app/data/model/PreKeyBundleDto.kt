package com.incognitalk.app.data.model

import kotlinx.serialization.Serializable

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
