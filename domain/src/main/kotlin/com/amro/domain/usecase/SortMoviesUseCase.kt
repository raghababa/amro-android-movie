package com.amro.domain.usecase

import com.amro.domain.model.MovieSortField
import com.amro.domain.model.MovieSummary
import com.amro.domain.model.SortOrder
import javax.inject.Inject

class SortMoviesUseCase @Inject constructor() {
    operator fun invoke(
        movies: List<MovieSummary>,
        sortField: MovieSortField,
        sortOrder: SortOrder,
    ): List<MovieSummary> {
        val comparator = when (sortField) {
            MovieSortField.POPULARITY -> compareBy<MovieSummary> { it.popularity }
            MovieSortField.TITLE -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            MovieSortField.RELEASE_DATE -> compareBy<MovieSummary> { it.releaseDate.orEmpty() }
        }

        val sorted = movies.sortedWith(comparator)
        return if (sortOrder == SortOrder.ASCENDING) sorted else sorted.reversed()
    }
}
