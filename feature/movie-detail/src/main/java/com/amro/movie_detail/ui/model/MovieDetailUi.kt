package com.amro.movie_detail.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class MovieDetailUi(
    val id: Long,
    val title: String,
    val tagline: String?,
    val overview: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: List<String>,
    val voteAverage: Double,
    val voteCount: Int,
    val budget: String?,
    val revenue: String?,
    val status: String?,
    val imdbUrl: String?,
    val runtimeMinutes: Int?,
    val releaseDate: String?,
)

