package com.incognitalk.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReplenishPreKeysRequest(
    val userId: String,
    val preKeys: List<PreKeySummary>
)
