package com.amro.amromovieexplorer.navigation

object AppDestinations {
    const val MOVIE_LIST = "movie_list"
    const val MOVIE_DETAIL = "movie_detail"
    const val ARG_MOVIE_ID = "movieId"

    const val MOVIE_DETAIL_ROUTE = "$MOVIE_DETAIL/{$ARG_MOVIE_ID}"

    fun movieDetailRoute(movieId: Long): String = "$MOVIE_DETAIL/$movieId"
}

