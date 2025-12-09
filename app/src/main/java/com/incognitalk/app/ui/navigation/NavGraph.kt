package com.incognitalk.app.ui.navigation

import android.app.Application
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
import com.incognitalk.app.ui.screens.RegistrationScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val db = IncogniTalkDatabase.getDatabase(context)
    val chatRepository = ChatRepository(db.chatDao(), db.messageDao())

    NavHost(navController = navController, startDestination = Destinations.Registration) {
        composable<Destinations.Registration> {
            RegistrationScreen(
                onRegistrationSuccess = {
                    navController.navigate(Destinations.Home) {
                        popUpTo(Destinations.Registration) { inclusive = true }
                    }
                }
            )
        }
        composable<Destinations.Home> {
            val homeViewModel: HomeScreenViewModel = viewModel(
                factory = HomeScreenViewModelFactory(chatRepository)
            )
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
