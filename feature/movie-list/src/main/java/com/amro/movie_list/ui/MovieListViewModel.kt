package com.amro.movie_list.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.core.R as CoreR
import com.amro.core.ui.UiText
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.MovieSummary
import com.amro.domain.model.SortOrder
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import com.amro.domain.usecase.FilterMoviesByGenreUseCase
import com.amro.domain.usecase.GetMovieGenresUseCase
import com.amro.domain.usecase.GetTrendingMoviesUseCase
import com.amro.domain.usecase.SortMoviesUseCase
import com.amro.movie_list.ui.model.GenreUi
import com.amro.movie_list.ui.model.MovieSummaryUi
import com.amro.movie_list.ui.state.MovieListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val getTrendingMovies: GetTrendingMoviesUseCase,
    private val getMovieGenres: GetMovieGenresUseCase,
    private val filterMoviesByGenre: FilterMoviesByGenreUseCase,
    private val sortMovies: SortMoviesUseCase,
) : ViewModel() {

    private var dataState = MovieListDataState()

    private val _uiState = MutableStateFlow<MovieListUiState>(MovieListUiState.Loading)
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onGenreSelected(genreId: Int?) {
        dataState = dataState.copy(selectedGenreId = genreId)
        updateUiState()
    }

    fun onSortFieldSelected(field: MovieSortField) {
        dataState = dataState.copy(sortField = field)
        updateUiState()
    }

    fun onSortOrderChanged(order: SortOrder) {
        dataState = dataState.copy(sortOrder = order)
        updateUiState()
    }

    fun onRetry() {
        load()
    }

    private fun load() {
        _uiState.value = MovieListUiState.Loading
        viewModelScope.launch {
            when (val genresResult = getMovieGenres(language = "en")) {
                is DomainResult.Success -> {
                    dataState = dataState.copy(
                        availableGenres = genresResult.value
                            .map { GenreUi(id = it.id, name = it.name) }
                            .sortedBy { it.name }
                    )
                }
                is DomainResult.Error -> {
                    _uiState.value = genresResult.toUiError()
                    return@launch
                }
            }

            when (val moviesResult = getTrendingMovies(timeWindow = TimeWindow.WEEK, language = "en-US")) {
                is DomainResult.Success -> {
                    dataState = dataState.copy(allMovies = moviesResult.value)
                    updateUiState()
                }

                is DomainResult.Error -> {
                    _uiState.value = moviesResult.toUiError()
                }
            }
        }
    }

    private fun updateUiState() {
        val state = dataState
        val filtered = filterMoviesByGenre(state.allMovies, state.selectedGenreId)
        val sorted = sortMovies(filtered, state.sortField, state.sortOrder)
        val currentMovies = sorted.map { it.toUi() }

        if (currentMovies.isEmpty()) {
            _uiState.value = MovieListUiState.Empty(
                availableGenres = state.availableGenres,
                selectedGenreId = state.selectedGenreId,
                sortField = state.sortField,
                sortOrder = state.sortOrder,
            )
            return
        }

        _uiState.value = MovieListUiState.Content(
            movies = currentMovies,
            availableGenres = state.availableGenres,
            selectedGenreId = state.selectedGenreId,
            sortField = state.sortField,
            sortOrder = state.sortOrder,
        )
    }
}

private data class MovieListDataState(
    val allMovies: List<MovieSummary> = emptyList(),
    val availableGenres: List<GenreUi> = emptyList(),
    val selectedGenreId: Int? = null,
    val sortField: MovieSortField = MovieSortField.POPULARITY,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
)

private fun DomainResult.Error.toUiError(): MovieListUiState.Error {
    val (message, retryable) = when (val e = error) {
        is DomainError.Network -> UiText.StringRes(CoreR.string.error_network) to true
        is DomainError.Configuration -> UiText.StringRes(CoreR.string.error_configuration) to false
        DomainError.Unauthorized -> UiText.StringRes(CoreR.string.error_unauthorized) to false
        DomainError.NotFound -> UiText.StringRes(CoreR.string.error_not_found) to false
        DomainError.RateLimited -> UiText.StringRes(CoreR.string.error_rate_limited) to true
        DomainError.Server -> UiText.StringRes(CoreR.string.error_server) to true
        is DomainError.InvalidInput -> UiText.StringRes(CoreR.string.error_something_went_wrong) to false
        is DomainError.Empty -> UiText.StringRes(CoreR.string.error_empty_result) to false
        is DomainError.Serialization -> UiText.StringRes(CoreR.string.error_unexpected_response) to false
        is DomainError.Unknown -> UiText.StringRes(CoreR.string.error_something_went_wrong) to false
    }
    return MovieListUiState.Error(message = message, isRetryable = retryable)
}

private fun MovieSummary.toUi(): MovieSummaryUi =
    MovieSummaryUi(
        id = id,
        title = title,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        genreNames = genres.map { it.name },
        releaseDate = releaseDate,
    )

