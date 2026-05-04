package com.tribely.app.core.network

import com.tribely.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import java.util.concurrent.TimeUnit

object SupabaseManager {
    @OptIn(SupabaseInternal::class)
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            httpEngine = OkHttp.create {
                config {
                    connectTimeout(30, TimeUnit.SECONDS)
                    readTimeout(60, TimeUnit.SECONDS)
                    writeTimeout(60, TimeUnit.SECONDS)
                    retryOnConnectionFailure(true)
                }
            }

            httpConfig {
                install(HttpTimeout) {
                    requestTimeoutMillis = 60_000
                    connectTimeoutMillis = 30_000
                    socketTimeoutMillis = 60_000
                }
            }

            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "tribely"
                host = "auth"
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }
}
