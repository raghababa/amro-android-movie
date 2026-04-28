package com.amro.domain.usecase

import com.amro.domain.model.MovieSummary
import javax.inject.Inject

class FilterMoviesByGenreUseCase @Inject constructor() {
    operator fun invoke(
        movies: List<MovieSummary>,
        genreId: Int?,
    ): List<MovieSummary> {
        if (genreId == null) return movies

        return movies.filter { movie ->
            movie.genres.any { it.id == genreId }
        }
    }
}
