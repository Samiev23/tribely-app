package com.tribely.app.core.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BattleReaction(
    val id: String? = null,
    @SerialName("submission_id") val submissionId: String,
    @SerialName("user_id") val userId: String,
    val type: String,
    @SerialName("created_at") val createdAt: Instant? = null
)
