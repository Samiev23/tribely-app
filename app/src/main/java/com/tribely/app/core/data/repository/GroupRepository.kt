package com.tribely.app.core.data.repository

import com.tribely.app.core.data.model.Group
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GroupRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Парсит ответ от RPC. Supabase возвращает либо JSON-объект {...},
     * либо массив [{...}] — обрабатываем оба случая.
     */
    private fun parseGroupResponse(rawJson: String): Group {
        val element: JsonElement = json.parseToJsonElement(rawJson)
        val obj: JsonObject = when (element) {
            is JsonArray -> {
                if (element.isEmpty()) error("Пустой ответ от сервера")
                element.first() as JsonObject
            }
            is JsonObject -> element
            else -> error("Неожиданный формат ответа: $rawJson")
        }
        return json.decodeFromJsonElement(Group.serializer(), obj)
    }

    /**
     * Создаёт группу через RPC create_group_with_admin.
     */
    suspend fun createGroup(name: String): Result<Group> = runCatching {
        val response = SupabaseManager.client
            .postgrest
            .rpc(
                "create_group_with_admin",
                buildJsonObject { put("p_name", name.trim()) }
            )

        parseGroupResponse(response.data)
    }

    /**
     * Войти в группу по коду через RPC join_group_by_code.
     */
    suspend fun joinGroupByCode(code: String): Result<Group> = runCatching {
        val response = SupabaseManager.client
            .postgrest
            .rpc(
                "join_group_by_code",
                buildJsonObject { put("p_invite_code", code.trim().uppercase()) }
            )

        parseGroupResponse(response.data)
    }

    /**
     * Возвращает список групп текущего юзера. Пустой если ни в одной не состоит.
     */
    suspend fun getMyGroups(): Result<List<Group>> = runCatching {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        val memberships = SupabaseManager.client
            .from("group_members")
            .select(Columns.list("group_id")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<MembershipRow>()

        if (memberships.isEmpty()) return@runCatching emptyList()

        val groupIds = memberships.map { it.groupId }

        SupabaseManager.client
            .from("groups")
            .select {
                filter { isIn("id", groupIds) }
            }
            .decodeList<Group>()
    }

    @Serializable
    private data class MembershipRow(
        @kotlinx.serialization.SerialName("group_id") val groupId: String
    )
}
