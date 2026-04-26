package com.amro.data.network

import com.amro.data.di.NetworkModule
import com.amro.data.network.tmdb.TmdbApi
import com.amro.data.network.tmdb.dto.TmdbGenresResponse
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

class ApiCallTest {

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
    fun `apiCall maps 401 to DomainError_Http non-retryable`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"status_message":"Invalid token"}"""))

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(
                NetworkModule.provideJson().asConverterFactory("application/json".toMediaType())
            )
            .build()

        val api = retrofit.create(TmdbApi::class.java)

        val result = apiCall { api.getMovieGenres(language = "en") }

        assertTrue(result is DomainResult.Error)
        val error = (result as DomainResult.Error).error
        assertTrue(error is DomainError.Http)
        val http = error as DomainError.Http
        assertEquals(401, http.code)
        assertTrue(http.isRetryable.not())
    }

    @Test
    fun `apiCall maps 500 to DomainError_Http retryable`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"status_message":"Oops"}"""))

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(
                NetworkModule.provideJson().asConverterFactory("application/json".toMediaType())
            )
            .build()

        val api = retrofit.create(TmdbApi::class.java)

        val result = apiCall { api.getMovieGenres(language = "en") }

        val error = (result as DomainResult.Error).error as DomainError.Http
        assertEquals(500, error.code)
        assertTrue(error.isRetryable)
    }
}

