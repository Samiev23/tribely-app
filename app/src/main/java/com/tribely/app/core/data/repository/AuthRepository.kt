package com.tribely.app.core.data.repository

import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

class AuthRepository(private val sessionManager: SessionManager) {

    @Serializable
    private data class ProfileNameUpdate(val display_name: String)

    @Serializable
    private data class ProfileExistsRow(val id: String)

    /**
     * Создаёт анонимного пользователя в Supabase, дожидается появления
     * профиля (создаётся триггером handle_new_user), обновляет display_name
     * и сохраняет сессию локально.
     */
    suspend fun signInAsDevUser(displayName: String): Result<String> = runCatching {
        val auth = SupabaseManager.client.auth

        // 1. Создаём анонимного пользователя — триггер на стороне БД
        //    автоматически создаёт запись в profiles с display_name = "User".
        auth.signInAnonymously()

        val user = auth.currentUserOrNull()
            ?: error("Не удалось получить текущего пользователя после входа")

        // 2. Ждём пока триггер handle_new_user создаст профиль (макс ~1.5 сек).
        var attempts = 0
        var profileExists = false
        while (attempts < 5 && !profileExists) {
            val rows = SupabaseManager.client
                .from("profiles")
                .select(Columns.list("id")) {
                    filter { eq("id", user.id) }
                }
                .decodeList<ProfileExistsRow>()

            if (rows.isNotEmpty()) {
                profileExists = true
            } else {
                attempts++
                delay(300)
            }
        }

        if (!profileExists) {
            error("Профиль не был создан триггером. Проверьте настройки Supabase.")
        }

        // 3. Обновляем имя — это разрешено политикой profiles_update_self.
        SupabaseManager.client
            .from("profiles")
            .update(ProfileNameUpdate(display_name = displayName)) {
                filter { eq("id", user.id) }
            }

        // 4. Сохраняем сессию локально.
        sessionManager.saveSession(userId = user.id, userName = displayName)
        user.id
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        SupabaseManager.client.auth.signOut()
        sessionManager.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return SupabaseManager.client.auth.currentUserOrNull() != null
    }
}
