package com.amro.movie_detail.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.amro.movie_detail.R as DetailR
import com.amro.movie_detail.ui.testtags.MovieDetailTestTags

@Composable
internal fun PosterPlaceholder(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
) {
    Surface(
        modifier = modifier
            .clip(shape)
            .testTag(MovieDetailTestTags.POSTER_PLACEHOLDER),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(DetailR.string.empty_no_poster),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Poster Placeholder", showBackground = true)
@Composable
private fun PreviewPosterPlaceholder() {
    MaterialTheme {
        PosterPlaceholder()
    }
}
