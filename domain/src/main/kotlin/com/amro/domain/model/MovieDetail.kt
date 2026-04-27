package com.amro.domain.model

data class MovieDetail(
    val id: Long,
    val title: String,
    val tagline: String?,
    val overview: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: List<Genre>,
    val voteAverage: Double,
    val voteCount: Int,
    val budget: Long,
    val revenue: Long,
    val status: String?,
    val imdbId: String?,
    val runtimeMinutes: Int?,
    val releaseDate: String?,
)

