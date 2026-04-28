package com.amro.movie_list.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class MovieSummaryUi(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genreNames: List<String>,
    val releaseDate: String?,
)

@Immutable
data class GenreUi(
    val id: Int,
    val name: String,
)

