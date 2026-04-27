package com.amro.data.network.security

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Adds the current bearer token to TMDB requests.
 *
 * A production OAuth-style flow could pair this with an OkHttp Authenticator that refreshes
 * the token and retries once after a 401 response.
 */
class AuthInterceptor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.getToken().trim()
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
