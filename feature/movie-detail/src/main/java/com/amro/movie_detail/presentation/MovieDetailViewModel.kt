package com.amro.movie_detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.core.R as CoreR
import com.amro.core.ui.DomainErrorUiMapper
import com.amro.domain.result.DomainResult
import com.amro.domain.usecase.GetMovieDetailUseCase
import com.amro.movie_detail.mapper.MovieDetailUiMapper
import com.amro.movie_detail.ui.model.MovieDetailUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val getMovieDetail: GetMovieDetailUseCase,
    private val movieDetailUiMapper: MovieDetailUiMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private var requestedMovieId: Long? = null
    private var loadJob: Job? = null

    fun retry() {
        requestedMovieId?.let { load(movieId = it, forceRefresh = true) }
    }

    fun load(movieId: Long, forceRefresh: Boolean = false) {
        val current = uiState.value

        if (!forceRefresh && requestedMovieId == movieId && current is MovieDetailUiState.Content) return

        val previousData = if (requestedMovieId == movieId) {
            current.previousDataOrNull()
        } else {
            null
        }

        requestedMovieId = movieId
        _uiState.value = MovieDetailUiState.Loading(previousData)

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            when (val result = getMovieDetail(movieId = movieId)) {
                is DomainResult.Success -> {
                    _uiState.value = MovieDetailUiState.Content(movieDetailUiMapper.toUi(result.value))
                }
                is DomainResult.Error -> {
                    _uiState.value = result.toUiError(previousData)
                }
            }
        }
    }
}

private fun DomainResult.Error.toUiError(previousData: MovieDetailUi? = null): MovieDetailUiState.Error {
    val errorUi = DomainErrorUiMapper.map(
        error = error,
        notFoundMessageRes = CoreR.string.error_movie_not_found,
        invalidInputMessageRes = CoreR.string.error_movie_not_found,
    )
    return MovieDetailUiState.Error(
        message = errorUi.message,
        isRetryable = errorUi.isRetryable,
        previousData = previousData,
    )
}

