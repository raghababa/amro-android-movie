package com.amro.data.network.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbTrendingMoviesResponse(
    val page: Int = 1,
    val results: List<TmdbTrendingMovieDto> = emptyList(),
    @SerialName("total_pages")
    val totalPages: Int? = null,
    @SerialName("total_results")
    val totalResults: Int? = null,
)

@Serializable
data class TmdbTrendingMovieDto(
    val id: Long,
    val title: String = "",
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    @SerialName("genre_ids")
    val genreIds: List<Int> = emptyList(),
    val popularity: Double? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
)

@Serializable
data class TmdbGenresResponse(
    val genres: List<TmdbGenreDto> = emptyList(),
)


@Serializable
data class TmdbGenreDto(
    val id: Int,
    val name: String = "",
)

@Serializable
data class TmdbMovieDetailDto(
    val id: Long,
    val title: String = "",
    val tagline: String? = null,
    val overview: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    @SerialName("vote_count")
    val voteCount: Int? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val status: String? = null,
    @SerialName("imdb_id")
    val imdbId: String? = null,
    @SerialName("runtime")
    val runtimeMinutes: Int? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
)

