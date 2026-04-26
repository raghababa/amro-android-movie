package com.amro.movie_list.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amro.movie_list.ui.action.MovieListAction

@Composable
fun MovieListRoute(
    onMovieSelected: (Long) -> Unit,
    viewModel: MovieListViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    MovieListScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is MovieListAction.SelectGenre -> viewModel.onGenreSelected(action.genreId)
                is MovieListAction.ChangeSortField -> viewModel.onSortFieldSelected(action.field)
                is MovieListAction.ChangeSortOrder -> viewModel.onSortOrderChanged(action.order)
                is MovieListAction.ClickMovie -> onMovieSelected(action.movieId)
                MovieListAction.Retry -> viewModel.onRetry()
            }
        }
    )
}

