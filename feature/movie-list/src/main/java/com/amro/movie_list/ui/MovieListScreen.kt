package com.amro.movie_list.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.amro.core.R as CoreR
import com.amro.core.ui.ErrorState
import com.amro.core.ui.FullScreenLoading
import com.amro.core.ui.UiText
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.SortOrder
import com.amro.movie_list.ui.action.MovieListAction
import com.amro.movie_list.ui.model.GenreUi
import com.amro.movie_list.ui.model.MovieSummaryUi
import com.amro.movie_list.ui.state.MovieListConfig
import com.amro.movie_list.ui.state.MovieListUiState
import com.amro.movie_list.ui.components.MovieListContentState
import com.amro.movie_list.ui.components.MovieListEmptyState
import com.amro.movie_list.ui.testtags.MovieListTestTags

@Composable
fun MovieListScreen(
    uiState: MovieListUiState,
    onAction: (MovieListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenModifier = modifier.testTag(MovieListTestTags.SCREEN)
    when (uiState) {
        is MovieListUiState.Loading -> Box(modifier = screenModifier.fillMaxSize()) {
            FullScreenLoading(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(MovieListTestTags.LOADING),
            )
        }
        is MovieListUiState.Empty -> MovieListEmptyState(
            state = uiState,
            onAction = onAction,
            modifier = screenModifier.testTag(MovieListTestTags.EMPTY),
        )
        is MovieListUiState.Error -> ErrorState(
            message = uiState.message,
            primaryActionLabel = if (uiState.isRetryable) UiText.StringRes(CoreR.string.action_retry) else null,
            onPrimaryAction = if (uiState.isRetryable) ({ onAction(MovieListAction.Retry) }) else null,
            modifier = screenModifier.testTag(MovieListTestTags.ERROR),
        )
        is MovieListUiState.Content -> MovieListContentState(
            state = uiState,
            onAction = onAction,
            modifier = screenModifier.testTag(MovieListTestTags.CONTENT),
        )
    }
}


@Preview(name = "MovieList - Content", showBackground = true, widthDp = 420)
@Composable
private fun PreviewMovieListContent() {
    val genres = listOf(
        GenreUi(28, "Action"),
        GenreUi(35, "Comedy"),
        GenreUi(878, "Sci‑Fi"),
    )
    val movies = listOf(
        MovieSummaryUi(
            id = 1,
            title = "The Super Mario Bros. Movie",
            posterUrl = null,
            backdropUrl = null,
            genreNames = listOf("Animation", "Adventure", "Comedy"),
            releaseDate = "2023-04-05",
        ),
        MovieSummaryUi(
            id = 2,
            title = "John Wick: Chapter 4",
            posterUrl = null,
            backdropUrl = null,
            genreNames = listOf("Action", "Thriller"),
            releaseDate = "2023-03-22",
        ),
    )

    MaterialTheme {
        MovieListScreen(
            uiState = MovieListUiState.Content(
                movies = movies,
                config = MovieListConfig(
                    availableGenres = genres,
                    selectedGenreId = null,
                    sortField = MovieSortField.POPULARITY,
                    sortOrder = SortOrder.DESCENDING,
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "MovieList - Empty (filtered)", showBackground = true)
@Composable
private fun PreviewMovieListEmptyFiltered() {
    MaterialTheme {
        MovieListScreen(
            uiState = MovieListUiState.Empty(
                config = MovieListConfig(
                    availableGenres = listOf(
                        GenreUi(28, "Action"),
                        GenreUi(35, "Comedy"),
                    ),
                    selectedGenreId = 35,
                    sortField = MovieSortField.POPULARITY,
                    sortOrder = SortOrder.DESCENDING,
                ),
            ),
            onAction = {},
        )
    }
}

