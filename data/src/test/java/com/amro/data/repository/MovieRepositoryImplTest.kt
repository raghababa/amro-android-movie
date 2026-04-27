package com.amro.data.repository

import com.amro.data.image.TmdbImageUrlBuilder
import com.amro.data.network.tmdb.dto.TmdbGenreDto
import com.amro.data.network.tmdb.dto.TmdbGenresResponse
import com.amro.data.network.tmdb.dto.TmdbMovieDetailDto
import com.amro.data.network.tmdb.dto.TmdbTrendingMovieDto
import com.amro.data.network.tmdb.dto.TmdbTrendingMoviesResponse
import com.amro.data.remote.TmdbRemoteDataSource
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieRepositoryImplTest {

    private val imageUrlBuilder = TmdbImageUrlBuilder("https://image.tmdb.org/t/p/")

    @Test
    fun `getTrendingMovies caches genres by language`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(totalPages = 1)
        val repository = MovieRepositoryImpl(remoteDataSource, imageUrlBuilder)

        repository.getTrendingMovies(timeWindow = TimeWindow.DAY, language = "en-US")
        repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = "en-US")

        assertEquals(listOf("en"), remoteDataSource.genreLanguages)
    }

    @Test
    fun `getTrendingMovies stops when current page reaches total pages`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(totalPages = 1)
        val repository = MovieRepositoryImpl(remoteDataSource, imageUrlBuilder)

        repository.getTrendingMovies(timeWindow = TimeWindow.DAY, language = "en-US")

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
        val repository = MovieRepositoryImpl(remoteDataSource, imageUrlBuilder)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = "en-US")

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
        val repository = MovieRepositoryImpl(remoteDataSource, imageUrlBuilder)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = "en-US")

        assertTrue(result is DomainResult.Success)
        val movies = (result as DomainResult.Success).value
        assertEquals(100, movies.size)
        assertEquals(1L, movies.first().id)
        assertEquals(100L, movies.last().id)
    }

    @Test
    fun `getTrendingMovies returns empty error when API returns no movies`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(
            trendingByPage = mapOf(1 to emptyList()),
            totalPages = 1,
        )
        val repository = MovieRepositoryImpl(remoteDataSource, imageUrlBuilder)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = "en-US")

        assertEquals(DomainResult.Error(DomainError.Empty("Trending movies")), result)
    }

    @Test
    fun `getTrendingMovies returns genre error before requesting trending movies`() = runTest {
        val remoteDataSource = FakeTmdbRemoteDataSource(
            genresResult = DomainResult.Error(DomainError.Network()),
        )
        val repository = MovieRepositoryImpl(remoteDataSource, imageUrlBuilder)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = "en-US")

        assertEquals(DomainResult.Error(DomainError.Network()), result)
        assertEquals(emptyList<Int>(), remoteDataSource.trendingPages)
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
        val repository = MovieRepositoryImpl(remoteDataSource, imageUrlBuilder)

        val result = repository.getTrendingMovies(timeWindow = TimeWindow.WEEK, language = "en-US")

        assertTrue(result is DomainResult.Success)
        val movie = (result as DomainResult.Success).value.single()
        assertEquals("https://image.tmdb.org/t/p/w185/poster.jpg", movie.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", movie.backdropUrl)
        assertEquals(listOf("Action"), movie.genres.map { it.name })
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
        val repository = MovieRepositoryImpl(remoteDataSource, imageUrlBuilder)

        val result = repository.getMovieDetail(movieId = 42, language = "en-US")

        assertTrue(result is DomainResult.Success)
        val detail = (result as DomainResult.Success).value
        assertEquals(42L, detail.id)
        assertEquals("The Matrix", detail.title)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", detail.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", detail.backdropUrl)
        assertEquals(listOf("Science Fiction"), detail.genres.map { it.name })
        assertEquals("tt0133093", detail.imdbId)
    }

    private class FakeTmdbRemoteDataSource(
        private val totalPages: Int = 1,
        private val trendingByPage: Map<Int, List<TmdbTrendingMovieDto>> = emptyMap(),
        private val genresResult: DomainResult<TmdbGenresResponse> = DomainResult.Success(
            TmdbGenresResponse(genres = listOf(TmdbGenreDto(id = 28, name = "Action")))
        ),
        private val detailResult: DomainResult<TmdbMovieDetailDto> = DomainResult.Success(
            TmdbMovieDetailDto(id = 1, title = "Movie 1")
        ),
    ) : TmdbRemoteDataSource {
        val genreLanguages = mutableListOf<String>()
        val trendingPages = mutableListOf<Int>()

        override suspend fun getTrendingMovies(
            timeWindow: String,
            language: String,
            page: Int,
        ): DomainResult<TmdbTrendingMoviesResponse> {
            trendingPages += page
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
        ): DomainResult<TmdbMovieDetailDto> = detailResult
    }

    private companion object {
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
