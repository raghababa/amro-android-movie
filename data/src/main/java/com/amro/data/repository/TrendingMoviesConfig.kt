package com.amro.data.repository

data class TrendingMoviesConfig(
    val movieLimit: Int = DEFAULT_TRENDING_MOVIE_LIMIT,
)

private const val DEFAULT_TRENDING_MOVIE_LIMIT = 100
