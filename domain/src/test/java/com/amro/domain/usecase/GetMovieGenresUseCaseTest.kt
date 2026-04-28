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

class GetMovieGenresUseCaseTest {

    @Test
    fun `delegates language to repository`() = runTest {
        val expectedResult = DomainResult.Success(listOf(Genre(id = 28, name = "Action")))
        val repository = FakeMovieRepository(genresResult = expectedResult)
        val useCase = GetMovieGenresUseCase(repository)

        val result = useCase(language = LanguageCode("nl-NL"))

        assertSame(expectedResult, result)
        assertEquals(LanguageCode("nl-NL"), repository.lastGenresLanguage)
        assertEquals(1, repository.genreCalls)
    }

    @Test
    fun `uses english as default language`() = runTest {
        val repository = FakeMovieRepository()
        val useCase = GetMovieGenresUseCase(repository)

        useCase()

        assertEquals(LanguageCode.EN, repository.lastGenresLanguage)
    }

    @Test
    fun `returns repository error unchanged`() = runTest {
        val expectedResult = DomainResult.Error(DomainError.Network())
        val repository = FakeMovieRepository(genresResult = expectedResult)
        val useCase = GetMovieGenresUseCase(repository)

        val result = useCase()

        assertSame(expectedResult, result)
    }

    private class FakeMovieRepository(
        private val genresResult: DomainResult<List<Genre>> = DomainResult.Success(emptyList()),
    ) : MovieRepository {
        var genreCalls = 0
            private set
        var lastGenresLanguage: LanguageCode? = null
            private set

        override suspend fun getTrendingMovies(
            timeWindow: TimeWindow,
            language: LanguageCode,
        ): DomainResult<List<MovieSummary>> =
            error("Trending movies are not used by GetMovieGenresUseCase")

        override suspend fun getMovieGenres(language: LanguageCode): DomainResult<List<Genre>> {
            genreCalls++
            lastGenresLanguage = language
            return genresResult
        }

        override suspend fun getMovieDetail(
            movieId: Long,
            language: LanguageCode,
        ): DomainResult<MovieDetail> =
            error("Movie detail is not used by GetMovieGenresUseCase")
    }
}
