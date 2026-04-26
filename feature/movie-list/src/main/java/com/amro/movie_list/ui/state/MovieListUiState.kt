package com.amro.movie_list.ui.state

import com.amro.core.ui.UiText
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.SortOrder
import com.amro.movie_list.ui.model.GenreUi
import com.amro.movie_list.ui.model.MovieSummaryUi

sealed interface MovieListUiState {
    data object Loading : MovieListUiState

    data class Error(
        val message: UiText,
        val isRetryable: Boolean,
    ) : MovieListUiState

    data class Empty(
        val availableGenres: List<GenreUi>,
        val selectedGenreId: Int?,
        val sortField: MovieSortField,
        val sortOrder: SortOrder,
    ) : MovieListUiState

    data class Content(
        val movies: List<MovieSummaryUi>,
        val availableGenres: List<GenreUi>,
        val selectedGenreId: Int?,
        val sortField: MovieSortField,
        val sortOrder: SortOrder,
    ) : MovieListUiState
}

