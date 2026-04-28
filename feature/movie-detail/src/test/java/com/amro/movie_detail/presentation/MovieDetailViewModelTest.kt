package com.amro.movie_detail.presentation

import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.domain.model.MovieSummary
import com.amro.domain.repository.LanguageCode
import com.amro.domain.repository.MovieRepository
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import com.amro.domain.usecase.GetMovieDetailUseCase
import com.amro.movie_detail.mapper.CurrencyFormatter
import com.amro.movie_detail.mapper.DateFormatter
import com.amro.movie_detail.mapper.MovieDetailUiMapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load emits content when movie detail succeeds`() = runTest {
        val viewModel = viewModel(
            detailResult = DomainResult.Success(movieDetail(id = 42, title = "The Matrix"))
        )

        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieDetailUiState.Content
        assertEquals(42L, state.movie.id)
        assertEquals("The Matrix", state.movie.title)
        assertEquals(listOf("Science Fiction"), state.movie.genres)
    }

    @Test
    fun `load emits retryable error when repository returns network error`() = runTest {
        val viewModel = viewModel(
            detailResult = DomainResult.Error(DomainError.Network())
        )

        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieDetailUiState.Error
        assertTrue(state.isRetryable)
    }

    @Test
    fun `load emits non retryable error when repository returns not found`() = runTest {
        val viewModel = viewModel(
            detailResult = DomainResult.Error(DomainError.NotFound)
        )

        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieDetailUiState.Error
        assertTrue(state.isRetryable.not())
    }

    @Test
    fun `load skips same movie when content is already loaded`() = runTest {
        val repository = FakeMovieRepository(
            detailResult = DomainResult.Success(movieDetail(id = 42))
        )
        val viewModel = viewModel(repository = repository)

        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.detailRequests)
    }

    @Test
    fun `force refresh reloads same movie`() = runTest {
        val repository = FakeMovieRepository(
            detailResult = DomainResult.Success(movieDetail(id = 42))
        )
        val viewModel = viewModel(repository = repository)

        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.load(movieId = 42, forceRefresh = true)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, repository.detailRequests)
    }

    @Test
    fun `retry reloads last requested movie`() = runTest {
        val repository = FakeMovieRepository(
            detailResult = DomainResult.Success(movieDetail(id = 42, title = "The Matrix"))
        )
        val viewModel = viewModel(repository = repository)

        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()
        val previousMovie = (viewModel.uiState.value as MovieDetailUiState.Content).movie

        viewModel.retry()

        val state = viewModel.uiState.value as MovieDetailUiState.Loading
        assertEquals(previousMovie, state.previousData)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, repository.detailRequests)
    }

    @Test
    fun `retry before any load does nothing`() = runTest {
        val repository = FakeMovieRepository(
            detailResult = DomainResult.Success(movieDetail(id = 42, title = "The Matrix"))
        )
        val viewModel = viewModel(repository = repository)

        viewModel.retry()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, repository.detailRequests)
        assertTrue(viewModel.uiState.value is MovieDetailUiState.Loading)
    }

    @Test
    fun `retry after error integrates use case mapper and emits content`() = runTest {
        val repository = FakeMovieRepository(
            detailResult = DomainResult.Error(DomainError.Network())
        )
        val viewModel = viewModel(repository = repository)
        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is MovieDetailUiState.Error)

        repository.detailResult = DomainResult.Success(movieDetail(id = 42, title = "The Matrix"))
        viewModel.retry()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieDetailUiState.Content
        assertEquals(42L, state.movie.id)
        assertEquals("The Matrix", state.movie.title)
        assertEquals("$1", state.movie.budget)
        assertEquals(2, repository.detailRequests)
    }

    @Test
    fun `force refresh keeps previous content while loading`() = runTest {
        val repository = FakeMovieRepository(
            detailResult = DomainResult.Success(movieDetail(id = 42, title = "The Matrix"))
        )
        val viewModel = viewModel(repository = repository)

        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()
        val previousMovie = (viewModel.uiState.value as MovieDetailUiState.Content).movie

        viewModel.load(movieId = 42, forceRefresh = true)

        val state = viewModel.uiState.value as MovieDetailUiState.Loading
        assertEquals(previousMovie, state.previousData)
    }

    @Test
    fun `loading a different movie clears previous content while loading`() = runTest {
        val firstResult = CompletableDeferred<DomainResult<MovieDetail>>()
        val secondResult = CompletableDeferred<DomainResult<MovieDetail>>()
        val repository = DeferredMovieRepository(firstResult, secondResult)
        val viewModel = viewModel(repository = repository)

        viewModel.load(movieId = 1)
        runCurrent()
        firstResult.complete(DomainResult.Success(movieDetail(id = 1, title = "Old Movie")))
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.load(movieId = 2)

        val state = viewModel.uiState.value as MovieDetailUiState.Loading
        assertEquals(null, state.previousData)
        secondResult.complete(DomainResult.Success(movieDetail(id = 2, title = "New Movie")))
        dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `force refresh keeps previous content when refresh fails`() = runTest {
        val repository = FakeMovieRepository(
            detailResult = DomainResult.Success(movieDetail(id = 42, title = "The Matrix"))
        )
        val viewModel = viewModel(repository = repository)

        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()
        val previousMovie = (viewModel.uiState.value as MovieDetailUiState.Content).movie

        repository.detailResult = DomainResult.Error(DomainError.Network())
        viewModel.load(movieId = 42, forceRefresh = true)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieDetailUiState.Error
        assertEquals(previousMovie, state.previousData)
        assertTrue(state.isRetryable)
    }

    @Test
    fun `same movie reload from error keeps previous content while loading`() = runTest {
        val repository = FakeMovieRepository(
            detailResult = DomainResult.Success(movieDetail(id = 42, title = "The Matrix"))
        )
        val viewModel = viewModel(repository = repository)

        viewModel.load(movieId = 42)
        dispatcher.scheduler.advanceUntilIdle()
        val previousMovie = (viewModel.uiState.value as MovieDetailUiState.Content).movie

        repository.detailResult = DomainResult.Error(DomainError.Network())
        viewModel.load(movieId = 42, forceRefresh = true)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.load(movieId = 42)

        val state = viewModel.uiState.value as MovieDetailUiState.Loading
        assertEquals(previousMovie, state.previousData)
    }

    @Test
    fun `new load cancels previous request and ignores stale response`() = runTest {
        val firstResult = CompletableDeferred<DomainResult<MovieDetail>>()
        val secondResult = CompletableDeferred<DomainResult<MovieDetail>>()
        val repository = DeferredMovieRepository(firstResult, secondResult)
        val viewModel = viewModel(repository = repository)

        viewModel.load(movieId = 1)
        runCurrent()
        viewModel.load(movieId = 2)
        runCurrent()

        firstResult.complete(DomainResult.Success(movieDetail(id = 1, title = "Old Movie")))
        secondResult.complete(DomainResult.Success(movieDetail(id = 2, title = "New Movie")))
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieDetailUiState.Content
        assertEquals(2L, state.movie.id)
        assertEquals("New Movie", state.movie.title)
    }

    @Test
    fun `invalid movie id maps to non retryable error without repository call`() = runTest {
        val repository = FakeMovieRepository(
            detailResult = DomainResult.Success(movieDetail(id = 42))
        )
        val viewModel = viewModel(repository = repository)

        viewModel.load(movieId = 0)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieDetailUiState.Error
        assertTrue(state.isRetryable.not())
        assertEquals(0, repository.detailRequests)
    }

    private fun viewModel(
        detailResult: DomainResult<MovieDetail> = DomainResult.Success(movieDetail(id = 1)),
        repository: MovieRepository = FakeMovieRepository(detailResult = detailResult),
    ): MovieDetailViewModel =
        MovieDetailViewModel(
            getMovieDetail = GetMovieDetailUseCase(repository),
            movieDetailUiMapper = MovieDetailUiMapper(
                currencyFormatter = CurrencyFormatter(),
                dateFormatter = DateFormatter(localeProvider = { Locale.US }),
            ),
        )

    private class FakeMovieRepository(
        var detailResult: DomainResult<MovieDetail>,
    ) : MovieRepository {
        var detailRequests = 0
            private set

        override suspend fun getTrendingMovies(
            timeWindow: TimeWindow,
            language: LanguageCode,
        ): DomainResult<List<MovieSummary>> =
            error("Trending movies are not used by MovieDetailViewModel")

        override suspend fun getMovieGenres(language: LanguageCode): DomainResult<List<Genre>> =
            error("Genres are not used by MovieDetailViewModel")

        override suspend fun getMovieDetail(
            movieId: Long,
            language: LanguageCode,
        ): DomainResult<MovieDetail> {
            detailRequests++
            return detailResult
        }
    }

    private class DeferredMovieRepository(
        private vararg val results: CompletableDeferred<DomainResult<MovieDetail>>,
    ) : MovieRepository {
        private var resultIndex = 0

        override suspend fun getTrendingMovies(
            timeWindow: TimeWindow,
            language: LanguageCode,
        ): DomainResult<List<MovieSummary>> =
            error("Trending movies are not used by MovieDetailViewModel")

        override suspend fun getMovieGenres(language: LanguageCode): DomainResult<List<Genre>> =
            error("Genres are not used by MovieDetailViewModel")

        override suspend fun getMovieDetail(
            movieId: Long,
            language: LanguageCode,
        ): DomainResult<MovieDetail> =
            results[resultIndex++].await()
    }

    private companion object {
        fun movieDetail(
            id: Long,
            title: String = "Movie $id",
        ): MovieDetail =
            MovieDetail(
                id = id,
                title = title,
                tagline = "Tagline",
                overview = "Overview",
                posterUrl = "poster",
                backdropUrl = "backdrop",
                genres = listOf(Genre(878, "Science Fiction")),
                voteAverage = 8.0,
                voteCount = 100,
                budget = 1,
                revenue = 2,
                status = "Released",
                imdbId = "tt$id",
                runtimeMinutes = 120,
                releaseDate = "2024-01-01",
            )
    }
}
