package com.amro.data.network.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbTrendingMoviesResponse(
    val page: Int,
    val results: List<TmdbTrendingMovieDto>,
    @SerialName("total_pages") val totalPages: Int? = null,
    @SerialName("total_results") val totalResults: Int? = null,
)

@Serializable
data class TmdbTrendingMovieDto(
    val id: Long,
    val title: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList(),
    val popularity: Double = 0.0,
    @SerialName("release_date") val releaseDate: String? = null,
)

@Serializable
data class TmdbGenresResponse(
    val genres: List<TmdbGenreDto> = emptyList(),
)

@Serializable
data class TmdbGenreDto(
    val id: Int,
    val name: String,
)

@Serializable
data class TmdbMovieDetailDto(
    val id: Long,
    val title: String,
    val tagline: String? = null,
    val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val budget: Long = 0,
    val revenue: Long = 0,
    val status: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("runtime") val runtimeMinutes: Int? = null,
    @SerialName("release_date") val releaseDate: String? = null,
)

