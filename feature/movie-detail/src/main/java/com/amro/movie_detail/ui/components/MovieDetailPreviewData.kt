package com.amro.movie_detail.ui.components

import com.amro.core.links.ExternalUrls
import com.amro.movie_detail.ui.model.MovieDetailUi

internal val previewMovieDetail = MovieDetailUi(
    id = 42,
    title = "The Matrix",
    tagline = "Welcome to the Real World.",
    overview = "A hacker discovers reality is a simulation.",
    posterUrl = null,
    backdropUrl = null,
    genres = listOf("Science Fiction", "Action"),
    voteAverage = 8.2,
    voteCount = 25000,
    budget = "$63M",
    revenue = "$467M",
    status = "Released",
    imdbUrl = ExternalUrls.imdbTitleUrl("tt0133093"),
    runtimeMinutes = 136,
    releaseDate = "1999-03-31",
)
