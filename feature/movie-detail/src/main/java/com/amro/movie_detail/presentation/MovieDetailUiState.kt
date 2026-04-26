package com.amro.movie_detail.presentation

import com.amro.core.ui.UiText
import com.amro.movie_detail.ui.model.MovieDetailUi

sealed interface MovieDetailUiState {
    data object Loading : MovieDetailUiState

    data class Error(
        val message: UiText,
        val isRetryable: Boolean,
    ) : MovieDetailUiState

    data class Content(
        val movie: MovieDetailUi,
    ) : MovieDetailUiState
}

