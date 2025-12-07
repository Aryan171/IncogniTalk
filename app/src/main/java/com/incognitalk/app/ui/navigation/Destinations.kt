package com.incognitalk.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Destinations {
    @Serializable
    data object Home : Destinations()

    @Serializable
    data object Info : Destinations()

    @Serializable
    data class Chat(val chatName: String) : Destinations()
}
