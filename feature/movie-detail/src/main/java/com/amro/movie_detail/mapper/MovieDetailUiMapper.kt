package com.amro.movie_detail.mapper

import com.amro.domain.model.MovieDetail
import com.amro.movie_detail.ui.model.MovieDetailUi
import java.text.NumberFormat
import java.util.Locale

internal fun MovieDetail.toUi(): MovieDetailUi {
    // TMDB budget and revenue fields are documented as USD values.
    val money = NumberFormat.getCurrencyInstance(Locale.US)
    val budgetFormatted = if (budget > 0) money.format(budget) else null
    val revenueFormatted = if (revenue > 0) money.format(revenue) else null

    return MovieDetailUi(
        id = id,
        title = title,
        tagline = tagline?.takeIf { it.isNotBlank() },
        overview = overview?.takeIf { it.isNotBlank() },
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        genres = genres.map { it.name },
        voteAverage = voteAverage,
        voteCount = voteCount,
        budget = budgetFormatted,
        revenue = revenueFormatted,
        status = status?.takeIf { it.isNotBlank() },
        imdbUrl = imdbId?.takeIf { it.isNotBlank() }?.let { "https://www.imdb.com/title/$it/" },
        runtimeMinutes = runtimeMinutes?.takeIf { it > 0 },
        releaseDate = releaseDate?.takeIf { it.isNotBlank() },
    )
}

