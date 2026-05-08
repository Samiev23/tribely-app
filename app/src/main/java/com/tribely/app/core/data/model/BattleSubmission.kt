package com.tribely.app.core.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BattleSubmission(
    val id: String,
    @SerialName("battle_id") val battleId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("photo_url") val photoUrl: String,
    val caption: String? = null,
    @SerialName("created_at") val createdAt: Instant
)
