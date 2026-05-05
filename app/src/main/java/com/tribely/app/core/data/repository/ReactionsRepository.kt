package com.tribely.app.core.data.repository

import android.util.Log
import com.tribely.app.core.data.model.Reaction
import com.tribely.app.core.data.model.ReactionType
import com.tribely.app.core.data.model.ReactionsState
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

private const val RT_TAG = "Tribely-RT"

@Serializable
private data class NewReaction(
    val submission_id: String,
    val user_id: String,
    val reaction_type: String
)

class ReactionsRepository {

    /**
     * Загружает все реакции на список submissions.
     * Возвращает Map<submissionId, ReactionsState>.
     */
    suspend fun loadReactions(
        submissionIds: List<String>
    ): Result<Map<String, ReactionsState>> = runCatching {
        if (submissionIds.isEmpty()) return@runCatching emptyMap()

        val myUserId = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not authenticated")

        val rows = SupabaseManager.client
            .from("reactions")
            .select {
                filter {
                    isIn("submission_id", submissionIds)
                }
            }
            .decodeList<Reaction>()

        Log.d(RT_TAG, "loadReactions: loaded ${rows.size} rows for ${submissionIds.size} submissions")

        rows.groupBy { it.submission_id }
            .mapValues { (_, reactions) ->
                ReactionsState(
                    fireCount = reactions.count { it.reaction_type == ReactionType.FIRE.key },
                    laughCount = reactions.count { it.reaction_type == ReactionType.LAUGH.key },
                    myFire = reactions.any {
                        it.user_id == myUserId && it.reaction_type == ReactionType.FIRE.key
                    },
                    myLaugh = reactions.any {
                        it.user_id == myUserId && it.reaction_type == ReactionType.LAUGH.key
                    }
                )
            }
    }

    /**
     * Toggle реакции: если стоит — удалит, если нет — добавит.
     */
    suspend fun toggleReaction(
        submissionId: String,
        type: ReactionType,
        currentlyOn: Boolean
    ): Result<Unit> = runCatching {
        val myUserId = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not authenticated")

        if (currentlyOn) {
            SupabaseManager.client
                .from("reactions")
                .delete {
                    filter {
                        eq("submission_id", submissionId)
                        eq("user_id", myUserId)
                        eq("reaction_type", type.key)
                    }
                }
        } else {
            SupabaseManager.client
                .from("reactions")
                .insert(NewReaction(submissionId, myUserId, type.key))
        }
    }

    /**
     * Realtime подписка на изменения таблицы reactions.
     * Возвращает Flow триггеров — при каждом изменении эмиттится Unit,
     * и подписчик может перезагрузить состояние.
     */
    fun subscribeToReactionChanges(channelName: String = "reactions-realtime"): Flow<Unit> = flow {
        val channel = SupabaseManager.client.channel(channelName)
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "reactions"
        }.map { event ->
            Log.d(RT_TAG, "Realtime event on $channelName: $event")
            Unit
        }

        try {
            Log.d(RT_TAG, "Subscribing to reactions channel: $channelName")
            channel.subscribe()
            Log.d(RT_TAG, "Subscribed to reactions channel: $channelName")
            emitAll(changes)
        } finally {
            Log.d(RT_TAG, "Removing reactions channel: $channelName")
            runCatching {
                SupabaseManager.client.realtime.removeChannel(channel)
            }.onFailure {
                Log.e(RT_TAG, "Failed to remove reactions channel: $channelName", it)
            }
        }
    }

    /**
     * Подключиться к Realtime (вызывается один раз при старте).
     */
    suspend fun connectRealtime() {
        Log.d(RT_TAG, "Connecting Supabase Realtime")
        SupabaseManager.client.realtime.connect()
        Log.d(RT_TAG, "Supabase Realtime connect() called")
    }

    /**
     * Отключить канал реакций.
     */
    suspend fun disconnectChannel(channelName: String = "reactions-realtime") {
        Log.d(RT_TAG, "Disconnect requested for channel: $channelName")
        runCatching {
            SupabaseManager.client.realtime.removeChannel(
                SupabaseManager.client.channel(channelName)
            )
        }.onFailure {
            Log.e(RT_TAG, "Failed to disconnect channel: $channelName", it)
        }
    }
}
