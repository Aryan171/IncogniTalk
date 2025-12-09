package com.incognitalk.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.repository.ChatRepository
import com.incognitalk.app.ui.chat.ChatScreen
import com.incognitalk.app.ui.chat.ChatViewModel
import com.incognitalk.app.ui.chat.ChatViewModelFactory
import com.incognitalk.app.ui.home.HomeScreen
import com.incognitalk.app.ui.home.HomeScreenViewModel
import com.incognitalk.app.ui.home.HomeScreenViewModelFactory
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
    val context = LocalContext.current

    val db = IncogniTalkDatabase.getDatabase(context)
    val chatRepository = ChatRepository(db.chatDao(), db.messageDao())

    val homeViewModel: HomeScreenViewModel = viewModel(
        factory = HomeScreenViewModelFactory(chatRepository)
    )

    NavHost(navController = navController, startDestination = Destinations.Home) {
        composable<Destinations.Home> {
            HomeScreen(
                onInfoClick = { navController.navigate(Destinations.Info) },
                onChatClick = { chatName -> navController.navigate(Destinations.Chat(chatName)) },
                homeScreenViewModel = homeViewModel
            )
        }
        composable<Destinations.Info> {
            InformationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable<Destinations.Chat> {
            val args = it.toRoute<Destinations.Chat>()
            val chatViewModel: ChatViewModel = viewModel(
                factory = ChatViewModelFactory(context.applicationContext as Application, chatRepository, args.chatName)
            )
            ChatScreen(
                chatName = args.chatName,
                onBackClick = { navController.popBackStack() },
                chatViewModel = chatViewModel
            )
        }
    }
}
