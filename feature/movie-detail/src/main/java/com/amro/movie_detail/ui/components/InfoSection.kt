package com.amro.movie_detail.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.amro.core.ui.theme.spacing
import com.amro.movie_detail.R as DetailR
import com.amro.movie_detail.ui.model.MovieDetailUi

@Composable
fun InfoSection(
    movie: MovieDetailUi,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (movie.genres.isNotEmpty()) {
            GenreTags(tags = movie.genres)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
        }

        movie.overview?.let { overview ->
            Text(stringResource(DetailR.string.section_overview), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Text(
                text = overview,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(name = "Info Section", showBackground = true)
@Composable
private fun PreviewInfoSection() {
    MaterialTheme {
        InfoSection(movie = previewMovieDetail)
    }
}

