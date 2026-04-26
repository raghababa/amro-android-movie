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
        popularity = popularity,
        releaseDate = releaseDate,
    )

internal fun TmdbGenreDto.toDomain(): Genre =
    Genre(
        id = id,
        name = name,
    )

internal fun TmdbMovieDetailDto.toDomain(
    posterUrl: String?,
): MovieDetail =
    MovieDetail(
        id = id,
        title = title,
        tagline = tagline,
        overview = overview,
        posterUrl = posterUrl,
        genres = genres.map { it.toDomain() },
        voteAverage = voteAverage,
        voteCount = voteCount,
        budget = budget,
        revenue = revenue,
        status = status,
        imdbId = imdbId,
        runtimeMinutes = runtimeMinutes,
        releaseDate = releaseDate,
    )

