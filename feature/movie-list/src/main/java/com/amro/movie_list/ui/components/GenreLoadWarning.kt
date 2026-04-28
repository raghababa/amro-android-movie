package com.amro.movie_list.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.amro.core.ui.UiText
import com.amro.core.ui.asString
import com.amro.core.ui.theme.spacing
import com.amro.movie_list.ui.testtags.MovieListTestTags

@Composable
internal fun GenreLoadWarning(
    message: UiText?,
    modifier: Modifier = Modifier,
) {
    if (message == null) return

    Text(
        text = message.asString(),
        modifier = modifier
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
            .testTag(MovieListTestTags.GENRE_ERROR),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
    )
}
