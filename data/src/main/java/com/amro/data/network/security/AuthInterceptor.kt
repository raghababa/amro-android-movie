package com.amro.data.network.security

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider().trim()
        val builder = chain.request().newBuilder()
            .header("Accept", "application/json")

        // Build normally fails if TMDB_BEARER_TOKEN is missing. This runtime fallback
        // avoids a hard crash in case a developer explicitly bypasses the check.
        if (token.isNotBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}