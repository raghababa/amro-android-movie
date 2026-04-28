package com.amro.movie_list.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amro.core.ui.theme.spacing
import com.amro.movie_list.ui.model.MovieSummaryUi

@Composable
fun MovieHeroCarousel(
    movies: List<MovieSummaryUi>,
    onMovieClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    autoScrollMs: Long = 3500L,
) {
    if (movies.isEmpty()) return

    val heroMovies = movies.take(12)
    val pagerState = rememberPagerState(pageCount = { heroMovies.size })

    AutoAdvancePager(pagerState = pagerState, autoScrollMs = autoScrollMs)

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        pageSpacing = 0.dp,
        verticalAlignment = Alignment.CenterVertically,
    ) { page ->
        val movie = heroMovies[page]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.screenHorizontal),
        ) {
            MovieHeroCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Movie Hero Carousel", showBackground = true, widthDp = 420)
@Composable
private fun PreviewMovieHeroCarousel() {
    MaterialTheme {
        MovieHeroCarousel(
            movies = previewMovies,
            onMovieClick = {},
            autoScrollMs = Long.MAX_VALUE,
        )
    }
}

internal val previewMovies = listOf(
    MovieSummaryUi(
        id = 1,
        title = "Apex",
        posterUrl = null,
        backdropUrl = null,
        genreNames = listOf("Action", "Thriller"),
        releaseDate = "2026-02-01",
    ),
    MovieSummaryUi(
        id = 2,
        title = "Family Night",
        posterUrl = null,
        backdropUrl = null,
        genreNames = listOf("Comedy", "Adventure"),
        releaseDate = "2026-01-15",
    ),
)

