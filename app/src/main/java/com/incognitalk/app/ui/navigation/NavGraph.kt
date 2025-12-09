package com.incognitalk.app.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.incognitalk.app.viewmodel.MainViewModel
import com.incognitalk.app.viewmodel.MainViewModelFactory

@Composable
fun NavGraph() {
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context.applicationContext as Application))
    val user by mainViewModel.user.collectAsState()

    val navController = rememberNavController()
    val db = IncogniTalkDatabase.getDatabase(context)
    val chatRepository = ChatRepository(db.chatDao(), db.messageDao())

    val startDestination = if (user == null) Destinations.Registration else Destinations.Home

    NavHost(navController = navController, startDestination = startDestination) {
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
