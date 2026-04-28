package com.amro.movie_list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.amro.domain.model.MovieSortField
import com.amro.domain.model.SortOrder
import com.amro.core.ui.theme.spacing
import com.amro.movie_list.R
import com.amro.movie_list.ui.testtags.MovieListTestTags
import com.amro.movie_list.ui.action.MovieListAction
import com.amro.movie_list.ui.state.MovieListConfig
import com.amro.movie_list.ui.state.MovieListUiState

@Composable
fun MovieListEmptyState(
    state: MovieListUiState.Empty,
    onAction: (MovieListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val config = state.config
    Column(modifier = modifier.fillMaxSize()) {
        GenreFilterRow(
            genres = config.availableGenres,
            selectedGenreId = config.selectedGenreId,
            onSelectGenre = { onAction(MovieListAction.SelectGenre(it)) },
            modifier = Modifier.fillMaxWidth()
                .padding(top = MaterialTheme.spacing.lg),
        )

        GenreLoadWarning(
            message = config.genreError,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
        )

        SortSection(
            sortField = config.sortField,
            sortOrder = config.sortOrder,
            onChangeSortField = { onAction(MovieListAction.ChangeSortField(it)) },
            onChangeSortOrder = { onAction(MovieListAction.ChangeSortOrder(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(
                    if (config.hasActiveFilter()) R.string.empty_no_movies_match_filter else R.string.empty_no_movies_found
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (config.hasActiveFilter()) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
                OutlinedButton(
                    onClick = { onAction(MovieListAction.SelectGenre(null)) },
                    modifier = Modifier.testTag(MovieListTestTags.CLEAR_FILTER_BUTTON),
                ) {
                    Text(stringResource(R.string.empty_go_back))
                }
            }
        }
    }
}

@Preview(name = "Movie List Empty", showBackground = true, widthDp = 420)
@Composable
private fun PreviewMovieListEmptyState() {
    MaterialTheme {
        MovieListEmptyState(
            state = MovieListUiState.Empty(
                config = MovieListConfig(
                    availableGenres = previewGenres,
                    selectedGenreId = 35,
                    sortField = MovieSortField.POPULARITY,
                    sortOrder = SortOrder.DESCENDING,
                ),
            ),
            onAction = {},
        )
    }
}

