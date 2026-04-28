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
import org.junit.Assert.assertSame
import org.junit.Test

class GetTrendingMoviesUseCaseTest {

    @Test
    fun `delegates time window and language to repository`() = runTest {
        val expectedResult = DomainResult.Success(listOf(movie(id = 1)))
        val repository = FakeMovieRepository(trendingResult = expectedResult)
        val useCase = GetTrendingMoviesUseCase(repository)

        val result = useCase(timeWindow = TimeWindow.DAY, language = LanguageCode("nl-NL"))

        assertSame(expectedResult, result)
        assertEquals(TimeWindow.DAY, repository.lastTimeWindow)
        assertEquals(LanguageCode("nl-NL"), repository.lastTrendingLanguage)
        assertEquals(1, repository.trendingCalls)
    }

    @Test
    fun `uses english united states as default language`() = runTest {
        val repository = FakeMovieRepository()
        val useCase = GetTrendingMoviesUseCase(repository)

        useCase(timeWindow = TimeWindow.WEEK)

        assertEquals(LanguageCode.EN_US, repository.lastTrendingLanguage)
    }

    @Test
    fun `returns repository error unchanged`() = runTest {
        val expectedResult = DomainResult.Error(DomainError.Server)
        val repository = FakeMovieRepository(trendingResult = expectedResult)
        val useCase = GetTrendingMoviesUseCase(repository)

        val result = useCase(timeWindow = TimeWindow.WEEK)

        assertSame(expectedResult, result)
    }

    private class FakeMovieRepository(
        private val trendingResult: DomainResult<List<MovieSummary>> = DomainResult.Success(emptyList()),
    ) : MovieRepository {
        var trendingCalls = 0
            private set
        var lastTimeWindow: TimeWindow? = null
            private set
        var lastTrendingLanguage: LanguageCode? = null
            private set

        override suspend fun getTrendingMovies(
            timeWindow: TimeWindow,
            language: LanguageCode,
        ): DomainResult<List<MovieSummary>> {
            trendingCalls++
            lastTimeWindow = timeWindow
            lastTrendingLanguage = language
            return trendingResult
        }

        override suspend fun getMovieGenres(language: LanguageCode): DomainResult<List<Genre>> =
            error("Genres are not used by GetTrendingMoviesUseCase")

        override suspend fun getMovieDetail(
            movieId: Long,
            language: LanguageCode,
        ): DomainResult<MovieDetail> =
            error("Movie detail is not used by GetTrendingMoviesUseCase")
    }

    private companion object {
        fun movie(id: Long): MovieSummary =
            MovieSummary(
                id = id,
                title = "Movie $id",
                posterUrl = null,
                backdropUrl = null,
                genres = emptyList(),
                popularity = 0.0,
                releaseDate = null,
            )
    }
}
