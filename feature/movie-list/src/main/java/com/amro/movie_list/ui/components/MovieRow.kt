package com.amro.movie_list.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
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
fun MovieRow(
    movie: MovieSummaryUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.cd_open_movie_details, movie.title),
                onClick = onClick,
            )
            .padding(MaterialTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = MaterialTheme.dimens.sizes.moviePosterWidth,
                    height = MaterialTheme.dimens.sizes.moviePosterHeight,
                )
                .clip(RoundedCornerShape(MaterialTheme.dimens.radii.sm))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = stringResource(R.string.cd_movie_poster, movie.title),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (movie.genreNames.isNotEmpty()) {
                Text(
                    text = movie.genreNames.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            movie.releaseDate?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Movie Row", showBackground = true, widthDp = 420)
@Composable
private fun PreviewMovieRow() {
    MaterialTheme {
        MovieRow(
            movie = previewMovies.first(),
            onClick = {},
        )
    }
}

