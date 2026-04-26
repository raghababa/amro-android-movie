package com.amro.movie_list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.SortOrder
import com.amro.core.ui.theme.spacing
import com.amro.movie_list.ui.testtags.MovieListTestTags
import com.amro.movie_list.ui.action.MovieListAction
import com.amro.movie_list.ui.state.MovieListUiState

@Composable
fun MovieListContentState(
    state: MovieListUiState.Content,
    onAction: (MovieListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(
        state.selectedGenreId,
        state.sortField,
        state.sortOrder,
    ) {
        listState.animateScrollToItem(0)
    }
    Column(modifier =
        modifier.fillMaxSize()
    ) {
        MovieHeroCarousel(
            movies = state.movies,
            onMovieClick = { onAction(MovieListAction.ClickMovie(it)) },
            modifier = Modifier.fillMaxWidth()
            .padding(top = MaterialTheme.spacing.sm),
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        GenreFilterRow(
            genres = state.availableGenres,
            selectedGenreId = state.selectedGenreId,
            onSelectGenre = { onAction(MovieListAction.SelectGenre(it)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        SortSection(
            sortField = state.sortField,
            sortOrder = state.sortOrder,
            onChangeSortField = { onAction(MovieListAction.ChangeSortField(it)) },
            onChangeSortOrder = { onAction(MovieListAction.ChangeSortOrder(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.lg),
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(MovieListTestTags.LIST),
            state = listState,
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            items(state.movies, key = { it.id }) { movie ->
                MovieRow(
                    movie = movie,
                    onClick = { onAction(MovieListAction.ClickMovie(movie.id)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(MovieListTestTags.movieItem(movie.id)),
                )
            }
        }
    }
}

@Preview(name = "Movie List Content", showBackground = true, widthDp = 420)
@Composable
private fun PreviewMovieListContentState() {
    MaterialTheme {
        MovieListContentState(
            state = MovieListUiState.Content(
                movies = previewMovies,
                availableGenres = previewGenres,
                selectedGenreId = null,
                sortField = MovieSortField.POPULARITY,
                sortOrder = SortOrder.DESCENDING,
            ),
            onAction = {},
        )
    }
}
