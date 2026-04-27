package com.amro.data.network.security

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val tokenProvider: () -> String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider().trim()
        if (token.isBlank()) {
            throw MissingTmdbTokenException()
        }

        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }
}

class MissingTmdbTokenException : IOException(
    "TMDB_BEARER_TOKEN is blank. Provide it via Gradle properties."
)
