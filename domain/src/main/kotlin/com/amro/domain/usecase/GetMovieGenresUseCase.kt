package com.amro.domain.usecase

import com.amro.domain.model.Genre
import com.amro.domain.repository.MovieRepository
import com.amro.domain.result.DomainResult
import javax.inject.Inject

class GetMovieGenresUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(
        language: String = "en",
    ): DomainResult<List<Genre>> =
        repository.getMovieGenres(language = language)
}

