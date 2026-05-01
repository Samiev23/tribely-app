package com.tribely.app.core.data.repository

import com.tribely.app.core.data.model.Challenge
import com.tribely.app.core.network.SupabaseManager
import io.github.jan.supabase.postgrest.from

// TODO USER ACTION: Перед запуском нужно выполнить в Supabase SQL Editor:
//   alter table public.challenges enable row level security;
//   create policy challenges_read_all on public.challenges
//     for select using (is_active = true);
// Это временная политика для теста — позже мы перепишем все политики комплексно.
class ChallengeRepository {
    suspend fun getAllActiveChallenges(): Result<List<Challenge>> = runCatching {
        SupabaseManager.client
            .from("challenges")
            .select()
            .decodeList<Challenge>()
    }
}
