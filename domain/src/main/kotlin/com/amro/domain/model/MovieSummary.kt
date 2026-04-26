package com.amro.domain.model

data class MovieSummary(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: List<Genre>,
    val popularity: Double,
    val releaseDate: String?,
)

