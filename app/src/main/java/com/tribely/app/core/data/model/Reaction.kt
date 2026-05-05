package com.tribely.app.core.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Reaction(
    val id: String? = null,
    val submission_id: String,
    val user_id: String,
    val reaction_type: String
)

/**
 * Агрегированное состояние реакций для одного submission.
 */
data class ReactionsState(
    val fireCount: Int = 0,
    val laughCount: Int = 0,
    val myFire: Boolean = false,
    val myLaugh: Boolean = false
)

enum class ReactionType(val key: String, val emoji: String) {
    FIRE("fire", "🔥"),
    LAUGH("laugh", "😂")
}
