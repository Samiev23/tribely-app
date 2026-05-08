package com.tribely.app.feature.battles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.data.repository.BattleRepository
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

data class CreateBattleState(
    val theme: String = "",
    val durationMinutes: Int = 60,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val createdBattleId: String? = null
)

class CreateBattleViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = BattleRepository()
    private val sessionManager = SessionManager(application)

    private val _state = MutableStateFlow(CreateBattleState())
    val state: StateFlow<CreateBattleState> = _state.asStateFlow()

    private suspend fun awaitAuthenticated(timeoutMs: Long = 5000) {
        withTimeoutOrNull(timeoutMs) {
            SupabaseManager.client.auth.sessionStatus
                .filterIsInstance<SessionStatus.Authenticated>()
                .first()
        }

        var attempts = 0
        while (SupabaseManager.client.auth.currentUserOrNull() == null && attempts < 5) {
            delay(200)
            attempts++
        }
    }

    fun updateTheme(theme: String) {
        _state.value = _state.value.copy(theme = theme)
    }

    fun updateDuration(minutes: Int) {
        _state.value = _state.value.copy(durationMinutes = minutes)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearCreatedBattle() {
        _state.value = _state.value.copy(createdBattleId = null)
    }

    fun create() {
        val theme = _state.value.theme.trim()
        if (theme.length < 5) {
            _state.value = _state.value.copy(error = "Минимум 5 символов")
            return
        }
        if (theme.length > 60) {
            _state.value = _state.value.copy(error = "Максимум 60 символов")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)

            try {
                awaitAuthenticated()
                val groupId = sessionManager.getGroupId()

                if (groupId == null) {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        error = "Группа не выбрана"
                    )
                    return@launch
                }

                repo.createBattle(
                    groupId = groupId,
                    theme = theme,
                    durationMinutes = _state.value.durationMinutes
                ).fold(
                    onSuccess = { battle ->
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            createdBattleId = battle.id
                        )
                    },
                    onFailure = { e ->
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            error = e.message ?: "Ошибка создания"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Ошибка"
                )
            }
        }
    }
}
