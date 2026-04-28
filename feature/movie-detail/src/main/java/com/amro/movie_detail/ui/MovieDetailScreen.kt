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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.amro.core.R as CoreR
import com.amro.core.links.ExternalUrls
import com.amro.core.ui.ErrorState
import com.amro.core.ui.FullScreenLoading
import com.amro.core.ui.UiText
import com.amro.core.ui.asString
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
        is MovieDetailUiState.Loading -> {
            val previousData = uiState.previousData
            if (previousData == null) {
                FullScreenLoading(
                    modifier = screenModifier.testTag(MovieDetailTestTags.LOADING),
                )
            } else {
                MovieDetailContent(
                    movie = previousData,
                    isRefreshing = true,
                    errorMessage = null,
                    isRetryable = false,
                    onRetry = onRetry,
                    onBack = onBack,
                    onOpenImdb = onOpenImdb,
                    modifier = screenModifier.testTag(MovieDetailTestTags.CONTENT),
                )
            }
        }
        is MovieDetailUiState.Error -> {
            val previousData = uiState.previousData
            if (previousData == null) {
                ErrorState(
                    message = uiState.message,
                    primaryActionLabel = if (uiState.isRetryable) UiText.StringRes(CoreR.string.action_retry) else null,
                    onPrimaryAction = if (uiState.isRetryable) onRetry else null,
                    secondaryActionLabel = UiText.StringRes(CoreR.string.action_back),
                    onSecondaryAction = onBack,
                    modifier = screenModifier.testTag(MovieDetailTestTags.ERROR),
                )
            } else {
                MovieDetailContent(
                    movie = previousData,
                    isRefreshing = false,
                    errorMessage = uiState.message,
                    isRetryable = uiState.isRetryable,
                    onRetry = onRetry,
                    onBack = onBack,
                    onOpenImdb = onOpenImdb,
                    modifier = screenModifier.testTag(MovieDetailTestTags.CONTENT),
                )
            }
        }
        is MovieDetailUiState.Content -> MovieDetailContent(
            movie = uiState.movie,
            isRefreshing = false,
            errorMessage = null,
            isRetryable = false,
            onRetry = onRetry,
            onBack = onBack,
            onOpenImdb = onOpenImdb,
            modifier = screenModifier.testTag(MovieDetailTestTags.CONTENT),
        )
    }
}

@Composable
private fun MovieDetailContent(
    movie: MovieDetailUi,
    isRefreshing: Boolean,
    errorMessage: UiText?,
    isRetryable: Boolean,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onOpenImdb: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
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
        if (isRefreshing) {
            val loadingDescription = stringResource(CoreR.string.cd_loading)
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(MovieDetailTestTags.LOADING)
                    .semantics {
                        contentDescription = loadingDescription
                    },
            )
        }
        errorMessage?.let { message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(MovieDetailTestTags.ERROR),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = message.asString(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                if (isRetryable) {
                    TextButton(onClick = onRetry) {
                        Text(text = stringResource(CoreR.string.action_retry))
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(MovieDetailTestTags.LIST),
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.screenHorizontal,
                vertical = MaterialTheme.spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
        ) {

            item(key = "header") {
                HeaderSection(
                    movie = movie,
                    modifier = Modifier.testTag(MovieDetailTestTags.HEADER_SECTION),
                )
            }
            item(key = "info") {
                InfoSection(
                    movie = movie,
                    modifier = Modifier.testTag(MovieDetailTestTags.INFO_SECTION),
                )
            }
            item(key = "stats") {
                StatsSection(
                    movie = movie,
                    onOpenImdb = onOpenImdb,
                    modifier = Modifier.testTag(MovieDetailTestTags.STATS_SECTION),
                )
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
        budget = "$11M",
        revenue = "$775M",
        status = "Released",
        imdbUrl = ExternalUrls.imdbTitleUrl("tt0076759"),
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

