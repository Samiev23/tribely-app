package com.tribely.app.feature.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tribely.app.core.data.model.DailyRollState
import com.tribely.app.core.data.repository.DailyRollRepository
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed interface DailyRollUiState {
    data object Loading : DailyRollUiState
    data class Success(val data: DailyRollState) : DailyRollUiState
    data class Error(val message: String) : DailyRollUiState
}

class DailyRollViewModel(
    private val repository: DailyRollRepository = DailyRollRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyRollUiState>(DailyRollUiState.Loading)
    val uiState: StateFlow<DailyRollUiState> = _uiState.asStateFlow()

    /**
     * Дожидаемся активной сессии Supabase. SDK переходит в SessionStatus.Authenticated
     * после того как успешно загружен/создан токен. До этого RPC будут отвечать "Not authenticated".
     */
    private suspend fun awaitAuthenticated(timeoutMs: Long = 5000) {
        withTimeoutOrNull(timeoutMs) {
            SupabaseManager.client.auth.sessionStatus
                .filterIsInstance<SessionStatus.Authenticated>()
                .first()
        }
    }

    fun loadRoll(groupId: String) {
        _uiState.value = DailyRollUiState.Loading
        viewModelScope.launch {
            awaitAuthenticated()

            var attempts = 0
            while (SupabaseManager.client.auth.currentUserOrNull() == null && attempts < 5) {
                delay(200)
                attempts++
            }

            repository.loadDailyRollState(groupId)
                .onSuccess { _uiState.value = DailyRollUiState.Success(it) }
                .onFailure { _uiState.value = DailyRollUiState.Error(it.message ?: "Неизвестная ошибка") }
        }
    }

    fun refresh(groupId: String) = loadRoll(groupId)
}
