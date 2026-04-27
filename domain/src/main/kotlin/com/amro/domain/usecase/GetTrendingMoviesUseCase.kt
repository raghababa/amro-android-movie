package com.amro.domain.usecase

import com.amro.domain.model.MovieSummary
import com.amro.domain.repository.LanguageCode
import com.amro.domain.repository.MovieRepository
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainResult
import javax.inject.Inject

class GetTrendingMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(
        timeWindow: TimeWindow,
        language: LanguageCode = LanguageCode.EN_US,
    ): DomainResult<List<MovieSummary>> =
        repository.getTrendingMovies(timeWindow = timeWindow, language = language)
}

