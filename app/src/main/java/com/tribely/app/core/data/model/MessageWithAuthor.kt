package com.tribely.app.core.data.model

import kotlinx.datetime.Instant

data class MessageWithAuthor(
    val id: String,
    val groupId: String,
    val userId: String,
    val text: String,
    val createdAt: Instant,
    val authorName: String,
    val authorAvatarColor: String,
    val authorIsMe: Boolean
)
