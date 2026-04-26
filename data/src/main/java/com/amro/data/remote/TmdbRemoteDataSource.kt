package com.amro.data.remote

import com.amro.data.network.tmdb.dto.TmdbGenresResponse
import com.amro.data.network.tmdb.dto.TmdbMovieDetailDto
import com.amro.data.network.tmdb.dto.TmdbTrendingMoviesResponse
import com.amro.domain.result.DomainResult

interface TmdbRemoteDataSource {
    suspend fun getTrendingMovies(
        timeWindow: String,
        language: String,
        page: Int,
    ): DomainResult<TmdbTrendingMoviesResponse>

    suspend fun getGenres(language: String): DomainResult<TmdbGenresResponse>

    suspend fun getMovieDetail(
        movieId: Long,
        language: String,
    ): DomainResult<TmdbMovieDetailDto>
}
