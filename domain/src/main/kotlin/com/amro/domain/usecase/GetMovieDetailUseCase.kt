package com.amro.domain.usecase

import com.amro.domain.model.MovieDetail
import com.amro.domain.repository.LanguageCode
import com.amro.domain.repository.MovieRepository
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import javax.inject.Inject

class GetMovieDetailUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(
        movieId: Long,
        language: LanguageCode = LanguageCode.EN_US,
    ): DomainResult<MovieDetail> {
        if (movieId <= 0) {
            return DomainResult.Error(DomainError.InvalidInput(field = "movieId"))
        }

        return repository.getMovieDetail(movieId = movieId, language = language)
    }
}

