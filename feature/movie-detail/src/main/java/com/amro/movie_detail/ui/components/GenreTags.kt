package com.amro.movie_detail.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.amro.core.ui.theme.spacing
import com.amro.movie_detail.ui.testtags.MovieDetailTestTags

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GenreTags(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    if (tags.isEmpty()) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        tags.forEachIndexed { index, tag ->
            GenreTag(
                text = tag,
                modifier = Modifier.testTag(MovieDetailTestTags.genreTag(index)),
            )
        }
    }
}

@Preview(name = "Genre Tags", showBackground = true)
@Composable
private fun PreviewGenreTags() {
    MaterialTheme {
        GenreTags(tags = previewMovieDetail.genres)
    }
}
