package com.amro.data.network

import com.amro.data.di.NetworkModule
import com.amro.data.network.security.MissingTmdbTokenException
import com.amro.data.network.tmdb.TmdbApi
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertThrows
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import kotlinx.serialization.SerializationException

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
    fun `apiCall maps 401 to unauthorized non-retryable`() = runTest {
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
        assertEquals(DomainError.Unauthorized, error)
        assertTrue(error.isRetryable.not())
    }

    @Test
    fun `apiCall maps 500 to server retryable`() = runTest {
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

        val error = (result as DomainResult.Error).error
        assertEquals(DomainError.Server, error)
        assertTrue(error.isRetryable)
    }

    @Test
    fun `apiCall returns success body`() = runTest {
        val result = apiCall { Response.success("ok") }

        assertEquals(DomainResult.Success("ok"), result)
    }

    @Test
    fun `apiCall maps successful null body to unexpected empty error`() = runTest {
        val result = apiCall<String> { Response.success(null) }

        assertEquals(
            DomainResult.Error(DomainError.UnexpectedEmpty("Response body")),
            result,
        )
    }

    @Test
    fun `apiCall maps 404 to not found`() = runTest {
        val result = apiCall<String> { Response.error(404, responseBody()) }

        assertEquals(DomainResult.Error(DomainError.NotFound), result)
    }

    @Test
    fun `apiCall maps 429 to rate limited`() = runTest {
        val result = apiCall<String> { Response.error(429, responseBody()) }

        assertEquals(DomainResult.Error(DomainError.RateLimited), result)
    }

    @Test
    fun `apiCall maps unhandled http code to unknown`() = runTest {
        val result = apiCall<String> { Response.error(418, responseBody()) }

        assertTrue(result is DomainResult.Error)
        assertTrue((result as DomainResult.Error).error is DomainError.Unknown)
    }

    @Test
    fun `apiCall maps io exception to network error`() = runTest {
        val cause = IOException("No connection")
        val result = apiCall<String> { throw cause }

        assertTrue(result is DomainResult.Error)
        val error = (result as DomainResult.Error).error
        assertTrue(error is DomainError.Network)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `apiCall maps serialization exception to serialization error`() = runTest {
        val cause = SerializationException("Bad json")
        val result = apiCall<String> { throw cause }

        assertTrue(result is DomainResult.Error)
        val error = (result as DomainResult.Error).error
        assertTrue(error is DomainError.Serialization)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `apiCall maps missing token exception to configuration error`() = runTest {
        val cause = MissingTmdbTokenException()
        val result = apiCall<String> { throw cause }

        assertTrue(result is DomainResult.Error)
        val error = (result as DomainResult.Error).error
        assertTrue(error is DomainError.Configuration)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `apiCall rethrows cancellation exception`() {
        assertThrows(CancellationException::class.java) {
            runTest {
                apiCall<String> { throw CancellationException("Cancelled") }
            }
        }
    }

    private fun responseBody() =
        """{"status_message":"Error"}""".toResponseBody("application/json".toMediaType())
}

