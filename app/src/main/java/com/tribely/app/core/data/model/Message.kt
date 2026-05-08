package com.tribely.app.core.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String? = null,
    @SerialName("group_id") val groupId: String,
    @SerialName("user_id") val userId: String,
    val text: String,
    @SerialName("created_at") val createdAt: Instant? = null
)
