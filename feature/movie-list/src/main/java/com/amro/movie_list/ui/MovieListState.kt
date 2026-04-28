package com.amro.movie_list.ui

import com.amro.domain.model.Genre
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.MovieSummary
import com.amro.domain.model.SortOrder
import com.amro.domain.result.DomainError

internal data class MovieListState(
    val allMovies: List<MovieSummary> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val selectedGenreId: Int? = null,
    val sortField: MovieSortField = MovieSortField.POPULARITY,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val genreLoadError: DomainError? = null,
    val loadState: MovieListLoadState = MovieListLoadState.Loading,
)

internal sealed interface MovieListLoadState {
    data object Loaded : MovieListLoadState
    data object Loading : MovieListLoadState
    data class Failed(val error: DomainError) : MovieListLoadState
}
