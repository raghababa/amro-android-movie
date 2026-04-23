package com.amro.data.network.tmdb

import com.amro.data.network.tmdb.dto.TmdbGenresResponse
import com.amro.data.network.tmdb.dto.TmdbMovieDetailDto
import com.amro.data.network.tmdb.dto.TmdbTrendingMoviesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String,
        @Query("language") language: String,
        @Query("page") page: Int,
    ): Response<TmdbTrendingMoviesResponse>

    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("language") language: String,
    ): Response<TmdbGenresResponse>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Long,
        @Query("language") language: String,
    ): Response<TmdbMovieDetailDto>
}

