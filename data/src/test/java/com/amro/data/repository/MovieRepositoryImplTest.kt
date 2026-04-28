package com.amro.data.repository

import com.amro.data.image.TmdbImageUrlBuilder
import com.amro.data.network.tmdb.dto.TmdbGenreDto
import com.amro.data.network.tmdb.dto.TmdbGenresResponse
import com.amro.data.network.tmdb.dto.TmdbMovieDetailDto
import com.amro.data.network.tmdb.dto.TmdbTrendingMovieDto
import com.amro.data.network.tmdb.dto.TmdbTrendingMoviesResponse
import com.amro.data.remote.TmdbRemoteDataSource
import com.amro.domain.repository.LanguageCode
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieRepositoryImplTest {

    @Test
    fun `getTrendingMovies caches genres by language`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(totalPages = 1)
        val repository = repository(remoteDataSource)

        repository.getTrendingMovies(timeWindow = TimeWindow.DAY, language = LanguageCode.EN_US)
        repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertEquals(listOf("en"), remoteDataSource.genreLanguages)
    }

    @Test
    fun `getTrendingMovies stops when current page reaches total pages`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(totalPages = 1)
        val repository = repository(remoteDataSource)

        repository.getTrendingMovies(timeWindow = TimeWindow.DAY, language = LanguageCode.EN_US)

        assertEquals(listOf(1), remoteDataSource.trendingPages)
    }

    @Test
    fun `getTrendingMovies stops when total pages is null`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(totalPages = null)
        val repository = repository(remoteDataSource)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.DAY, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        assertEquals(listOf(1L), (result as DomainResult.Success).value.map { it.id })
        assertEquals(listOf(1), remoteDataSource.trendingPages)
    }

    @Test
    fun `getTrendingMovies deduplicates movies across pages`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(
            trendingByPage = mapOf(
                1 to listOf(trendingMovie(id = 1), trendingMovie(id = 2)),
                2 to listOf(trendingMovie(id = 2), trendingMovie(id = 3)),
            ),
            totalPages = 2,
        )
        val repository = repository(remoteDataSource)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        assertEquals(listOf(1L, 2L, 3L), (result as DomainResult.Success).value.map { it.id })
    }

    @Test
    fun `getTrendingMovies caps results at top 100`() = runTest {
        val trendingByPage = (1..5).associateWith { page ->
            (1..25).map { index -> trendingMovie(id = ((page - 1) * 25 + index).toLong()) }
        }
        val remoteDataSource = FakeTmdbRemoteDataSource(
            trendingByPage = trendingByPage,
            totalPages = 5,
        )
        val repository = repository(remoteDataSource)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        val movies = (result as DomainResult.Success).value
        assertEquals(100, movies.size)
        assertEquals(1L, movies.first().id)
        assertEquals(100L, movies.last().id)
    }

    @Test
    fun `getTrendingMovies keeps requesting pages until result limit is reached`() = runTest {
        val trendingByPage = (1..6).associateWith { page ->
            (1..17).map { index -> trendingMovie(id = ((page - 1) * 17 + index).toLong()) }
        }
        val remoteDataSource = FakeTmdbRemoteDataSource(
            trendingByPage = trendingByPage,
            totalPages = 6,
        )
        val repository = repository(remoteDataSource)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        val movies = (result as DomainResult.Success).value
        assertEquals(100, movies.size)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), remoteDataSource.trendingPages)
        assertEquals(100L, movies.last().id)
    }

    @Test
    fun `getTrendingMovies uses configured result limit`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(
            trendingByPage = mapOf(
                1 to listOf(trendingMovie(id = 1), trendingMovie(id = 2)),
                2 to listOf(trendingMovie(id = 3), trendingMovie(id = 4)),
            ),
            totalPages = 2,
        )
        val repository = repository(
            remoteDataSource = remoteDataSource,
            trendingMoviesConfig = TrendingMoviesConfig(movieLimit = 3),
        )

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        val movies = (result as DomainResult.Success).value
        assertEquals(listOf(1L, 2L, 3L), movies.map { it.id })
        assertEquals(listOf(1, 2), remoteDataSource.trendingPages)
    }

    @Test
    fun `getTrendingMovies returns empty error when API returns no movies`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(
            trendingByPage = mapOf(1 to emptyList()),
            totalPages = 1,
        )
        val repository = repository(remoteDataSource)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertEquals(DomainResult.Error(DomainError.UnexpectedEmpty("Trending movies")), result)
    }

    @Test
    fun `getTrendingMovies continues with empty genres when genres fail`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(
            genresResult = DomainResult.Error(DomainError.Network()),
        )
        val repository = repository(remoteDataSource)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        val movie = (result as DomainResult.Success).value.single()
        assertEquals(emptyList<String>(), movie.genres.map { it.name })
        assertEquals(listOf(1), remoteDataSource.trendingPages)
    }

    @Test
    fun `getTrendingMovies returns movie page error unchanged`() = runTest {
        val expectedResult = DomainResult.Error(DomainError.Server)
        val remoteDataSource = FakeTmdbRemoteDataSource(
            trendingResultByPage = mapOf(1 to expectedResult),
        )
        val repository = repository(remoteDataSource)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertEquals(expectedResult, result)
        assertEquals(listOf(1), remoteDataSource.trendingPages)
    }

    @Test
    fun `getTrendingMovies maps images and resolved genres`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(
            trendingByPage = mapOf(
                1 to listOf(
                    trendingMovie(
                        id = 1,
                        posterPath = "/poster.jpg",
                        backdropPath = "/backdrop.jpg",
                        genreIds = listOf(28, 999),
                    )
                )
            ),
            totalPages = 1,
        )
        val repository = repository(remoteDataSource)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        val movie = (result as DomainResult.Success).value.single()
        assertEquals("https://image.tmdb.org/t/p/w185/poster.jpg", movie.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", movie.backdropUrl)
        assertEquals(listOf("Action"), movie.genres.map { it.name })
    }

    @Test
    fun `getMovieGenres fetches requested language once then serves cache`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource()
        val repository = repository(remoteDataSource)

        val firstResult = repository.getMovieGenres(language = LanguageCode("nl-NL"))
        val secondResult = repository.getMovieGenres(language = LanguageCode("nl-NL"))

        assertTrue(firstResult is DomainResult.Success)
        assertTrue(secondResult is DomainResult.Success)
        assertEquals(listOf("nl-NL"), remoteDataSource.genreLanguages)
    }

    @Test
    fun `getMovieGenres returns genre fetch error without caching`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(
            genresResult = DomainResult.Error(DomainError.Network()),
        )
        val repository = repository(remoteDataSource)

        val result = repository.getMovieGenres(language = LanguageCode.EN)

        assertEquals(DomainResult.Error(DomainError.Network()), result)
        assertEquals(listOf("en"), remoteDataSource.genreLanguages)
    }

    @Test
    fun `getMovieDetail maps detail and image urls`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(
            detailResult = DomainResult.Success(
                TmdbMovieDetailDto(
                    id = 42,
                    title = "The Matrix",
                    posterPath = "/poster.jpg",
                    backdropPath = "/backdrop.jpg",
                    genres = listOf(TmdbGenreDto(id = 878, name = "Science Fiction")),
                    voteAverage = 8.2,
                    voteCount = 25000,
                    budget = 63_000_000,
                    revenue = 467_000_000,
                    status = "Released",
                    imdbId = "tt0133093",
                    runtimeMinutes = 136,
                    releaseDate = "1999-03-31",
                )
            )
        )
        val repository = repository(remoteDataSource)

        val result = repository.getMovieDetail(movieId = 42, language = LanguageCode.EN_US)

        assertTrue(result is DomainResult.Success)
        val detail = (result as DomainResult.Success).value
        assertEquals(42L, detail.id)
        assertEquals("The Matrix", detail.title)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", detail.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", detail.backdropUrl)
        assertEquals(listOf("Science Fiction"), detail.genres.map { it.name })
        assertEquals("tt0133093", detail.imdbId)
    }

    @Test
    fun `getMovieDetail delegates id and language and returns errors unchanged`() = runTest {
        val expectedResult = DomainResult.Error(DomainError.NotFound)
        val remoteDataSource = FakeTmdbRemoteDataSource(detailResult = expectedResult)
        val repository = repository(remoteDataSource)

        val result = repository.getMovieDetail(movieId = 42, language = LanguageCode("nl-NL"))

        assertEquals(expectedResult, result)
        assertEquals(42L, remoteDataSource.lastDetailMovieId)
        assertEquals("nl-NL", remoteDataSource.lastDetailLanguage)
    }

    private class FakeTmdbRemoteDataSource(
        private val totalPages: Int? = 1,
        private val trendingByPage: Map<Int, List<TmdbTrendingMovieDto>> = emptyMap(),
        private val trendingResultByPage: Map<Int, DomainResult<TmdbTrendingMoviesResponse>> = emptyMap(),
        private val genresResult: DomainResult<TmdbGenresResponse> = DomainResult.Success(
            TmdbGenresResponse(genres = listOf(TmdbGenreDto(id = 28, name = "Action")))
        ),
        private val detailResult: DomainResult<TmdbMovieDetailDto> = DomainResult.Success(
            TmdbMovieDetailDto(id = 1, title = "Movie 1")
        ),
    ) : TmdbRemoteDataSource {
        val genreLanguages = mutableListOf<String>()
        val trendingPages = mutableListOf<Int>()
        var lastDetailMovieId: Long? = null
            private set
        var lastDetailLanguage: String? = null
            private set

        override suspend fun getTrendingMovies(
            timeWindow: String,
            language: String,
            page: Int,
        ): DomainResult<TmdbTrendingMoviesResponse> {
            trendingPages += page
            trendingResultByPage[page]?.let { return it }
            return DomainResult.Success(
                TmdbTrendingMoviesResponse(
                    page = page,
                    results = trendingByPage[page] ?: listOf(trendingMovie(id = page.toLong())),
                    totalPages = totalPages,
                )
            )
        }

        override suspend fun getGenres(language: String): DomainResult<TmdbGenresResponse> {
            genreLanguages += language
            return genresResult
        }

        override suspend fun getMovieDetail(
            movieId: Long,
            language: String,
        ): DomainResult<TmdbMovieDetailDto> {
            lastDetailMovieId = movieId
            lastDetailLanguage = language
            return detailResult
        }
    }

    private companion object {
        fun repository(
            remoteDataSource: TmdbRemoteDataSource,
            trendingMoviesConfig: TrendingMoviesConfig = TrendingMoviesConfig(),
        ): MovieRepositoryImpl =
            MovieRepositoryImpl(
                remoteDataSource = remoteDataSource,
                imageUrlBuilder = TmdbImageUrlBuilder("https://image.tmdb.org/t/p/"),
                genreCache = InMemoryGenreCache(),
                genreLanguageResolver = DefaultGenreLanguageResolver(),
                trendingMoviesConfig = trendingMoviesConfig,
            )

        fun trendingMovie(
            id: Long,
            posterPath: String? = null,
            backdropPath: String? = null,
            genreIds: List<Int> = listOf(28),
        ): TmdbTrendingMovieDto =
            TmdbTrendingMovieDto(
                id = id,
                title = "Movie $id",
                posterPath = posterPath,
                backdropPath = backdropPath,
                genreIds = genreIds,
            )
    }
}
