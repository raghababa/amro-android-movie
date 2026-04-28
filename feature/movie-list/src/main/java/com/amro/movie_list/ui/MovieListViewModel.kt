package com.amro.movie_list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.domain.model.Genre
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.MovieSummary
import com.amro.domain.model.SortOrder
import com.amro.domain.repository.LanguageCode
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainResult
import com.amro.domain.usecase.FilterMoviesByGenreUseCase
import com.amro.domain.usecase.GetMovieGenresUseCase
import com.amro.domain.usecase.GetTrendingMoviesUseCase
import com.amro.domain.usecase.SortMoviesUseCase
import com.amro.movie_list.ui.state.MovieListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val getTrendingMovies: GetTrendingMoviesUseCase,
    private val getMovieGenres: GetMovieGenresUseCase,
    private val filterMoviesByGenre: FilterMoviesByGenreUseCase,
    private val sortMovies: SortMoviesUseCase,
    private val uiMapper: MovieListUiMapper,
) : ViewModel() {

    private val internalState = MutableStateFlow(MovieListState())
    private var loadJob: Job? = null

    val uiState: StateFlow<MovieListUiState> = internalState
        .map(::toUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = toUiState(internalState.value),
        )

    init {
        load()
    }

    fun onGenreSelected(genreId: Int?) {
        internalState.update { state ->
            if (state.selectedGenreId == genreId) return@update state

            state.copy(
                selectedGenreId = genreId,
            )
        }
    }

    fun onSortFieldSelected(field: MovieSortField) {
        internalState.update { state ->
            if (state.sortField == field) return@update state

            state.copy(
                sortField = field,
            )
        }
    }

    fun onSortOrderChanged(order: SortOrder) {
        internalState.update { state ->
            if (state.sortOrder == order) return@update state

            state.copy(
                sortOrder = order,
            )
        }
    }

    fun onRetry() {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            internalState.update { state ->
                state.copy(loadState = MovieListLoadState.Loading)
            }

            val (genresResult, moviesResult) = fetchListData()

            handleLoadResult(
                genresResult = genresResult,
                moviesResult = moviesResult,
            )
        }
    }

    private fun handleLoadResult(
        genresResult: DomainResult<List<Genre>>,
        moviesResult: DomainResult<List<MovieSummary>>,
    ) {
        when (moviesResult) {
            is DomainResult.Success -> {
                internalState.update { state ->
                    state.withGenresResult(genresResult)
                        .copy(
                            allMovies = moviesResult.value,
                            loadState = MovieListLoadState.Loaded,
                        )
                }
            }

            is DomainResult.Error -> {
                internalState.update { state ->
                    state
                        .withGenresResult(genresResult)
                        .copy(
                            loadState = MovieListLoadState.Failed(moviesResult.error),
                        )
                }
            }
        }
    }

    private suspend fun fetchListData(): Pair<DomainResult<List<Genre>>, DomainResult<List<MovieSummary>>> =
        coroutineScope {
            val genresDeferred = async { getMovieGenres(language = LanguageCode.EN) }
            val moviesDeferred = async {
                getTrendingMovies(timeWindow = TimeWindow.WEEK, language = LanguageCode.EN_US)
            }
            genresDeferred.await() to moviesDeferred.await()
        }

    private fun toUiState(state: MovieListState): MovieListUiState {
        val filtered = filterMoviesByGenre(state.allMovies, state.selectedGenreId)
        val sorted = sortMovies(filtered, state.sortField, state.sortOrder)
        return uiMapper.map(
            state = state,
            movies = sorted,
        )
    }
}

private fun MovieListState.withGenresResult(
    genresResult: DomainResult<List<Genre>>,
): MovieListState =
    when (genresResult) {
        is DomainResult.Success -> {
            val genres = genresResult.value
            copy(
                genres = genres,
                selectedGenreId = selectedGenreId?.takeIf { id ->
                    genres.any { it.id == id }
                },
                genreLoadError = null,
            )
        }

        is DomainResult.Error -> copy(
            genreLoadError = genresResult.error,
        )
    }
