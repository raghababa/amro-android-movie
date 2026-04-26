package com.amro.movie_list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.amro.core.ui.theme.spacing
import com.amro.movie_list.R
import com.amro.movie_list.ui.model.GenreUi

@Composable
fun GenreFilterRow(
    genres: List<GenreUi>,
    selectedGenreId: Int?,
    onSelectGenre: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selectedGenreId == null,
                onClick = { onSelectGenre(null) },
                label = { Text(stringResource(R.string.filter_all)) },
            )
        }
        items(genres, key = { it.id }) { genre ->
            FilterChip(
                selected = selectedGenreId == genre.id,
                onClick = { onSelectGenre(genre.id) },
                label = { Text(genre.name) },
            )
        }
    }
}

