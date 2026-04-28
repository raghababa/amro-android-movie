package com.amro.data.integration

import com.amro.data.di.NetworkModule
import com.amro.data.image.TmdbImageUrlBuilder
import com.amro.data.network.security.AuthInterceptor
import com.amro.data.network.tmdb.TmdbApi
import com.amro.data.remote.TmdbRemoteDataSourceImpl
import com.amro.data.repository.DefaultGenreLanguageResolver
import com.amro.data.repository.InMemoryGenreCache
import com.amro.data.repository.MovieRepositoryImpl
import com.amro.data.repository.TrendingMoviesConfig
import com.amro.domain.repository.LanguageCode
import com.amro.domain.repository.TimeWindow
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

class TmdbDataIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = NetworkModule.provideJson()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { "test-token" })
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val api = retrofit.create(TmdbApi::class.java)
        val remoteDataSource = TmdbRemoteDataSourceImpl(api)

        repository = MovieRepositoryImpl(
            remoteDataSource = remoteDataSource,
            imageUrlBuilder = TmdbImageUrlBuilder("https://image.tmdb.org/t/p/"),
            genreCache = InMemoryGenreCache(),
            genreLanguageResolver = DefaultGenreLanguageResolver(),
            trendingMoviesConfig = TrendingMoviesConfig(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `trending flow integrates auth retrofit remote source repository and mappers`() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {
                  "genres": [
                    { "id": 28, "name": "Action" },
                    { "id": 35, "name": "Comedy" }
                  ],
                  "ignored_field": true
                }
                """.trimIndent()
            )
        )
        server.enqueue(
            jsonResponse(
                """
                {
                  "page": 1,
                  "total_pages": 1,
                  "results": [
                    {
                      "id": 10,
                      "title": "Apex",
                      "poster_path": "/apex.jpg",
                      "backdrop_path": "/apex-backdrop.jpg",
                      "genre_ids": [28, 999],
                      "popularity": 90.5,
                      "release_date": "2026-02-01"
                    },
                    {
                      "id": 11,
                      "title": "Family Night",
                      "poster_path": "/family.jpg",
                      "backdrop_path": null,
                      "genre_ids": [35],
                      "popularity": 80.0,
                      "release_date": "2026-01-15"
                    }
                  ]
                }
                """.trimIndent()
            )
        )

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        val movies = (result as DomainResult.Success).value
        assertEquals(listOf(10L, 11L), movies.map { it.id })
        assertEquals(listOf("Action"), movies.first().genres.map { it.name })
        assertEquals("https://image.tmdb.org/t/p/w185/apex.jpg", movies.first().posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/apex-backdrop.jpg", movies.first().backdropUrl)

        val genresRequest = server.takeRequest()
        assertEquals("/genre/movie/list?language=en", genresRequest.path)
        assertEquals("Bearer test-token", genresRequest.getHeader("Authorization"))
        assertEquals("application/json", genresRequest.getHeader("Accept"))

        val trendingRequest = server.takeRequest()
        assertEquals("/trending/movie/week?language=en-US&page=1", trendingRequest.path)
        assertEquals("Bearer test-token", trendingRequest.getHeader("Authorization"))
    }

    @Test
    fun `trending flow paginates deduplicates and reuses cached genres`() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {
                  "genres": [
                    { "id": 28, "name": "Action" },
                    { "id": 35, "name": "Comedy" }
                  ]
                }
                """.trimIndent()
            )
        )
        server.enqueue(
            trendingResponse(
                page = 1,
                totalPages = 2,
                moviesJson = """
                [
                  { "id": 10, "title": "Apex", "genre_ids": [28], "popularity": 90.0 },
                  { "id": 11, "title": "Echo", "genre_ids": [35], "popularity": 80.0 }
                ]
                """.trimIndent(),
            )
        )
        server.enqueue(
            trendingResponse(
                page = 2,
                totalPages = 2,
                moviesJson = """
                [
                  { "id": 11, "title": "Echo Duplicate", "genre_ids": [35], "popularity": 80.0 },
                  { "id": 12, "title": "Nova", "genre_ids": [28], "popularity": 70.0 }
                ]
                """.trimIndent(),
            )
        )
        server.enqueue(
            trendingResponse(
                page = 1,
                totalPages = 1,
                moviesJson = """
                [
                  { "id": 13, "title": "Orbit", "genre_ids": [35], "popularity": 60.0 }
                ]
                """.trimIndent(),
            )
        )

        val firstResult = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)
        val secondResult = repository.getTrendingMovies(timeWindow = TimeWindow.DAY, language = LanguageCode.EN_US)

        assertTrue(firstResult is DomainResult.Success)
        val firstMovies = (firstResult as DomainResult.Success).value
        assertEquals(listOf(10L, 11L, 12L), firstMovies.map { it.id })
        assertEquals(listOf("Action"), firstMovies.first().genres.map { it.name })
        assertTrue(secondResult is DomainResult.Success)
        assertEquals(listOf(13L), (secondResult as DomainResult.Success).value.map { it.id })

        assertEquals("/genre/movie/list?language=en", server.takeRequest().path)
        assertEquals("/trending/movie/week?language=en-US&page=1", server.takeRequest().path)
        assertEquals("/trending/movie/week?language=en-US&page=2", server.takeRequest().path)
        assertEquals("/trending/movie/day?language=en-US&page=1", server.takeRequest().path)
    }

    @Test
    fun `trending flow continues without genres when genre endpoint fails`() = runTest {
        server.enqueue(jsonResponse("""{"status_message":"Temporary failure"}""", code = 500))
        server.enqueue(
            trendingResponse(
                page = 1,
                totalPages = 1,
                moviesJson = """
                [
                  { "id": 10, "title": "Apex", "genre_ids": [28], "popularity": 90.0 }
                ]
                """.trimIndent(),
            )
        )

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        val movie = (result as DomainResult.Success).value.single()
        assertEquals(10L, movie.id)
        assertTrue(movie.genres.isEmpty())
        assertEquals("/genre/movie/list?language=en", server.takeRequest().path)
        assertEquals("/trending/movie/week?language=en-US&page=1", server.takeRequest().path)
    }

    @Test
    fun `detail flow integrates retrofit remote source repository and detail mapper`() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {
                  "id": 42,
                  "title": "The Matrix",
                  "tagline": "Welcome to the Real World.",
                  "overview": "A hacker discovers reality is a simulation.",
                  "poster_path": "/matrix.jpg",
                  "backdrop_path": "/matrix-backdrop.jpg",
                  "genres": [
                    { "id": 878, "name": "Science Fiction" },
                    { "id": 28, "name": "Action" }
                  ],
                  "vote_average": 8.2,
                  "vote_count": 25000,
                  "budget": 63000000,
                  "revenue": 467000000,
                  "status": "Released",
                  "imdb_id": "tt0133093",
                  "runtime": 136,
                  "release_date": "1999-03-31"
                }
                """.trimIndent()
            )
        )

        val result = repository.getMovieDetail(movieId = 42, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        val movie = (result as DomainResult.Success).value
        assertEquals(42L, movie.id)
        assertEquals("The Matrix", movie.title)
        assertEquals(listOf("Science Fiction", "Action"), movie.genres.map { it.name })
        assertEquals("https://image.tmdb.org/t/p/w500/matrix.jpg", movie.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/matrix-backdrop.jpg", movie.backdropUrl)
        assertEquals("tt0133093", movie.imdbId)

        val request = server.takeRequest()
        assertEquals("/movie/42?language=en-US", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
    }

    @Test
    fun `remote API error is exposed as domain error through repository`() = runTest {
        server.enqueue(jsonResponse("""{"status_message":"Invalid token"}""", code = 401))

        val result = repository.getMovieDetail(movieId = 42, language = LanguageCode.EN_US)

        assertEquals(DomainResult.Error(DomainError.Unauthorized), result)
    }

    @Test
    fun `detail flow maps not found through remote source and repository`() = runTest {
        server.enqueue(jsonResponse("""{"status_message":"Movie not found"}""", code = 404))

        val result = repository.getMovieDetail(movieId = 404, language = LanguageCode.EN_US)

        assertEquals(DomainResult.Error(DomainError.NotFound), result)
        val request = server.takeRequest()
        assertEquals("/movie/404?language=en-US", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
    }

    private fun jsonResponse(body: String, code: Int = 200): MockResponse =
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody(body)

    private fun trendingResponse(
        page: Int,
        totalPages: Int,
        moviesJson: String,
    ): MockResponse =
        jsonResponse(
            """
            {
              "page": $page,
              "total_pages": $totalPages,
              "results": $moviesJson
            }
            """.trimIndent()
        )
}
