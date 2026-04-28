package com.amro.domain.usecase

import com.amro.domain.model.MovieSortField
import com.amro.domain.model.MovieSummary
import com.amro.domain.model.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class SortMoviesUseCaseTest {

    private val useCase = SortMoviesUseCase()

    @Test
    fun `sorts by popularity descending`() {
        val movies = listOf(
            movie(id = 1, popularity = 4.0),
            movie(id = 2, popularity = 8.0),
            movie(id = 3, popularity = 6.0),
        )

        val result = useCase(
            movies = movies,
            sortField = MovieSortField.POPULARITY,
            sortOrder = SortOrder.DESCENDING,
        )

        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    @Test
    fun `sorts by title case-insensitive ascending`() {
        val movies = listOf(
            movie(id = 1, title = "zebra"),
            movie(id = 2, title = "Alpha"),
            movie(id = 3, title = "beta"),
        )

        val result = useCase(
            movies = movies,
            sortField = MovieSortField.TITLE,
            sortOrder = SortOrder.ASCENDING,
        )

        assertEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    @Test
    fun `sorts by title descending`() {
        val movies = listOf(
            movie(id = 1, title = "Alpha"),
            movie(id = 2, title = "beta"),
            movie(id = 3, title = "zebra"),
        )

        val result = useCase(
            movies = movies,
            sortField = MovieSortField.TITLE,
            sortOrder = SortOrder.DESCENDING,
        )

        assertEquals(listOf(3L, 2L, 1L), result.map { it.id })
    }

    @Test
    fun `sorts by release date ascending with missing dates last`() {
        val movies = listOf(
            movie(id = 1, releaseDate = "2024-01-01"),
            movie(id = 2, releaseDate = null),
            movie(id = 3, releaseDate = "2023-01-01"),
        )

        val result = useCase(
            movies = movies,
            sortField = MovieSortField.RELEASE_DATE,
            sortOrder = SortOrder.ASCENDING,
        )

        assertEquals(listOf(3L, 1L, 2L), result.map { it.id })
    }

    @Test
    fun `sorts by release date descending with missing dates last`() {
        val movies = listOf(
            movie(id = 1, releaseDate = "2024-01-01"),
            movie(id = 2, releaseDate = null),
            movie(id = 3, releaseDate = "2023-01-01"),
        )

        val result = useCase(
            movies = movies,
            sortField = MovieSortField.RELEASE_DATE,
            sortOrder = SortOrder.DESCENDING,
        )

        assertEquals(listOf(1L, 3L, 2L), result.map { it.id })
    }

    @Test
    fun `sorts by release date with invalid dates last`() {
        val movies = listOf(
            movie(id = 1, releaseDate = "2024-01-01"),
            movie(id = 2, releaseDate = "not-a-date"),
            movie(id = 3, releaseDate = "2023-01-01"),
            movie(id = 4, releaseDate = " "),
        )

        val result = useCase(
            movies = movies,
            sortField = MovieSortField.RELEASE_DATE,
            sortOrder = SortOrder.ASCENDING,
        )

        assertEquals(listOf(3L, 1L, 2L, 4L), result.map { it.id })
    }

    private fun movie(
        id: Long,
        title: String = "Movie $id",
        popularity: Double = 0.0,
        releaseDate: String? = null,
    ): MovieSummary =
        MovieSummary(
            id = id,
            title = title,
            posterUrl = null,
            backdropUrl = null,
            genres = emptyList(),
            popularity = popularity,
            releaseDate = releaseDate,
        )
}
