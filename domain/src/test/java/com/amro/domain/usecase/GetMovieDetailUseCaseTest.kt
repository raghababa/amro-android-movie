package com.amro.domain.usecase

import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.domain.model.MovieSummary
import com.amro.domain.repository.LanguageCode
import com.amro.domain.repository.MovieRepository
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetMovieDetailUseCaseTest {

    @Test
    fun `invalid movie id returns invalid input and does not call repository`() = runTest {
        val repository = FakeMovieRepository()
        val useCase = GetMovieDetailUseCase(repository)

        val result = useCase(movieId = 0)

        assertEquals(0, repository.detailCalls)
        assertTrue(result is DomainResult.Error)
        assertEquals(DomainError.InvalidInput(field = "movieId"), (result as DomainResult.Error).error)
    }

    @Test
    fun `valid movie id delegates to repository`() = runTest {
        val repository = FakeMovieRepository()
        val useCase = GetMovieDetailUseCase(repository)

        val result = useCase(movieId = 42, language = LanguageCode("nl-NL"))

        assertEquals(1, repository.detailCalls)
        assertEquals(42L, repository.lastMovieId)
        assertEquals(LanguageCode("nl-NL"), repository.lastLanguage)
        assertTrue(result is DomainResult.Success)
    }

    private class FakeMovieRepository : MovieRepository {
        var detailCalls = 0
        var lastMovieId: Long? = null
        var lastLanguage: LanguageCode? = null

        override suspend fun getTrendingMovies(
            timeWindow: TimeWindow,
            language: LanguageCode,
        ): DomainResult<List<MovieSummary>> =
            DomainResult.Success(emptyList())

        override suspend fun getMovieGenres(language: LanguageCode): DomainResult<List<Genre>> =
            DomainResult.Success(emptyList())

        override suspend fun getMovieDetail(
            movieId: Long,
            language: LanguageCode,
        ): DomainResult<MovieDetail> {
            detailCalls++
            lastMovieId = movieId
            lastLanguage = language
            return DomainResult.Success(
                MovieDetail(
                    id = movieId,
                    title = "Movie $movieId",
                    tagline = null,
                    overview = null,
                    posterUrl = null,
                    backdropUrl = null,
                    genres = emptyList(),
                    voteAverage = 0.0,
                    voteCount = 0,
                    budget = 0,
                    revenue = 0,
                    status = null,
                    imdbId = null,
                    runtimeMinutes = null,
                    releaseDate = null,
                )
            )
        }
    }
}
