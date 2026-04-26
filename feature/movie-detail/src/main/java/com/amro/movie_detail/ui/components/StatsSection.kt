package com.amro.movie_detail.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.amro.core.R as CoreR
import com.amro.core.ui.theme.spacing
import com.amro.movie_detail.R as DetailR
import com.amro.movie_detail.ui.testtags.MovieDetailTestTags
import com.amro.movie_detail.ui.model.MovieDetailUi

@Composable
fun StatsSection(
    movie: MovieDetailUi,
    onOpenImdb: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(stringResource(DetailR.string.section_stats), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))

        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
            StatItem(label = stringResource(DetailR.string.stats_rating), value = "%.1f".format(movie.voteAverage))
            StatItem(label = stringResource(DetailR.string.stats_votes), value = movie.voteCount.toString())
            val runtimeValue =
                movie.runtimeMinutes?.let { stringResource(DetailR.string.runtime_minutes, it) }
                    ?: stringResource(CoreR.string.placeholder_dash)
            StatItem(label = stringResource(DetailR.string.stats_runtime), value = runtimeValue)
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))

        Text(stringResource(DetailR.string.section_financials), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
            StatItem(label = stringResource(DetailR.string.stats_budget), value = movie.budget ?: stringResource(CoreR.string.placeholder_dash))
            StatItem(label = stringResource(DetailR.string.stats_revenue), value = movie.revenue ?: stringResource(CoreR.string.placeholder_dash))
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))

        Text(stringResource(DetailR.string.section_status), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
        Text(text = movie.status ?: stringResource(CoreR.string.placeholder_dash), style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

        Text(stringResource(DetailR.string.section_release_date), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
        Text(text = movie.releaseDate ?: stringResource(CoreR.string.placeholder_dash), style = MaterialTheme.typography.bodyMedium)

        movie.imdbUrl?.let { url ->
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
            OutlinedButton(
                onClick = { onOpenImdb(url) },
                modifier = Modifier.testTag(MovieDetailTestTags.OPEN_IMDB_BUTTON),
            ) {
                Text(stringResource(DetailR.string.action_open_imdb))
            }
        }
    }
}

@Preview(name = "Stats Section", showBackground = true)
@Composable
private fun PreviewStatsSection() {
    MaterialTheme {
        StatsSection(
            movie = previewMovieDetail,
            onOpenImdb = {},
        )
    }
}

