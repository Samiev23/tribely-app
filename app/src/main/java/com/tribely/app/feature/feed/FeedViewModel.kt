package com.tribely.app.feature.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tribely.app.core.data.model.ReactionType
import com.tribely.app.core.data.model.ReactionsState
import com.tribely.app.core.data.model.SubmissionWithAuthor
import com.tribely.app.core.data.repository.DailyRollRepository
import com.tribely.app.core.data.repository.ReactionsRepository
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private const val RT_TAG = "Tribely-RT"

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Success(val submissions: List<SubmissionWithAuthor>) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

class FeedViewModel(
    private val repository: DailyRollRepository = DailyRollRepository()
) : ViewModel() {

    private val reactionsRepo = ReactionsRepository()
    private var realtimeJob: Job? = null
    private var reactionsCache: Map<String, ReactionsState> = emptyMap()

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private suspend fun awaitAuthenticated(timeoutMs: Long = 5000) {
        withTimeoutOrNull(timeoutMs) {
            SupabaseManager.client.auth.sessionStatus
                .filterIsInstance<SessionStatus.Authenticated>()
                .first()
        }
    }

    fun loadFeed(groupId: String) {
        _uiState.value = FeedUiState.Loading
        viewModelScope.launch {
            awaitAuthenticated()

            var attempts = 0
            while (SupabaseManager.client.auth.currentUserOrNull() == null && attempts < 5) {
                delay(200)
                attempts++
            }

            repository.loadGroupFeed(groupId)
                .onSuccess {
                    _uiState.value = FeedUiState.Success(it.withCachedReactions())
                    reloadReactions()
                    startRealtimeSubscription()
                }
                .onFailure { _uiState.value = FeedUiState.Error(it.message ?: "Ошибка загрузки") }
        }
    }

    fun toggleReaction(submissionId: String, type: ReactionType) {
        val state = _uiState.value
        if (state !is FeedUiState.Success) return

        val currentSubmission = state.submissions.find { it.id == submissionId } ?: return
        if (currentSubmission.isMine) return

        val currentlyOn = when (type) {
            ReactionType.FIRE -> currentSubmission.reactions.myFire
            ReactionType.LAUGH -> currentSubmission.reactions.myLaugh
        }

        val updatedSubs = state.submissions.map { sub ->
            if (sub.id == submissionId) {
                val reactions = sub.reactions
                sub.copy(
                    reactions = when (type) {
                        ReactionType.FIRE -> reactions.copy(
                            myFire = !reactions.myFire,
                            fireCount = if (currentlyOn) {
                                (reactions.fireCount - 1).coerceAtLeast(0)
                            } else {
                                reactions.fireCount + 1
                            }
                        )
                        ReactionType.LAUGH -> reactions.copy(
                            myLaugh = !reactions.myLaugh,
                            laughCount = if (currentlyOn) {
                                (reactions.laughCount - 1).coerceAtLeast(0)
                            } else {
                                reactions.laughCount + 1
                            }
                        )
                    }
                )
            } else {
                sub
            }
        }
        reactionsCache = updatedSubs.associate { it.id to it.reactions }
        _uiState.value = state.copy(submissions = updatedSubs)

        viewModelScope.launch {
            reactionsRepo.toggleReaction(submissionId, type, currentlyOn)
                .onFailure {
                    Log.e(RT_TAG, "Feed toggleReaction failed: submissionId=$submissionId type=$type", it)
                    reloadReactions()
                }
        }
    }

    private suspend fun reloadReactions() {
        val state = _uiState.value
        if (state !is FeedUiState.Success) {
            Log.d(RT_TAG, "Feed reloadReactions skipped: state is not Success")
            return
        }

        val ids = state.submissions.map { it.id }
        if (ids.isEmpty()) {
            Log.d(RT_TAG, "Feed reloadReactions skipped: no submissions")
            return
        }

        Log.d(RT_TAG, "Feed reloadReactions: loading ${ids.size} submissions")
        reactionsRepo.loadReactions(ids).onSuccess { reactionsBySubmission ->
            val current = _uiState.value
            if (current !is FeedUiState.Success) return@onSuccess

            val updated = current.submissions.map { sub ->
                sub.copy(reactions = reactionsBySubmission[sub.id] ?: ReactionsState())
            }
            reactionsCache = updated.associate { it.id to it.reactions }
            _uiState.value = current.copy(submissions = updated)
            Log.d(RT_TAG, "Feed reloadReactions: applied ${reactionsBySubmission.size} reaction states")
        }.onFailure {
            Log.e(RT_TAG, "Feed reloadReactions failed", it)
        }
    }

    private fun startRealtimeSubscription() {
        if (realtimeJob?.isActive == true) {
            Log.d(RT_TAG, "Feed realtime subscription already active")
            return
        }

        realtimeJob = viewModelScope.launch {
            Log.d(RT_TAG, "Feed realtime subscription starting")
            runCatching { reactionsRepo.connectRealtime() }
                .onFailure { Log.e(RT_TAG, "Feed realtime connect failed", it) }
            try {
                reactionsRepo.subscribeToReactionChanges("reactions-feed").collect {
                    Log.d(RT_TAG, "Feed realtime event received, reloading reactions")
                    reloadReactions()
                }
            } finally {
                withContext(NonCancellable) {
                    reactionsRepo.disconnectChannel("reactions-feed")
                }
            }
        }
    }

    override fun onCleared() {
        realtimeJob?.cancel()
        super.onCleared()
    }

    private fun List<SubmissionWithAuthor>.withCachedReactions(): List<SubmissionWithAuthor> {
        if (reactionsCache.isEmpty()) return this

        return map { submission ->
            submission.copy(reactions = reactionsCache[submission.id] ?: submission.reactions)
        }
    }
}
