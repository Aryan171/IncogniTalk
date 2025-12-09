package com.incognitalk.app.ui.navigation

import android.app.Application
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.repository.ChatRepository
import com.incognitalk.app.data.repository.ChatSocketRepository
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
import com.incognitalk.app.viewmodel.UserState

@Composable
fun NavGraph() {
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context.applicationContext as Application))
    val userState by mainViewModel.userState.collectAsState()

    val navController = rememberNavController()
    val db = IncogniTalkDatabase.getDatabase(context)
    val chatRepository = ChatRepository(db.chatDao(), db.messageDao())

    // Start/Stop WebSocket connection based on user state
    DisposableEffect(userState) {
        val currentUser = (userState as? UserState.Loaded)?.user
        if (currentUser != null) {
            ChatSocketRepository.init(context)
            ChatSocketRepository.start()
        }
        onDispose {
            if (currentUser != null) {
                ChatSocketRepository.stop()
            }
        }
    }

    // Show loading screen or main content based on user state
    Crossfade(targetState = userState) { state ->
        when (state) {
            is UserState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UserState.Loaded -> {
                val startDestination = if (state.user != null) Destinations.Home else Destinations.Registration
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
        }
    }
}
