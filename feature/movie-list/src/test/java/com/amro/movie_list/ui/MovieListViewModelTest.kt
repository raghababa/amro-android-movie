package com.amro.movie_list.ui

import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.MovieSummary
import com.amro.domain.model.SortOrder
import com.amro.domain.repository.LanguageCode
import com.amro.domain.repository.MovieRepository
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import com.amro.domain.usecase.FilterMoviesByGenreUseCase
import com.amro.domain.usecase.GetMovieGenresUseCase
import com.amro.domain.usecase.GetTrendingMoviesUseCase
import com.amro.domain.usecase.SortMoviesUseCase
import com.amro.movie_list.ui.mapper.GenreUiMapper
import com.amro.movie_list.ui.mapper.MovieSummaryUiMapper
import com.amro.movie_list.ui.state.MovieListUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {

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
    fun `initial load emits content sorted by popularity descending`() = runTest {
        val viewModel = viewModel(
            moviesResult = DomainResult.Success(
                listOf(
                    movie(id = 1, title = "Low", popularity = 1.0, genres = listOf(action)),
                    movie(id = 2, title = "High", popularity = 9.0, genres = listOf(comedy)),
                    movie(id = 3, title = "Mid", popularity = 5.0, genres = listOf(action, comedy)),
                )
            )
        )

        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(listOf(2L, 3L, 1L), state.movies.map { it.id })
        assertEquals(listOf("Action", "Comedy"), state.config.availableGenres.map { it.name })
        assertEquals(MovieSortField.POPULARITY, state.config.sortField)
        assertEquals(SortOrder.DESCENDING, state.config.sortOrder)
        assertTrue(state.config.isSortedByPopularity)
        assertTrue(state.config.isDefaultState)
    }

    @Test
    fun `selecting genre filters currently loaded top movies`() = runTest {
        val viewModel = viewModel(
            moviesResult = DomainResult.Success(
                listOf(
                    movie(id = 1, genres = listOf(action)),
                    movie(id = 2, genres = listOf(comedy)),
                    movie(id = 3, genres = listOf(action, comedy)),
                )
            )
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onGenreSelected(action.id)
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(listOf(3L, 1L), state.movies.map { it.id })
        assertEquals(action.id, state.config.selectedGenreId)
    }

    @Test
    fun `selecting genre with no matching movies emits empty state`() = runTest {
        val viewModel = viewModel(
            genresResult = DomainResult.Success(listOf(action, drama)),
            moviesResult = DomainResult.Success(listOf(movie(id = 1, genres = listOf(action)))),
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onGenreSelected(drama.id)
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value as MovieListUiState.Empty
        assertEquals(drama.id, state.config.selectedGenreId)
        assertEquals(listOf("Action", "Drama"), state.config.availableGenres.map { it.name })
        assertTrue(state.config.hasActiveFilter())
        assertTrue(state.isEmpty())
        assertEquals(null, state.previousData)
        assertEquals(null, state.currentMoviesOrNull())
    }

    @Test
    fun `changing sort field and order updates content`() = runTest {
        val viewModel = viewModel(
            moviesResult = DomainResult.Success(
                listOf(
                    movie(id = 1, title = "Beta", releaseDate = "2024-01-01"),
                    movie(id = 2, title = "Alpha", releaseDate = "2023-01-01"),
                )
            )
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onSortFieldSelected(MovieSortField.TITLE)
        viewModel.onSortOrderChanged(SortOrder.ASCENDING)
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(listOf(2L, 1L), state.movies.map { it.id })
        assertEquals(MovieSortField.TITLE, state.config.sortField)
        assertEquals(SortOrder.ASCENDING, state.config.sortOrder)
    }

    @Test
    fun `genre failure still emits content with empty filter options when movies succeed`() = runTest {
        val repository = FakeMovieRepository(
            genresResult = DomainResult.Error(DomainError.Network()),
            moviesResult = DomainResult.Success(listOf(movie(id = 1))),
        )
        val viewModel = viewModel(repository = repository)

        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(listOf(1L), state.movies.map { it.id })
        assertTrue(state.config.availableGenres.isEmpty())
        assertEquals(null, state.config.selectedGenreId)
        assertTrue(state.config.genreError != null)
        assertTrue(state.config.isDefaultState)
        assertEquals(1, repository.trendingRequests)
    }

    @Test
    fun `initial load requests genres with en and trending with en us`() = runTest {
        val repository = FakeMovieRepository(
            genresResult = DomainResult.Success(listOf(action, comedy)),
            moviesResult = DomainResult.Success(listOf(movie(id = 1))),
        )
        viewModel(repository = repository)

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(LanguageCode.EN, repository.lastGenresLanguage)
        assertEquals(TimeWindow.WEEK, repository.lastTrendingTimeWindow)
        assertEquals(LanguageCode.EN_US, repository.lastTrendingLanguage)
    }

    @Test
    fun `movie failure after genres success emits retryable error with available filters`() = runTest {
        val viewModel = viewModel(
            genresResult = DomainResult.Success(listOf(action, comedy)),
            moviesResult = DomainResult.Error(DomainError.Server),
        )

        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieListUiState.Error
        assertTrue(state.isRetryable)
        assertEquals(listOf("Action", "Comedy"), state.config.availableGenres.map { it.name })
    }

    @Test
    fun `movie failure after genre failure emits error with genre error in config`() = runTest {
        val viewModel = viewModel(
            genresResult = DomainResult.Error(DomainError.Network()),
            moviesResult = DomainResult.Error(DomainError.Unauthorized),
        )

        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieListUiState.Error
        assertTrue(state.isRetryable.not())
        assertTrue(state.config.availableGenres.isEmpty())
        assertTrue(state.config.genreError != null)
    }

    @Test
    fun `retry after movie failure integrates use cases mapper and emits content`() = runTest {
        val repository = FakeMovieRepository(
            genresResult = DomainResult.Success(listOf(action, comedy)),
            moviesResult = DomainResult.Error(DomainError.Network()),
        )
        val viewModel = viewModel(repository = repository)
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is MovieListUiState.Error)

        repository.moviesResult = DomainResult.Success(
            listOf(
                movie(id = 1, title = "Low", popularity = 1.0, genres = listOf(action)),
                movie(id = 2, title = "High", popularity = 9.0, genres = listOf(comedy)),
            )
        )
        viewModel.onRetry()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(listOf(2L, 1L), state.movies.map { it.id })
        assertEquals(listOf("Action", "Comedy"), state.config.availableGenres.map { it.name })
        assertEquals(2, repository.trendingRequests)
    }

    @Test
    fun `successful empty movie result emits empty state`() = runTest {
        val viewModel = viewModel(
            genresResult = DomainResult.Success(listOf(action, comedy)),
            moviesResult = DomainResult.Success(emptyList()),
        )

        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieListUiState.Empty
        assertEquals(listOf("Action", "Comedy"), state.config.availableGenres.map { it.name })
        assertTrue(state.config.isDefaultState)
    }

    @Test
    fun `retry loading keeps previous movies with their config`() = runTest {
        val firstGenres = CompletableDeferred<DomainResult<List<Genre>>>()
        val firstMovies = CompletableDeferred<DomainResult<List<MovieSummary>>>()
        val secondGenres = CompletableDeferred<DomainResult<List<Genre>>>()
        val secondMovies = CompletableDeferred<DomainResult<List<MovieSummary>>>()
        val repository = DeferredMovieRepository(
            genreResults = mutableListOf(firstGenres, secondGenres),
            movieResults = mutableListOf(firstMovies, secondMovies),
        )
        val viewModel = viewModel(repository = repository)
        dispatcher.scheduler.runCurrent()
        firstGenres.complete(DomainResult.Success(listOf(action, comedy)))
        firstMovies.complete(DomainResult.Success(listOf(movie(id = 1), movie(id = 2, genres = listOf(comedy)))))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onGenreSelected(comedy.id)
        dispatcher.scheduler.runCurrent()
        val previousState = viewModel.uiState.value as MovieListUiState.Content

        viewModel.onRetry()
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value as MovieListUiState.Loading
        assertTrue(state.isRefreshing)
        assertEquals(previousState.movies, state.previousData?.movies)
        assertEquals(previousState.config, state.previousData?.config)

        secondGenres.complete(DomainResult.Success(listOf(action, comedy)))
        secondMovies.complete(DomainResult.Success(listOf(movie(id = 1), movie(id = 2, genres = listOf(comedy)))))
        dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `reload keeps selected genre when genre still exists`() = runTest {
        val repository = FakeMovieRepository(
            genresResult = DomainResult.Success(listOf(action, comedy)),
            moviesResult = DomainResult.Success(listOf(movie(id = 1), movie(id = 2, genres = listOf(comedy)))),
        )
        val viewModel = viewModel(repository = repository)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onGenreSelected(comedy.id)

        viewModel.onRetry()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(comedy.id, state.config.selectedGenreId)
        assertEquals(listOf(2L), state.movies.map { it.id })
    }

    @Test
    fun `reload resets selected genre when genre no longer exists`() = runTest {
        val repository = FakeMovieRepository(
            genresResult = DomainResult.Success(listOf(action, comedy)),
            moviesResult = DomainResult.Success(listOf(movie(id = 1), movie(id = 2, genres = listOf(comedy)))),
        )
        val viewModel = viewModel(repository = repository)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onGenreSelected(comedy.id)

        repository.genresResult = DomainResult.Success(listOf(action))
        viewModel.onRetry()
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(null, state.config.selectedGenreId)
        assertEquals(listOf("Action"), state.config.availableGenres.map { it.name })
    }

    @Test
    fun `sort and filter changes do not trigger reload`() = runTest {
        val repository = FakeMovieRepository(
            genresResult = DomainResult.Success(listOf(action, comedy)),
            moviesResult = DomainResult.Success(
                listOf(
                    movie(id = 1, title = "Beta", genres = listOf(action)),
                    movie(id = 2, title = "Alpha", genres = listOf(comedy)),
                )
            ),
        )
        val viewModel = viewModel(repository = repository)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onGenreSelected(comedy.id)
        viewModel.onSortFieldSelected(MovieSortField.TITLE)
        viewModel.onSortOrderChanged(SortOrder.ASCENDING)
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(listOf(2L), state.movies.map { it.id })
        assertEquals(1, repository.trendingRequests)
    }

    @Test
    fun `repeated same sort and filter clicks are ignored`() = runTest {
        val repository = FakeMovieRepository(
            genresResult = DomainResult.Success(listOf(action, comedy)),
            moviesResult = DomainResult.Success(
                listOf(
                    movie(id = 1, title = "Beta", genres = listOf(action)),
                    movie(id = 2, title = "Alpha", genres = listOf(comedy)),
                )
            ),
        )
        val viewModel = viewModel(repository = repository)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onGenreSelected(comedy.id)
        viewModel.onGenreSelected(comedy.id)
        viewModel.onSortFieldSelected(MovieSortField.TITLE)
        viewModel.onSortFieldSelected(MovieSortField.TITLE)
        viewModel.onSortOrderChanged(SortOrder.ASCENDING)
        viewModel.onSortOrderChanged(SortOrder.ASCENDING)
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(listOf(2L), state.movies.map { it.id })
        assertEquals(comedy.id, state.config.selectedGenreId)
        assertEquals(MovieSortField.TITLE, state.config.sortField)
        assertEquals(SortOrder.ASCENDING, state.config.sortOrder)
        assertEquals(1, repository.trendingRequests)
    }

    @Test
    fun `stale load cannot override newer retry result`() = runTest {
        val firstGenres = CompletableDeferred<DomainResult<List<Genre>>>()
        val firstMovies = CompletableDeferred<DomainResult<List<MovieSummary>>>()
        val secondGenres = CompletableDeferred<DomainResult<List<Genre>>>()
        val secondMovies = CompletableDeferred<DomainResult<List<MovieSummary>>>()
        val repository = DeferredMovieRepository(
            genreResults = mutableListOf(firstGenres, secondGenres),
            movieResults = mutableListOf(firstMovies, secondMovies),
        )
        val viewModel = viewModel(repository = repository)
        dispatcher.scheduler.runCurrent()

        viewModel.onRetry()
        dispatcher.scheduler.runCurrent()
        secondGenres.complete(DomainResult.Success(listOf(comedy)))
        secondMovies.complete(DomainResult.Success(listOf(movie(id = 2, genres = listOf(comedy)))))
        dispatcher.scheduler.advanceUntilIdle()
        firstGenres.complete(DomainResult.Success(listOf(action)))
        firstMovies.complete(DomainResult.Success(listOf(movie(id = 1, genres = listOf(action)))))
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MovieListUiState.Content
        assertEquals(listOf(2L), state.movies.map { it.id })
        assertEquals(listOf("Comedy"), state.config.availableGenres.map { it.name })
    }

    private fun TestScope.viewModel(
        genresResult: DomainResult<List<Genre>> = DomainResult.Success(listOf(action, comedy)),
        moviesResult: DomainResult<List<MovieSummary>> = DomainResult.Success(listOf(movie(id = 1))),
        repository: MovieRepository = FakeMovieRepository(
            genresResult = genresResult,
            moviesResult = moviesResult,
        ),
    ): MovieListViewModel {
        val viewModel = MovieListViewModel(
            getTrendingMovies = GetTrendingMoviesUseCase(repository),
            getMovieGenres = GetMovieGenresUseCase(repository),
            filterMoviesByGenre = FilterMoviesByGenreUseCase(),
            sortMovies = SortMoviesUseCase(),
            uiMapper = MovieListUiMapper(
                genreUiMapper = GenreUiMapper(),
                movieSummaryUiMapper = MovieSummaryUiMapper(),
            ),
        )
        viewModel.uiState.launchIn(backgroundScope)
        return viewModel
    }

    private class FakeMovieRepository(
        var genresResult: DomainResult<List<Genre>>,
        var moviesResult: DomainResult<List<MovieSummary>>,
    ) : MovieRepository {
        var trendingRequests = 0
            private set
        var lastTrendingTimeWindow: TimeWindow? = null
            private set
        var lastTrendingLanguage: LanguageCode? = null
            private set
        var lastGenresLanguage: LanguageCode? = null
            private set

        override suspend fun getTrendingMovies(
            timeWindow: TimeWindow,
            language: LanguageCode,
        ): DomainResult<List<MovieSummary>> {
            trendingRequests++
            lastTrendingTimeWindow = timeWindow
            lastTrendingLanguage = language
            return moviesResult
        }

        override suspend fun getMovieGenres(language: LanguageCode): DomainResult<List<Genre>> {
            lastGenresLanguage = language
            return genresResult
        }

        override suspend fun getMovieDetail(
            movieId: Long,
            language: LanguageCode,
        ): DomainResult<MovieDetail> =
            error("Movie detail is not used by MovieListViewModel")
    }

    private class DeferredMovieRepository(
        private val genreResults: MutableList<CompletableDeferred<DomainResult<List<Genre>>>>,
        private val movieResults: MutableList<CompletableDeferred<DomainResult<List<MovieSummary>>>>,
    ) : MovieRepository {
        override suspend fun getTrendingMovies(
            timeWindow: TimeWindow,
            language: LanguageCode,
        ): DomainResult<List<MovieSummary>> =
            movieResults.removeAt(0).await()

        override suspend fun getMovieGenres(language: LanguageCode): DomainResult<List<Genre>> =
            genreResults.removeAt(0).await()

        override suspend fun getMovieDetail(
            movieId: Long,
            language: LanguageCode,
        ): DomainResult<MovieDetail> =
            error("Movie detail is not used by MovieListViewModel")
    }

    private companion object {
        val action = Genre(id = 28, name = "Action")
        val comedy = Genre(id = 35, name = "Comedy")
        val drama = Genre(id = 18, name = "Drama")

        fun movie(
            id: Long,
            title: String = "Movie $id",
            popularity: Double = id.toDouble(),
            genres: List<Genre> = listOf(action),
            releaseDate: String? = null,
        ): MovieSummary =
            MovieSummary(
                id = id,
                title = title,
                posterUrl = "poster-$id",
                backdropUrl = "backdrop-$id",
                genres = genres,
                popularity = popularity,
                releaseDate = releaseDate,
            )
    }
}
