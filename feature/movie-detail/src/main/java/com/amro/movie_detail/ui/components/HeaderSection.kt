package com.amro.movie_detail.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.amro.core.ui.theme.dimens
import com.amro.core.ui.theme.spacing
import com.amro.movie_detail.ui.model.MovieDetailUi

@Composable
fun HeaderSection(
    movie: MovieDetailUi,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    Column(modifier = modifier.fillMaxWidth()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.dimens.sizes.heroPosterHeight)
                .clip(shape),
        ) {
            PosterPlaceholder(
                modifier = Modifier.fillMaxSize(),
                shape = shape,
            )
            (movie.backdropUrl ?: movie.posterUrl)?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape),
                    contentScale = ContentScale.Crop,

                    alignment = Alignment.TopCenter,
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))

        Text(
            text = movie.title,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        movie.tagline?.let {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Header Section", showBackground = true, widthDp = 420)
@Composable
private fun PreviewHeaderSection() {
    MaterialTheme {
        HeaderSection(movie = previewMovieDetail)
    }
}

