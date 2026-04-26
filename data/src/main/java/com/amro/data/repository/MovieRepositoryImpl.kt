package com.amro.data.repository

import android.util.Log
import com.amro.data.image.TmdbImageUrlBuilder
import com.amro.data.mapper.toDomain
import com.amro.data.network.apiCall
import com.amro.data.network.tmdb.TmdbApi
import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.domain.model.MovieSummary
import com.amro.domain.repository.MovieRepository
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult

internal class MovieRepositoryImpl(
    private val api: TmdbApi,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : MovieRepository {

    override suspend fun getTrendingMovies(
        timeWindow: TimeWindow,
        language: String,
    ): DomainResult<List<MovieSummary>> {
        val genreLanguage = language.substringBefore('-').ifBlank { "en" }
        val genresResult = getMovieGenres(language = genreLanguage)
        val genresById: Map<Int, Genre> = when (genresResult) {
            is DomainResult.Success -> genresResult.value.associateBy { it.id }
            is DomainResult.Error -> return genresResult
        }

        val movies = mutableListOf<MovieSummary>()
        val seenIds = HashSet<Long>()

        for (page in 1..5) {
            val pageResult =
                apiCall { api.getTrendingMovies(timeWindow = timeWindow.value, language = language, page = page) }
            when (pageResult) {
                is DomainResult.Success -> {
                    for (dto in pageResult.value.results) {
                        if (!seenIds.add(dto.id)) continue
                        val mappedGenres = dto.genreIds.mapNotNull(genresById::get)
                        movies += dto.toDomain(
                            posterUrl = imageUrlBuilder.posterSmall(dto.posterPath),
                            backdropUrl = imageUrlBuilder.buildBackdropUrl(dto.backdropPath, TmdbImageUrlBuilder.ImageSize.W780),
                            genres = mappedGenres,
                        )
                        if (movies.size >= 100) break
                    }
                }

                is DomainResult.Error -> return pageResult
            }

            if (movies.size >= 100) break
        }

        if (movies.isEmpty()) {
            return DomainResult.Error(DomainError.Empty("Trending movies"))
        }

        return DomainResult.Success(movies.take(100))
    }

    override suspend fun getMovieGenres(language: String): DomainResult<List<Genre>> {
        val result = apiCall { api.getMovieGenres(language = language) }
        return when (result) {
            is DomainResult.Success ->
                DomainResult.Success(result.value.genres.map { it.toDomain() })
            is DomainResult.Error -> result
        }
    }

    override suspend fun getMovieDetail(movieId: Long, language: String): DomainResult<MovieDetail> {
        val result = apiCall { api.getMovieDetail(movieId = movieId, language = language) }

        return when (result) {

            is DomainResult.Success ->{

                DomainResult.Success(
                    result.value.toDomain(
                        posterUrl = imageUrlBuilder.posterMedium(result.value.posterPath),
                    )
                )
            }
            is DomainResult.Error -> result

        }
    }
}
