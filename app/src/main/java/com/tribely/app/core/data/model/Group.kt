package com.tribely.app.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String,
    val name: String,
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("rc_balance") val rcBalance: Int = 0,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class GroupMembership(
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
    val role: String,
    @SerialName("joined_at") val joinedAt: String
)
