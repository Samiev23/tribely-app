package com.tribely.app.feature.battles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.data.model.Battle
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

data class BattlesState(
    val isLoading: Boolean = true,
    val activeBattles: List<Battle> = emptyList(),
    val finishedBattles: List<Battle> = emptyList(),
    val error: String? = null
)

class BattlesViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = BattleRepository()
    private val sessionManager = SessionManager(application)

    private val _state = MutableStateFlow(BattlesState())
    val state: StateFlow<BattlesState> = _state.asStateFlow()

    init {
        loadBattles()
    }

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

    fun loadBattles() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                awaitAuthenticated()
                val groupId = sessionManager.getGroupId()

                if (groupId == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Группа не выбрана"
                    )
                    return@launch
                }

                repo.getBattlesByGroup(groupId).fold(
                    onSuccess = { battles ->
                        val active = battles.filter {
                            it.status == "live" || it.status == "voting"
                        }
                        val finished = battles.filter { it.status == "finished" }

                        _state.value = BattlesState(
                            isLoading = false,
                            activeBattles = active,
                            finishedBattles = finished
                        )
                    },
                    onFailure = { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message ?: "Ошибка загрузки"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка"
                )
            }
        }
    }

    fun refresh() = loadBattles()
}
