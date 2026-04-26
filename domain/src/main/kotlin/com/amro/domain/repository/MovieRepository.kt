package com.amro.domain.repository

import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.domain.model.MovieSummary
import com.amro.domain.result.DomainResult

interface MovieRepository {
    suspend fun getTrendingMovies(
        timeWindow: TimeWindow,
        language: String = "en-US",
    ): DomainResult<List<MovieSummary>>

    suspend fun getMovieGenres(
        language: String = "en",
    ): DomainResult<List<Genre>>

    suspend fun getMovieDetail(
        movieId: Long,
        language: String = "en-US",
    ): DomainResult<MovieDetail>
}

enum class TimeWindow(val value: String) {
    Day("day"),
    Week("week"),
}

