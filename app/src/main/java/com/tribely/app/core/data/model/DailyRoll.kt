package com.tribely.app.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyRoll(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("roll_date") val rollDate: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class GroupMemberWithProfile(
    @SerialName("user_id") val userId: String,
    val role: String,
    @SerialName("joined_at") val joinedAt: String,
    val profiles: ProfileShort? = null
)

@Serializable
data class ProfileShort(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class SubmissionShort(
    val id: String,
    @SerialName("daily_roll_id") val dailyRollId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("created_at") val createdAt: String
)

/**
 * Композитная модель для UI: кто из группы что сделал
 */
data class MemberSubmissionStatus(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val hasSubmitted: Boolean,
    val submittedAt: String?
)

/**
 * Композитная модель для DailyRollScreen
 */
data class DailyRollState(
    val roll: DailyRoll,
    val challenge: Challenge,
    val members: List<MemberSubmissionStatus>,
    val mySubmissionId: String?,
    val totalMembers: Int,
    val completedCount: Int
)
