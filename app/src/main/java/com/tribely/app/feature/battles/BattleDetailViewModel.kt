package com.tribely.app.feature.battles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tribely.app.core.data.model.Battle
import com.tribely.app.core.data.model.BattleSubmissionWithAuthor
import com.tribely.app.core.data.model.ReactionType
import com.tribely.app.core.data.model.ReactionsState
import com.tribely.app.core.data.repository.BattleRepository
import com.tribely.app.core.data.repository.GroupRepository
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class BattleDetailState(
    val isLoading: Boolean = true,
    val battle: Battle? = null,
    val submissions: List<BattleSubmissionWithAuthor> = emptyList(),
    val isUploading: Boolean = false,
    val error: String? = null,
    val uploadDone: Boolean = false
)

class BattleDetailViewModel(
    private val battleId: String
) : ViewModel() {
    private val repo = BattleRepository()
    private val groupRepo = GroupRepository()

    private val _state = MutableStateFlow(BattleDetailState())
    val state: StateFlow<BattleDetailState> = _state.asStateFlow()

    private var myUserId: String? = null
    private var realtimeJob: Job? = null

    init {
        load()
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

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                awaitAuthenticated()
                val myId = SupabaseManager.client.auth.currentUserOrNull()?.id
                    ?: error("Not authenticated")
                myUserId = myId

                val battle = repo.getBattleById(battleId).getOrThrow()
                val submissions = repo.getSubmissionsByBattle(battleId).getOrThrow()
                val members = groupRepo.getGroupMembersWithProfiles(battle.groupId).getOrDefault(emptyList())
                val membersByUserId = members.associateBy { it.userId }
                val reactions = repo.getReactionsBySubmissions(submissions.map { it.id }).getOrDefault(emptyList())

                val withAuthors = submissions.map { submission ->
                    val member = membersByUserId[submission.userId]
                    val authorName = member?.profiles?.displayName ?: "Юзер"
                    val submissionReactions = reactions.filter { it.submissionId == submission.id }
                    val myReactionTypes = submissionReactions
                        .filter { it.userId == myId }
                        .map { it.type }

                    BattleSubmissionWithAuthor(
                        id = submission.id,
                        battleId = submission.battleId,
                        userId = submission.userId,
                        photoUrl = submission.photoUrl,
                        signedPhotoUrl = repo.getSignedPhotoUrl(submission.photoUrl),
                        caption = submission.caption,
                        authorName = authorName,
                        authorAvatarColor = avatarColorFor(authorName),
                        authorIsMe = submission.userId == myId,
                        createdAt = submission.createdAt,
                        reactions = ReactionsState(
                            fireCount = submissionReactions.count { it.type == ReactionType.FIRE.key },
                            laughCount = submissionReactions.count { it.type == ReactionType.LAUGH.key },
                            myFire = ReactionType.FIRE.key in myReactionTypes,
                            myLaugh = ReactionType.LAUGH.key in myReactionTypes
                        )
                    )
                }

                _state.value = BattleDetailState(
                    isLoading = false,
                    battle = battle,
                    submissions = withAuthors
                )

                if (realtimeJob == null) subscribeToReactions()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка"
                )
            }
        }
    }

    fun upload(photoBytes: ByteArray, caption: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true, error = null)

            repo.submitToBattle(battleId, photoBytes, caption).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        uploadDone = true
                    )
                    load()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isUploading = false,
                        error = e.message ?: "Ошибка загрузки"
                    )
                }
            )
        }
    }

    fun toggleReaction(submissionId: String, type: ReactionType) {
        val state = _state.value
        val submission = state.submissions.firstOrNull { it.id == submissionId } ?: return
        val isActive = when (type) {
            ReactionType.FIRE -> submission.reactions.myFire
            ReactionType.LAUGH -> submission.reactions.myLaugh
        }

        val updatedSubmissions = state.submissions.map { current ->
            if (current.id != submissionId) {
                current
            } else {
                val reactions = current.reactions
                current.copy(
                    reactions = when (type) {
                        ReactionType.FIRE -> reactions.copy(
                            fireCount = (reactions.fireCount + if (isActive) -1 else 1).coerceAtLeast(0),
                            myFire = !isActive
                        )
                        ReactionType.LAUGH -> reactions.copy(
                            laughCount = (reactions.laughCount + if (isActive) -1 else 1).coerceAtLeast(0),
                            myLaugh = !isActive
                        )
                    }
                )
            }
        }
        _state.value = state.copy(submissions = updatedSubmissions)

        viewModelScope.launch {
            val result = if (isActive) {
                repo.removeReaction(submissionId, type)
            } else {
                repo.addReaction(submissionId, type)
            }
            result.onFailure {
                _state.value = _state.value.copy(error = it.message ?: "Ошибка реакции")
                load()
            }
        }
    }

    private fun subscribeToReactions() {
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            runCatching {
                val channel = SupabaseManager.client.channel("battle_$battleId")
                val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "battle_reactions"
                }

                changes.onStart {
                    channel.subscribe()
                }.collect {
                    load()
                }
            }
        }
    }

    fun resetUploadFlag() {
        _state.value = _state.value.copy(uploadDone = false)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        realtimeJob?.cancel()
        super.onCleared()
    }

    private fun avatarColorFor(name: String): String {
        val palette = listOf("#FF3EA5", "#D4FF00", "#00E5FF", "#FFAA00", "#A855F7")
        return palette[kotlin.math.abs(name.hashCode()) % palette.size]
    }
}

class BattleDetailViewModelFactory(
    private val battleId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BattleDetailViewModel(battleId) as T
    }
}
