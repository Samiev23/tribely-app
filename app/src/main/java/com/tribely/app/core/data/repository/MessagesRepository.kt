package com.tribely.app.core.data.repository

import com.tribely.app.core.data.model.Message
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class MessagesRepository {
    private val client = SupabaseManager.client

    /**
     * Получить последние N сообщений группы (новые внизу).
     * Берём в порядке DESC и потом разворачиваем — так быстрее по индексу.
     */
    suspend fun getMessagesByGroup(
        groupId: String,
        limit: Long = 100
    ): Result<List<Message>> = runCatching {
        client.from("messages")
            .select {
                filter { eq("group_id", groupId) }
                order("created_at", Order.DESCENDING)
                limit(limit)
            }
            .decodeList<Message>()
            .reversed()
    }

    /**
     * Отправить сообщение в группу.
     * RLS проверит что юзер в группе.
     */
    suspend fun sendMessage(
        groupId: String,
        text: String
    ): Result<Message> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        val trimmed = text.trim()
        if (trimmed.isEmpty()) error("Empty message")
        if (trimmed.length > 500) error("Message too long")

        val payload = mapOf(
            "group_id" to groupId,
            "user_id" to userId,
            "text" to trimmed
        )

        client.from("messages")
            .insert(payload) { select() }
            .decodeSingle<Message>()
    }

    /**
     * Получить последнее сообщение группы (для превью в списке чатов).
     * Возвращает null если в чате пусто.
     */
    suspend fun getLastMessage(groupId: String): Result<Message?> = runCatching {
        val list = client.from("messages")
            .select {
                filter { eq("group_id", groupId) }
                order("created_at", Order.DESCENDING)
                limit(1)
            }
            .decodeList<Message>()

        list.firstOrNull()
    }

    /**
     * Удалить своё сообщение (на будущее).
     */
    suspend fun deleteMessage(messageId: String): Result<Unit> = runCatching {
        client.from("messages").delete {
            filter { eq("id", messageId) }
        }
    }
}
