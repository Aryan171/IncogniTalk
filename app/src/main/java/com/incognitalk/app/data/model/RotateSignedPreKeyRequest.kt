package com.incognitalk.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RotateSignedPreKeyRequest(
    val userId: String,
    val signedPreKey: SignedPreKeySummary
)
