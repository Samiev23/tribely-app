package com.tribely.app.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(SessionManager(application))

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun signInAsTestUser(testUserNumber: Int) {
        viewModelScope.launch {
            _state.value = AuthState.Loading

            val displayName = when (testUserNumber) {
                1 -> "Тест-юзер 1"
                2 -> "Тест-юзер 2"
                else -> "Тест-юзер $testUserNumber"
            }

            authRepository.signInAsDevUser(displayName)
                .onSuccess {
                    _state.value = AuthState.Success
                }
                .onFailure { error ->
                    _state.value = AuthState.Error(error.message ?: "Ошибка входа")
                }
        }
    }
}
