package com.amro.domain.usecase

import com.amro.domain.model.MovieDetail
import com.amro.domain.repository.MovieRepository
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import javax.inject.Inject

class GetMovieDetailUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(
        movieId: Long,
        language: String = "en-US",
    ): DomainResult<MovieDetail> {
        if (movieId <= 0) {
            return DomainResult.Error(DomainError.InvalidInput(field = "movieId"))
        }

        return repository.getMovieDetail(movieId = movieId, language = language)
    }
}

