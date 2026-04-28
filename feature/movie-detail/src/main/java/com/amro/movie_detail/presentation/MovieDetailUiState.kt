package com.amro.movie_detail.presentation

import com.amro.core.ui.UiText
import com.amro.movie_detail.ui.model.MovieDetailUi

sealed interface MovieDetailUiState {
    fun previousDataOrNull(): MovieDetailUi?

    data class Loading(
        val previousData: MovieDetailUi? = null,
    ) : MovieDetailUiState {
        override fun previousDataOrNull(): MovieDetailUi? = previousData
    }

    data class Error(
        val message: UiText,
        val isRetryable: Boolean,
        val previousData: MovieDetailUi? = null,
    ) : MovieDetailUiState {
        override fun previousDataOrNull(): MovieDetailUi? = previousData
    }

    data class Content(
        val movie: MovieDetailUi,
    ) : MovieDetailUiState {
        override fun previousDataOrNull(): MovieDetailUi = movie
    }
}

