package com.amro.movie_detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.core.R as CoreR
import com.amro.core.ui.UiText
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import com.amro.domain.usecase.GetMovieDetailUseCase
import com.amro.movie_detail.mapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieDetail: GetMovieDetailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private var loadedMovieId: Long? = null

    fun load(movieId: Long, forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            val current = uiState.value
            if (loadedMovieId == movieId && current is MovieDetailUiState.Content) return
        }
        loadedMovieId = movieId
        _uiState.value = MovieDetailUiState.Loading
        viewModelScope.launch {
            when (val result = getMovieDetail(movieId = movieId)) {
                is DomainResult.Success -> {
                    _uiState.value = MovieDetailUiState.Content(result.value.toUi())
                }
                is DomainResult.Error -> {
                    _uiState.value = result.toUiError()
                }
            }
        }
    }
}

private fun DomainResult.Error.toUiError(): MovieDetailUiState.Error {
    val (message, retryable) = when (val e = error) {
        is DomainError.Network -> UiText.StringRes(CoreR.string.error_network) to true
        is DomainError.Configuration -> UiText.StringRes(CoreR.string.error_configuration) to false
        DomainError.Unauthorized -> UiText.StringRes(CoreR.string.error_unauthorized) to false
        DomainError.NotFound -> UiText.StringRes(CoreR.string.error_movie_not_found) to false
        DomainError.RateLimited -> UiText.StringRes(CoreR.string.error_rate_limited) to true
        DomainError.Server -> UiText.StringRes(CoreR.string.error_server) to true
        is DomainError.InvalidInput -> UiText.StringRes(CoreR.string.error_movie_not_found) to false
        is DomainError.Empty -> UiText.StringRes(CoreR.string.error_empty_result) to false
        is DomainError.Serialization -> UiText.StringRes(CoreR.string.error_unexpected_response) to false
        is DomainError.Unknown -> UiText.StringRes(CoreR.string.error_something_went_wrong) to false
    }
    return MovieDetailUiState.Error(message = message, isRetryable = retryable)
}

