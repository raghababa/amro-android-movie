package com.amro.movie_list.ui.state

import com.amro.core.ui.UiText
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.SortOrder
import com.amro.movie_list.ui.model.GenreUi
import com.amro.movie_list.ui.model.MovieSummaryUi

data class MovieListConfig(
    val availableGenres: List<GenreUi> = emptyList(),
    val selectedGenreId: Int? = null,
    val sortField: MovieSortField = MovieSortField.POPULARITY,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val genreError: UiText? = null,
) {
    fun hasActiveFilter(): Boolean = selectedGenreId != null

    val isSortedByPopularity: Boolean
        get() = sortField == MovieSortField.POPULARITY

    val isDefaultState: Boolean
        get() = !hasActiveFilter() &&
            sortField == MovieSortField.POPULARITY &&
            sortOrder == SortOrder.DESCENDING
}

data class PreviousListState(
    val movies: List<MovieSummaryUi>,
    val config: MovieListConfig,
)

sealed interface MovieListUiState {
    val previousData: PreviousListState?
        get() = null

    fun previousDataOrNull(): PreviousListState? = previousData

    sealed interface Loading : MovieListUiState {
        val config: MovieListConfig
        val isRefreshing: Boolean

        data class Initial(
            override val config: MovieListConfig = MovieListConfig(),
        ) : Loading {
            override val isRefreshing: Boolean = false
        }

        data class Refreshing(
            override val config: MovieListConfig,
            override val previousData: PreviousListState,
        ) : Loading {
            override val isRefreshing: Boolean = true
        }
    }

    data class Error(
        val message: UiText,
        val isRetryable: Boolean,
        val config: MovieListConfig = MovieListConfig(),
        override val previousData: PreviousListState? = null,
    ) : MovieListUiState

    data class Empty(
        val config: MovieListConfig,
        override val previousData: PreviousListState? = null,
    ) : MovieListUiState

    data class Content(
        val movies: List<MovieSummaryUi>,
        val config: MovieListConfig,
    ) : MovieListUiState {
        override fun previousDataOrNull(): PreviousListState =
            PreviousListState(movies = movies, config = config)

        override fun currentMoviesOrNull(): List<MovieSummaryUi> = movies
    }

    fun currentMoviesOrNull(): List<MovieSummaryUi>? = previousData?.movies

    fun isEmpty(): Boolean = this is Empty

    fun hasContent(): Boolean = currentMoviesOrNull()?.isNotEmpty() == true
}

