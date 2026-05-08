package com.tribely.app.core.data.model

data class BattleWithDetails(
    val battle: Battle,
    val submissionsCount: Int,
    val creatorName: String,
    val creatorIsMe: Boolean,
    val iSubmitted: Boolean
)
