package com.tribely.app.core.data.repository

import com.tribely.app.core.data.model.Battle
import com.tribely.app.core.data.model.BattleReaction
import com.tribely.app.core.data.model.BattleSubmission
import com.tribely.app.core.data.model.ReactionType
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class BattleRepository {
    private val client = SupabaseManager.client

    /**
     * Создать новый баттл в группе.
     * Автоматически рассчитывает submission_ends_at = now + duration_minutes.
     */
    suspend fun createBattle(
        groupId: String,
        theme: String,
        durationMinutes: Int
    ): Result<Battle> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        val now = Clock.System.now()
        val endsAt = now.plus(durationMinutes.minutes)

        client.from("battles")
            .insert(
                NewBattlePayload(
                    groupId = groupId,
                    creatorId = userId,
                    theme = theme.trim(),
                    durationMinutes = durationMinutes,
                    submissionEndsAt = endsAt
                )
            ) { select() }
            .decodeSingle<Battle>()
    }

    /**
     * Получить все баттлы группы (любой статус).
     */
    suspend fun getBattlesByGroup(groupId: String): Result<List<Battle>> =
        runCatching {
            client.from("battles")
                .select {
                    filter { eq("group_id", groupId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Battle>()
        }

    /**
     * Получить один баттл по id.
     */
    suspend fun getBattleById(battleId: String): Result<Battle> = runCatching {
        client.from("battles")
            .select {
                filter { eq("id", battleId) }
                limit(1)
            }
            .decodeSingle<Battle>()
    }

    /**
     * Получить все сабмишны баттла.
     */
    suspend fun getSubmissionsByBattle(battleId: String): Result<List<BattleSubmission>> =
        runCatching {
            client.from("battle_submissions")
                .select {
                    filter { eq("battle_id", battleId) }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<BattleSubmission>()
        }

    /**
     * Загрузить фото в bucket battles и создать сабмишн.
     */
    suspend fun submitToBattle(
        battleId: String,
        photoBytes: ByteArray,
        caption: String?
    ): Result<BattleSubmission> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        val fileName = "$userId/${battleId}_${Clock.System.now().toEpochMilliseconds()}.jpg"

        client.storage.from("battles").upload(fileName, photoBytes) {
            contentType = ContentType.Image.JPEG
            upsert = false
        }

        client.from("battle_submissions")
            .insert(
                NewBattleSubmissionPayload(
                    battleId = battleId,
                    userId = userId,
                    photoUrl = fileName,
                    caption = caption?.takeIf { it.isNotBlank() }
                )
            ) { select() }
            .decodeSingle<BattleSubmission>()
    }

    /**
     * Получить signed URL фото на 1 час.
     */
    suspend fun getSignedPhotoUrl(path: String): String? = runCatching {
        client.storage.from("battles").createSignedUrl(path, expiresIn = 1.hours)
    }.getOrNull()

    suspend fun getReactionsBySubmissions(
        submissionIds: List<String>
    ): Result<List<BattleReaction>> = runCatching {
        if (submissionIds.isEmpty()) return@runCatching emptyList()

        client.from("battle_reactions")
            .select {
                filter { isIn("submission_id", submissionIds) }
            }
            .decodeList<BattleReaction>()
    }

    suspend fun addReaction(
        submissionId: String,
        type: ReactionType
    ): Result<Unit> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        client.from("battle_reactions").insert(
            NewBattleReactionPayload(
                submissionId = submissionId,
                userId = userId,
                type = type.key
            )
        )
    }

    suspend fun removeReaction(
        submissionId: String,
        type: ReactionType
    ): Result<Unit> = runCatching {
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("Not authenticated")

        client.from("battle_reactions").delete {
            filter {
                eq("submission_id", submissionId)
                eq("user_id", userId)
                eq("type", type.key)
            }
        }
    }
}

@Serializable
private data class NewBattlePayload(
    @SerialName("group_id") val groupId: String,
    @SerialName("creator_id") val creatorId: String,
    val theme: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("submission_ends_at") val submissionEndsAt: Instant
)

@Serializable
private data class NewBattleSubmissionPayload(
    @SerialName("battle_id") val battleId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("photo_url") val photoUrl: String,
    val caption: String? = null
)

@Serializable
private data class NewBattleReactionPayload(
    @SerialName("submission_id") val submissionId: String,
    @SerialName("user_id") val userId: String,
    val type: String
)
