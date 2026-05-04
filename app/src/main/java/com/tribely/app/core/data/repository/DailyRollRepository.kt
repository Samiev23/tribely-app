package com.tribely.app.core.data.repository

import com.tribely.app.core.data.model.Challenge
import com.tribely.app.core.data.model.DailyRoll
import com.tribely.app.core.data.model.DailyRollState
import com.tribely.app.core.data.model.GroupMemberWithProfile
import com.tribely.app.core.data.model.MemberSubmissionStatus
import com.tribely.app.core.data.model.SubmissionShort
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

@Serializable
private data class NewSubmission(
    val id: String,
    val daily_roll_id: String,
    val user_id: String,
    val media_url: String,
    val media_type: String
)

class DailyRollRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun parseDailyRoll(rawJson: String): DailyRoll {
        val element: JsonElement = json.parseToJsonElement(rawJson)
        val obj: JsonObject = when (element) {
            is JsonArray -> {
                if (element.isEmpty()) error("Пустой ответ от сервера")
                element.first() as JsonObject
            }
            is JsonObject -> element
            else -> error("Неожиданный формат ответа: $rawJson")
        }
        return json.decodeFromJsonElement(DailyRoll.serializer(), obj)
    }

    /**
     * Получить (или создать) бросок дня группы.
     */
    suspend fun getTodayRoll(groupId: String): Result<DailyRoll> = runCatching {
        val response = SupabaseManager.client
            .postgrest
            .rpc(
                "get_or_create_today_roll",
                buildJsonObject { put("p_group_id", groupId) }
            )
        parseDailyRoll(response.data)
    }

    /**
     * Получить челлендж по id.
     */
    suspend fun getChallenge(challengeId: String): Result<Challenge> = runCatching {
        SupabaseManager.client
            .from("challenges")
            .select {
                filter { eq("id", challengeId) }
            }
            .decodeSingle<Challenge>()
    }

    /**
     * Получить список участников группы вместе с их профилями.
     * Используем postgrest embedding: select=*,profiles(*)
     */
    suspend fun getGroupMembersWithProfiles(groupId: String): Result<List<GroupMemberWithProfile>> = runCatching {
        SupabaseManager.client
            .from("group_members")
            .select(Columns.raw("user_id,role,joined_at,profiles(id,display_name,avatar_url)")) {
                filter { eq("group_id", groupId) }
            }
            .decodeList<GroupMemberWithProfile>()
    }

    /**
     * Получить все submissions для конкретного броска.
     */
    suspend fun getSubmissionsForRoll(rollId: String): Result<List<SubmissionShort>> = runCatching {
        SupabaseManager.client
            .from("submissions")
            .select {
                filter { eq("daily_roll_id", rollId) }
            }
            .decodeList<SubmissionShort>()
    }

    /**
     * Главный метод для UI: собирает всё вместе.
     */
    suspend fun loadDailyRollState(groupId: String): Result<DailyRollState> = runCatching {
        val currentUserId = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        val roll = getTodayRoll(groupId).getOrThrow()
        val challenge = getChallenge(roll.challengeId).getOrThrow()
        val members = getGroupMembersWithProfiles(groupId).getOrThrow()
        val submissions = getSubmissionsForRoll(roll.id).getOrThrow()

        val submittedUserIds = submissions.map { it.userId }.toSet()
        val mySubmission = submissions.firstOrNull { it.userId == currentUserId }

        val statuses = members.map { member ->
            val submission = submissions.firstOrNull { it.userId == member.userId }
            MemberSubmissionStatus(
                userId = member.userId,
                displayName = member.profiles?.displayName ?: "User",
                avatarUrl = member.profiles?.avatarUrl,
                hasSubmitted = submission != null,
                submittedAt = submission?.createdAt
            )
        }.sortedByDescending { it.hasSubmitted }

        DailyRollState(
            roll = roll,
            challenge = challenge,
            members = statuses,
            mySubmissionId = mySubmission?.id,
            totalMembers = members.size,
            completedCount = submittedUserIds.size
        )
    }

    /**
     * Загружает фото в Supabase Storage bucket "submissions"
     * по пути {user_id}/{submission_id}.jpg и создаёт запись в таблице submissions.
     * Возвращает submission_id.
     */
    suspend fun uploadSubmission(
        rollId: String,
        imageBytes: ByteArray
    ): Result<String> = runCatching {
        val tag = "TribelyUpload"
        android.util.Log.d(tag, "uploadSubmission: started, bytes=${imageBytes.size}")

        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")
        android.util.Log.d(tag, "userId=$userId, rollId=$rollId")

        val submissionId = UUID.randomUUID().toString()
        val storagePath = "$userId/$submissionId.jpg"
        android.util.Log.d(tag, "storagePath=$storagePath")

        val uploadStart = System.currentTimeMillis()
        SupabaseManager.client
            .storage
            .from("submissions")
            .upload(storagePath, imageBytes) {
                upsert = false
                contentType = ContentType.Image.JPEG
            }
        val uploadElapsed = System.currentTimeMillis() - uploadStart
        android.util.Log.d(tag, "Storage upload complete in ${uploadElapsed}ms")

        val dbStart = System.currentTimeMillis()
        SupabaseManager.client
            .from("submissions")
            .insert(
                NewSubmission(
                    id = submissionId,
                    daily_roll_id = rollId,
                    user_id = userId,
                    media_url = storagePath,
                    media_type = "photo"
                )
            )
        val dbElapsed = System.currentTimeMillis() - dbStart
        android.util.Log.d(tag, "DB insert complete in ${dbElapsed}ms, submissionId=$submissionId")

        submissionId
    }
}
