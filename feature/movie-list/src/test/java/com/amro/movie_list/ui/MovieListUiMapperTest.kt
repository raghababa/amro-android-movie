package com.amro.movie_list.ui

import com.amro.domain.model.Genre
import com.amro.domain.model.MovieSummary
import com.amro.movie_list.ui.mapper.GenreUiMapper
import com.amro.movie_list.ui.mapper.MovieSummaryUiMapper
import com.amro.movie_list.ui.state.MovieListUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class MovieListUiMapperTest {

    private val uiMapper = MovieListUiMapper(
        genreUiMapper = GenreUiMapper(),
        movieSummaryUiMapper = MovieSummaryUiMapper(),
    )

    @Test
    fun `map returns content with mapped movies and genres`() {
        val state = uiMapper.map(
            state = MovieListState(
                genres = listOf(action, comedy),
                selectedGenreId = action.id,
                loadState = MovieListLoadState.Loaded,
            ),
            movies = listOf(
                movie(id = 2, title = "Alpha", genres = listOf(action)),
                movie(id = 1, title = "Beta", genres = listOf(action)),
            ),
        ) as MovieListUiState.Content

        assertEquals(listOf(2L, 1L), state.movies.map { it.id })
        assertEquals(listOf("Action", "Comedy"), state.config.availableGenres.map { it.name })
        assertEquals(action.id, state.config.selectedGenreId)
    }

    @Test
    fun `map returns empty without storing previous content when filter has no matches`() {
        val state = uiMapper.map(
            state = MovieListState(
                allMovies = listOf(movie(id = 1, genres = listOf(action))),
                genres = listOf(action, comedy),
                selectedGenreId = comedy.id,
                loadState = MovieListLoadState.Loaded,
            ),
            movies = emptyList(),
        ) as MovieListUiState.Empty

        assertEquals(comedy.id, state.config.selectedGenreId)
        assertEquals(null, state.previousData)
        assertEquals(null, state.currentMoviesOrNull())
    }

    private companion object {
        val action = Genre(id = 28, name = "Action")
        val comedy = Genre(id = 35, name = "Comedy")

        fun movie(
            id: Long,
            title: String = "Movie $id",
            genres: List<Genre>,
        ): MovieSummary =
            MovieSummary(
                id = id,
                title = title,
                posterUrl = "poster-$id",
                backdropUrl = "backdrop-$id",
                genres = genres,
                popularity = id.toDouble(),
                releaseDate = null,
            )
    }
}
