package com.tribely.app.feature.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.data.model.Message
import com.tribely.app.core.data.repository.GroupRepository
import com.tribely.app.core.data.repository.MessagesRepository
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

data class ChatPreview(
    val groupId: String,
    val groupName: String,
    val memberCount: Int,
    val lastMessage: Message? = null,
    val lastMessageAuthor: String? = null,
    val lastMessageIsMe: Boolean = false
)

data class ChatListState(
    val isLoading: Boolean = true,
    val chats: List<ChatPreview> = emptyList(),
    val error: String? = null
)

class ChatListViewModel(application: Application) : AndroidViewModel(application) {
    private val messagesRepo = MessagesRepository()
    private val groupRepo = GroupRepository()
    private val sessionManager = SessionManager(application)

    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()

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

                val groupId = sessionManager.getGroupId()
                if (groupId == null) {
                    _state.value = ChatListState(
                        isLoading = false,
                        error = "Группа не выбрана"
                    )
                    return@launch
                }

                val group = groupRepo.getMyGroups()
                    .getOrNull()
                    ?.firstOrNull { it.id == groupId }
                val members = groupRepo.getGroupMembersWithProfiles(groupId)
                    .getOrNull()
                    ?: emptyList()

                val lastMsg = messagesRepo.getLastMessage(groupId).getOrNull()

                val lastAuthor = lastMsg?.let { msg ->
                    members.firstOrNull { it.userId == msg.userId }?.profiles?.displayName
                }
                val lastIsMe = lastMsg?.userId == myId

                val chat = ChatPreview(
                    groupId = groupId,
                    groupName = group?.name ?: "Группа",
                    memberCount = members.size,
                    lastMessage = lastMsg,
                    lastMessageAuthor = lastAuthor,
                    lastMessageIsMe = lastIsMe
                )

                _state.value = ChatListState(
                    isLoading = false,
                    chats = listOf(chat)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка загрузки"
                )
            }
        }
    }

    fun refresh() = load()
}
