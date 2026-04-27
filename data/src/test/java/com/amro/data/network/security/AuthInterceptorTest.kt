package com.amro.data.network.security

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `interceptor fails fast when token is blank`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider = { " " }))
            .build()
        val request = Request.Builder()
            .url(server.url("/movie"))
            .build()

        val error = assertThrows(IOException::class.java) {
            client.newCall(request).execute()
        }

        assertEquals(
            "TMDB_BEARER_TOKEN is blank. Provide it via Gradle properties.",
            error.message,
        )
    }

    @Test
    fun `interceptor always adds authorization when token exists`() {
        server.enqueue(MockResponse().setResponseCode(200))
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider = { "token" }))
            .build()
        val request = Request.Builder()
            .url(server.url("/movie"))
            .build()

        client.newCall(request).execute().close()

        val recordedRequest = server.takeRequest()
        assertEquals("application/json", recordedRequest.getHeader("Accept"))
        assertEquals("Bearer token", recordedRequest.getHeader("Authorization"))
    }
}
