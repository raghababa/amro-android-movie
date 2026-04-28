package com.amro.amromovieexplorer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amro.movie_detail.ui.MovieDetailRoute
import com.amro.movie_list.ui.MovieListRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.MOVIE_LIST,
        modifier = modifier,
    ) {
        composable(AppDestinations.MOVIE_LIST) {
            MovieListRoute(
                onMovieSelected = { movieId ->
                    navController.navigate(AppDestinations.movieDetailRoute(movieId))
                }
            )
        }

        composable(
            route = AppDestinations.MOVIE_DETAIL_ROUTE,
            arguments = listOf(navArgument(AppDestinations.ARG_MOVIE_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getLong(AppDestinations.ARG_MOVIE_ID) ?: return@composable
            MovieDetailRoute(
                movieId = movieId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

