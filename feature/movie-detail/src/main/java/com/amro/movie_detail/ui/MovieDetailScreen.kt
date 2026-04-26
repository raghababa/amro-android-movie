package com.amro.movie_detail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.amro.core.R as CoreR
import com.amro.core.ui.ErrorState
import com.amro.core.ui.FullScreenLoading
import com.amro.core.ui.UiText
import com.amro.core.ui.theme.spacing
import com.amro.movie_detail.presentation.MovieDetailUiState
import com.amro.movie_detail.ui.components.HeaderSection
import com.amro.movie_detail.ui.components.InfoSection
import com.amro.movie_detail.ui.components.StatsSection
import com.amro.movie_detail.ui.model.MovieDetailUi
import com.amro.movie_detail.ui.testtags.MovieDetailTestTags
import com.amro.movie_detail.R as DetailR

@Composable
fun MovieDetailScreen(
    uiState: MovieDetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onOpenImdb: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenModifier = modifier.testTag(MovieDetailTestTags.SCREEN)
    when (uiState) {
        MovieDetailUiState.Loading -> FullScreenLoading(
            modifier = screenModifier.testTag(MovieDetailTestTags.LOADING),
        )
        is MovieDetailUiState.Error -> ErrorState(
            message = uiState.message,
            primaryActionLabel = if (uiState.isRetryable) UiText.StringRes(CoreR.string.action_retry) else null,
            onPrimaryAction = if (uiState.isRetryable) onRetry else null,
            secondaryActionLabel = UiText.StringRes(CoreR.string.action_back),
            onSecondaryAction = onBack,
            modifier = screenModifier.testTag(MovieDetailTestTags.ERROR),
        )
        is MovieDetailUiState.Content -> {
            Column(
                modifier = screenModifier.testTag(MovieDetailTestTags.CONTENT),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(MovieDetailTestTags.BACK_BUTTON),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(DetailR.string.cd_back),
                        )
                    }
                    Text(
                        text = stringResource(DetailR.string.title_details),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(MovieDetailTestTags.LIST),
                    contentPadding = PaddingValues(MaterialTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
                ) {

                    item(key = "header") {
                        HeaderSection(
                            movie = uiState.movie,
                            modifier = Modifier.testTag(MovieDetailTestTags.HEADER_SECTION),
                        )
                    }
                    item(key = "info") {
                        InfoSection(
                            movie = uiState.movie,
                            modifier = Modifier.testTag(MovieDetailTestTags.INFO_SECTION),
                        )
                    }
                    item(key = "stats") {
                        StatsSection(
                            movie = uiState.movie,
                            onOpenImdb = onOpenImdb,
                            modifier = Modifier.testTag(MovieDetailTestTags.STATS_SECTION),
                        )
                    }
                }
            }
        }
    }
}



@Preview(name = "MovieDetail - Content", showBackground = true, widthDp = 420)
@Composable
private fun PreviewMovieDetailContent() {
    val movie = MovieDetailUi(
        id = 11,
        title = "Star Wars",
        tagline = "A long time ago in a galaxy far, far away...",
        overview = "Princess Leia is captured and held hostage by the evil Imperial forces...",
        posterUrl = null,
        backdropUrl = null,
        genres = listOf("Adventure", "Action", "Science Fiction"),
        voteAverage = 8.2,
        voteCount = 22061,
        budget = "$11,000,000.00",
        revenue = "$775,398,007.00",
        status = "Released",
        imdbUrl = "https://www.imdb.com/title/tt0076759/",
        runtimeMinutes = 121,
        releaseDate = "1977-05-25",
    )

    MaterialTheme {
        MovieDetailScreen(
            uiState = MovieDetailUiState.Content(movie),
            onRetry = {},
            onBack = {},
            onOpenImdb = {},
        )
    }
}

