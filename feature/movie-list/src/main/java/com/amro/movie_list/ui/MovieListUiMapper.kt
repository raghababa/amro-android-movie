package com.amro.movie_list.ui

import com.amro.core.ui.DomainErrorUiMapper
import com.amro.domain.model.MovieSummary
import com.amro.movie_list.ui.mapper.GenreUiMapper
import com.amro.movie_list.ui.mapper.MovieSummaryUiMapper
import com.amro.movie_list.ui.state.MovieListConfig
import com.amro.movie_list.ui.state.MovieListUiState
import com.amro.movie_list.ui.state.PreviousListState
import javax.inject.Inject

class MovieListUiMapper @Inject constructor(
    private val genreUiMapper: GenreUiMapper,
    private val movieSummaryUiMapper: MovieSummaryUiMapper,
) {
    internal fun map(
        state: MovieListState,
        movies: List<MovieSummary>,
    ): MovieListUiState {
        val config = state.toConfig()
        val currentMovies = if (state.loadState == MovieListLoadState.Loading && movies.isEmpty()) {
            emptyList()
        } else {
            movies.map(movieSummaryUiMapper::toUi)
        }
        val previousContent = currentMovies.toPreviousContent(config)

        return when (val loadState = state.loadState) {
            MovieListLoadState.Loading -> previousContent?.let {
                MovieListUiState.Loading.Refreshing(
                    config = config,
                    previousData = it,
                )
            } ?: MovieListUiState.Loading.Initial(config = config)

            is MovieListLoadState.Failed -> {
                val errorUi = DomainErrorUiMapper.map(loadState.error)
                MovieListUiState.Error(
                    message = errorUi.message,
                    isRetryable = errorUi.isRetryable,
                    config = config,
                    previousData = previousContent,
                )
            }

            MovieListLoadState.Loaded -> currentMovies.toContentState(config)
        }
    }

    private fun MovieListState.toConfig(): MovieListConfig {
        val genreErrorUi = genreLoadError?.let(DomainErrorUiMapper::map)
        return MovieListConfig(
            availableGenres = genres
                .map(genreUiMapper::toUi)
                .sortedBy { it.name },
            selectedGenreId = selectedGenreId,
            sortField = sortField,
            sortOrder = sortOrder,
            genreError = genreErrorUi?.message,
        )
    }

    private fun List<com.amro.movie_list.ui.model.MovieSummaryUi>.toPreviousContent(
        config: MovieListConfig,
    ): PreviousListState? =
        takeIf { it.isNotEmpty() }
            ?.let { movies -> PreviousListState(movies = movies, config = config) }

    private fun List<com.amro.movie_list.ui.model.MovieSummaryUi>.toContentState(
        config: MovieListConfig,
    ): MovieListUiState =
        if (isEmpty()) {
            MovieListUiState.Empty(config = config)
        } else {
            MovieListUiState.Content(
                movies = this,
                config = config,
            )
        }
}
