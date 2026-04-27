package com.amro.data.mapper

import com.amro.data.network.tmdb.dto.TmdbGenreDto
import com.amro.data.network.tmdb.dto.TmdbMovieDetailDto
import com.amro.data.network.tmdb.dto.TmdbTrendingMovieDto
import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.domain.model.MovieSummary

internal fun TmdbTrendingMovieDto.toDomain(
    posterUrl: String?,
    backdropUrl: String?,
    genres: List<Genre>,
): MovieSummary =
    MovieSummary(
        id = id,
        title = title,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        genres = genres,
        popularity = popularity ?: DEFAULT_POPULARITY,
        releaseDate = releaseDate,
    )

internal fun TmdbGenreDto.toDomain(): Genre =
    Genre(
        id = id,
        name = name,
    )

internal fun TmdbMovieDetailDto.toDomain(
    posterUrl: String?,
    backdropUrl: String?,
): MovieDetail =
    MovieDetail(
        id = id,
        title = title,
        tagline = tagline,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        genres = genres.map { it.toDomain() },
        voteAverage = voteAverage ?: DEFAULT_VOTE_AVERAGE,
        voteCount = voteCount ?: DEFAULT_VOTE_COUNT,
        budget = budget ?: DEFAULT_BUDGET,
        revenue = revenue ?: DEFAULT_REVENUE,
        status = status,
        imdbId = imdbId,
        runtimeMinutes = runtimeMinutes,
        releaseDate = releaseDate,
    )

private const val DEFAULT_POPULARITY = 0.0
private const val DEFAULT_VOTE_AVERAGE = 0.0
private const val DEFAULT_VOTE_COUNT = 0
private const val DEFAULT_BUDGET = 0L
private const val DEFAULT_REVENUE = 0L
