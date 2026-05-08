package com.tribely.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tribely.app.core.data.model.Message
import com.tribely.app.core.data.model.MessageWithAuthor
import com.tribely.app.core.data.repository.GroupRepository
import com.tribely.app.core.data.repository.MessagesRepository
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

data class ChatState(
    val isLoading: Boolean = true,
    val groupName: String = "",
    val memberCount: Int = 0,
    val messages: List<MessageWithAuthor> = emptyList(),
    val draft: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val groupId: String
) : ViewModel() {
    private val messagesRepo = MessagesRepository()
    private val groupRepo = GroupRepository()
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var myUserId: String? = null
    private var membersCache = mapOf<String, Pair<String, String>>()
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

                val group = groupRepo.getMyGroups()
                    .getOrNull()
                    ?.firstOrNull { it.id == groupId }
                val members = groupRepo.getGroupMembersWithProfiles(groupId)
                    .getOrNull()
                    ?: emptyList()

                membersCache = members.associate { member ->
                    val name = member.profiles?.displayName ?: "Юзер"
                    member.userId to (name to avatarColorFor(name))
                }

                val msgs = messagesRepo.getMessagesByGroup(groupId).getOrThrow()
                val withAuthors = msgs.mapNotNull { it.toUiModel(myId) }

                _state.value = ChatState(
                    isLoading = false,
                    groupName = group?.name ?: "Чат",
                    memberCount = members.size,
                    messages = withAuthors,
                    draft = _state.value.draft,
                    isSending = _state.value.isSending
                )

                if (realtimeJob == null) subscribeToMessages()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun updateDraft(text: String) {
        if (text.length <= 500) {
            _state.value = _state.value.copy(draft = text)
        }
    }

    fun send() {
        val text = _state.value.draft.trim()
        if (text.isEmpty() || text.length > 500 || _state.value.isSending) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true, error = null)

            messagesRepo.sendMessage(groupId, text).fold(
                onSuccess = { msg ->
                    val myId = myUserId ?: return@fold
                    val ui = msg.toUiModel(myId)

                    if (ui != null) {
                        val current = _state.value.messages
                        if (current.none { it.id == ui.id }) {
                            _state.value = _state.value.copy(
                                messages = current + ui,
                                draft = "",
                                isSending = false
                            )
                        } else {
                            _state.value = _state.value.copy(
                                draft = "",
                                isSending = false
                            )
                        }
                    } else {
                        _state.value = _state.value.copy(
                            draft = "",
                            isSending = false
                        )
                    }
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSending = false,
                        error = e.message ?: "Не удалось отправить"
                    )
                }
            )
        }
    }

    private fun subscribeToMessages() {
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            try {
                val channel = SupabaseManager.client.channel("chat_$groupId")

                val flow = channel.postgresChangeFlow<PostgresAction>(
                    schema = "public"
                ) {
                    table = "messages"
                }

                flow.onStart {
                    channel.subscribe()
                }.collect { action ->
                    when (action) {
                        is PostgresAction.Insert -> handleNewMessage(action.record)
                        is PostgresAction.Delete -> handleDeletedMessage(action.oldRecord)
                        else -> Unit
                    }
                }
            } catch (_: Exception) {
                // Realtime is best-effort; manual refresh/load will still show messages.
            }
        }
    }

    private fun handleNewMessage(record: JsonObject) {
        try {
            val myId = myUserId ?: return
            val msg = json.decodeFromJsonElement<Message>(record)
            if (msg.groupId != groupId) return
            val ui = msg.toUiModel(myId) ?: return

            val current = _state.value.messages
            if (current.any { it.id == ui.id }) return

            _state.value = _state.value.copy(
                messages = current + ui
            )
        } catch (_: Exception) {
            // Ignore malformed realtime payloads.
        }
    }

    private fun handleDeletedMessage(record: JsonObject) {
        try {
            val deletedGroupId = record["group_id"]?.jsonPrimitive?.content
            if (deletedGroupId != null && deletedGroupId != groupId) return
            val deletedId = record["id"]?.jsonPrimitive?.content ?: return
            _state.value = _state.value.copy(
                messages = _state.value.messages.filter { it.id != deletedId }
            )
        } catch (_: Exception) {
            // Ignore malformed realtime payloads.
        }
    }

    private fun Message.toUiModel(myId: String): MessageWithAuthor? {
        val msgId = id ?: return null
        val createdAt = createdAt ?: return null
        val author = membersCache[userId]

        return MessageWithAuthor(
            id = msgId,
            groupId = groupId,
            userId = userId,
            text = text,
            createdAt = createdAt,
            authorName = author?.first ?: "Юзер",
            authorAvatarColor = author?.second ?: "#FF3EA5",
            authorIsMe = userId == myId
        )
    }

    private fun avatarColorFor(name: String): String {
        val colors = listOf("#FF3EA5", "#A855F7", "#00E5FF", "#FFAA00", "#22C55E")
        val index = kotlin.math.abs(name.hashCode()) % colors.size
        return colors[index]
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        realtimeJob?.cancel()
        runCatching {
            viewModelScope.launch {
                SupabaseManager.client.realtime.removeChannel(
                    SupabaseManager.client.channel("chat_$groupId")
                )
            }
        }
        super.onCleared()
    }
}

class ChatViewModelFactory(
    private val groupId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(groupId) as T
    }
}
