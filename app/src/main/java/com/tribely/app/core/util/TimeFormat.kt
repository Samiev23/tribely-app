package com.tribely.app.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object TimeFormat {

    /**
     * Возвращает строку типа "только что", "5 мин назад", "2 ч назад", "вчера".
     */
    fun timeAgo(isoTimestamp: String): String {
        return try {
            val instant = Instant.parse(isoTimestamp)
            val now = Clock.System.now()
            val seconds = (now - instant).inWholeSeconds

            when {
                seconds < 30 -> "только что"
                seconds < 60 -> "$seconds сек назад"
                seconds < 3600 -> "${seconds / 60} мин назад"
                seconds < 86400 -> "${seconds / 3600} ч назад"
                seconds < 172800 -> "вчера"
                else -> "${seconds / 86400} дн назад"
            }
        } catch (e: Exception) {
            ""
        }
    }
}
