package com.amro.movie_detail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amro.movie_detail.presentation.MovieDetailViewModel

@Composable
fun MovieDetailRoute(
    movieId: Long,
    onBack: () -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(movieId) {
        viewModel.load(movieId)
    }

    MovieDetailScreen(
        uiState = uiState,
        onRetry = { viewModel.load(movieId, forceRefresh = true) },
        onBack = onBack,
        onOpenImdb = { url ->
            runCatching { uriHandler.openUri(url) }
        }
    )
}

