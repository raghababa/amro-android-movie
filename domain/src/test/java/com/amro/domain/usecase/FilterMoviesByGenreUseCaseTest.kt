package com.amro.domain.usecase

import com.amro.domain.model.Genre
import com.amro.domain.model.MovieSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterMoviesByGenreUseCaseTest {

    private val useCase = FilterMoviesByGenreUseCase()

    @Test
    fun `null genre returns all movies`() {
        val movies = listOf(movie(id = 1, genres = listOf(action)))

        val result = useCase(movies = movies, genreId = null)

        assertEquals(movies, result)
    }

    @Test
    fun `genre filters movies containing matching genre`() {
        val movies = listOf(
            movie(id = 1, genres = listOf(action)),
            movie(id = 2, genres = listOf(comedy)),
            movie(id = 3, genres = listOf(action, comedy)),
        )

        val result = useCase(movies = movies, genreId = action.id)

        assertEquals(listOf(1L, 3L), result.map { it.id })
    }

    @Test
    fun `genre with no matches returns empty list`() {
        val movies = listOf(
            movie(id = 1, genres = listOf(action)),
            movie(id = 2, genres = listOf(comedy)),
        )

        val result = useCase(movies = movies, genreId = drama.id)

        assertEquals(emptyList<MovieSummary>(), result)
    }

    private fun movie(id: Long, genres: List<Genre>): MovieSummary =
        MovieSummary(
            id = id,
            title = "Movie $id",
            posterUrl = null,
            backdropUrl = null,
            genres = genres,
            popularity = 0.0,
            releaseDate = null,
        )

    private companion object {
        val action = Genre(id = 28, name = "Action")
        val comedy = Genre(id = 35, name = "Comedy")
        val drama = Genre(id = 18, name = "Drama")
    }
}
