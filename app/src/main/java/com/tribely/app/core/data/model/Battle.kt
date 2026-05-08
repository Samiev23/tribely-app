package com.tribely.app.core.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Battle(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("creator_id") val creatorId: String,
    val theme: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    val status: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("submission_ends_at") val submissionEndsAt: Instant
)
