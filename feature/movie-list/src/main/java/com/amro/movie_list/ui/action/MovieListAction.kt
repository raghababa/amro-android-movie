package com.amro.movie_list.ui.action

import com.amro.domain.model.MovieSortField
import com.amro.domain.model.SortOrder

sealed interface MovieListAction {
    data class SelectGenre(val genreId: Int?) : MovieListAction
    data class ChangeSortField(val field: MovieSortField) : MovieListAction
    data class ChangeSortOrder(val order: SortOrder) : MovieListAction
    data class ClickMovie(val movieId: Long) : MovieListAction
    data object Retry : MovieListAction
}

