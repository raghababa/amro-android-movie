package com.amro.data.remote

import com.amro.data.network.apiCall
import com.amro.data.network.tmdb.TmdbApi
import com.amro.data.network.tmdb.dto.TmdbGenresResponse
import com.amro.data.network.tmdb.dto.TmdbMovieDetailDto
import com.amro.data.network.tmdb.dto.TmdbTrendingMoviesResponse
import com.amro.domain.result.DomainResult
import javax.inject.Inject

internal class TmdbRemoteDataSourceImpl @Inject constructor(
    private val api: TmdbApi,
) : TmdbRemoteDataSource {

    override suspend fun getTrendingMovies(
        timeWindow: String,
        language: String,
        page: Int,
    ): DomainResult<TmdbTrendingMoviesResponse> =
        apiCall {
            api.getTrendingMovies(
                timeWindow = timeWindow,
                language = language,
                page = page,
            )
        }

    override suspend fun getGenres(language: String): DomainResult<TmdbGenresResponse> =
        apiCall { api.getMovieGenres(language = language) }

    override suspend fun getMovieDetail(
        movieId: Long,
        language: String,
    ): DomainResult<TmdbMovieDetailDto> =
        apiCall { api.getMovieDetail(movieId = movieId, language = language) }
}
