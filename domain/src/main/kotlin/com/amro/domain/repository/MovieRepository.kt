package com.amro.domain.repository

import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.domain.model.MovieSummary
import com.amro.domain.result.DomainResult

typealias MoviesResult = DomainResult<List<MovieSummary>>

interface MovieRepository {
    /**
     * Returns trending movies for a given time window.
     *
     * Result size and pagination are capped internally by the repository implementation.
     */
    suspend fun getTrendingMovies(
        timeWindow: TimeWindow,
        language: LanguageCode = LanguageCode.EN_US,
    ): MoviesResult

    suspend fun getMovieGenres(
        language: LanguageCode = LanguageCode.EN,
    ): DomainResult<List<Genre>>

    suspend fun getMovieDetail(
        movieId: Long,
        language: LanguageCode = LanguageCode.EN_US,
    ): DomainResult<MovieDetail>
}

enum class TimeWindow(val value: String) {
    DAY("day"),
    WEEK("week"),
}

@JvmInline
value class LanguageCode(val value: String) {
    init {
        require(value.matches(LANGUAGE_CODE_PATTERN)) { "Invalid language code format." }
    }

    companion object {
        val EN = LanguageCode("en")
        val EN_US = LanguageCode("en-US")
    }
}

private val LANGUAGE_CODE_PATTERN = Regex("^[a-z]{2}(-[A-Z]{2})?$")

