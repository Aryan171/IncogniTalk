package com.incognitalk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.incognitalk.app.ui.chat.ChatScreen
import com.incognitalk.app.ui.home.HomeScreen
import com.incognitalk.app.ui.information.InformationScreen
import com.incognitalk.app.ui.navigation.Destinations
import com.incognitalk.app.ui.theme.IncogniTalkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IncogniTalkTheme {
                IncogniTalkNavHost()
            }
        }
    }
}

@Composable
fun IncogniTalkNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destinations.Home) {
        composable<Destinations.Home> {
            HomeScreen(
                onInfoClick = { navController.navigate(Destinations.Info) },
                onChatClick = { chatName -> navController.navigate(Destinations.Chat(chatName)) }
            )
        }
        composable<Destinations.Info> {
            InformationScreen()
        }
        composable<Destinations.Chat> {
            val args = it.toRoute<Destinations.Chat>()
            ChatScreen(chatName = args.chatName)
        }
    }
}
