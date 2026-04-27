package com.amro.domain.usecase

import com.amro.domain.model.MovieSortField
import com.amro.domain.model.MovieSummary
import com.amro.domain.model.SortOrder
import java.time.LocalDate
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
            MovieSortField.RELEASE_DATE -> return movies.sortedByReleaseDate(sortOrder)
        }

        val sorted = movies.sortedWith(comparator)
        return if (sortOrder == SortOrder.ASCENDING) sorted else sorted.reversed()
    }
}

private fun List<MovieSummary>.sortedByReleaseDate(sortOrder: SortOrder): List<MovieSummary> {
    val dateComparator =
        if (sortOrder == SortOrder.ASCENDING) {
            compareBy<MovieSummary> { it.releaseDate.toLocalDateOrNull() }
        } else {
            compareByDescending { it.releaseDate.toLocalDateOrNull() }
        }

    return sortedWith(
        compareBy<MovieSummary> { it.releaseDate.toLocalDateOrNull() == null }
            .then(dateComparator)
    )
}

private fun String?.toLocalDateOrNull(): LocalDate? =
    this
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
