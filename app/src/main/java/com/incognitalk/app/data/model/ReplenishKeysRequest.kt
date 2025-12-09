package com.incognitalk.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReplenishKeysRequest(
    val userId: String,
    val preKeys: List<PreKeySummary>,
    val signedPreKeys: List<SignedPreKeySummary>
)
