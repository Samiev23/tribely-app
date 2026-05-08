package com.tribely.app.core.data.model

import kotlinx.datetime.Instant

data class BattleSubmissionWithAuthor(
    val id: String,
    val battleId: String,
    val userId: String,
    val photoUrl: String,
    val signedPhotoUrl: String? = null,
    val caption: String? = null,
    val authorName: String,
    val authorAvatarColor: String,
    val authorIsMe: Boolean,
    val createdAt: Instant,
    val reactions: ReactionsState = ReactionsState()
)
