package com.incognitalk.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.incognitalk.app.data.database.IncogniTalkDatabase
import com.incognitalk.app.data.model.User
import com.incognitalk.app.data.network.ApiServiceImpl
import com.incognitalk.app.data.network.KtorClient
import com.incognitalk.app.data.repository.AuthRepository
import com.incognitalk.app.data.repository.SignalRepository
import com.incognitalk.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val signalRepository = SignalRepository(application)
    private val authRepository = AuthRepository(ApiServiceImpl(KtorClient.client))
    private val userRepository: UserRepository

    init {
        val userDao = IncogniTalkDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun register(username: String) {
        viewModelScope.launch {
            _uiState.value = RegistrationUiState.Loading
            try {
                if (authRepository.checkUsernameAvailability(username)) {
                    signalRepository.initializeKeys()
                    val registrationBundle = signalRepository.getRegistrationBundle()
                    authRepository.register(username, registrationBundle)
                    userRepository.saveUser(User(username))
                    _uiState.value = RegistrationUiState.Success
                } else {
                    _uiState.value = RegistrationUiState.Error("Username is not available")
                }
            } catch (e: Exception) {
                _uiState.value = RegistrationUiState.Error("Could not connect to the server")
            }
        }
    }
}

sealed class RegistrationUiState {
    object Idle : RegistrationUiState()
    object Loading : RegistrationUiState()
    object Success : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
}
