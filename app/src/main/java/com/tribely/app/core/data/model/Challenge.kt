package com.tribely.app.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Challenge(
    val id: String,
    val category: String,
    @SerialName("text_ru") val textRu: String,
    @SerialName("text_en") val textEn: String,
    @SerialName("media_type") val mediaType: String,
    @SerialName("is_active") val isActive: Boolean = true
)
