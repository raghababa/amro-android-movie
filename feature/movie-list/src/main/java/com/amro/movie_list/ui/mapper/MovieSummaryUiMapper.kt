package com.amro.movie_list.ui.mapper

import com.amro.domain.model.MovieSummary
import com.amro.movie_list.ui.model.MovieSummaryUi
import javax.inject.Inject

class MovieSummaryUiMapper @Inject constructor() {
    fun toUi(movie: MovieSummary): MovieSummaryUi =
        MovieSummaryUi(
            id = movie.id,
            title = movie.title,
            posterUrl = movie.posterUrl,
            backdropUrl = movie.backdropUrl,
            genreNames = movie.genres.map { it.name },
            releaseDate = movie.releaseDate,
        )
}
