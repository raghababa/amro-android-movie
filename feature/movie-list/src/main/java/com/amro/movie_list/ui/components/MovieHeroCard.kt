package com.amro.movie_list.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.amro.core.ui.theme.dimens
import com.amro.core.ui.theme.spacing
import com.amro.movie_list.R
import com.amro.movie_list.ui.model.MovieSummaryUi

@Composable
internal fun MovieHeroCard(
    movie: MovieSummaryUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    val scrim = MaterialTheme.colorScheme.scrim

    Surface(
        modifier = modifier
            .height(MaterialTheme.dimens.sizes.heroPosterHeight)
            .clip(shape)
            .clickable(
                onClick = onClick,
            ),
        shape = shape,
        tonalElevation = MaterialTheme.dimens.elevations.subtle,
        shadowElevation = MaterialTheme.dimens.elevations.card,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = movie.backdropUrl ?: movie.posterUrl,
                contentDescription = stringResource(R.string.cd_movie_poster, movie.title),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                scrim.copy(alpha = 0f),
                                scrim.copy(alpha = 0.8f),
                            ),
                        )
                    )
                    .padding(MaterialTheme.spacing.md)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(name = "Movie Hero Card", showBackground = true, widthDp = 420)
@Composable
private fun PreviewMovieHeroCard() {
    MaterialTheme {
        MovieHeroCard(
            movie = previewMovies.first(),
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
