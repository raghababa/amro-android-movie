package com.amro.data.repository

import com.amro.data.image.TmdbImageUrlBuilder
import com.amro.data.mapper.toDomain
import com.amro.data.remote.TmdbRemoteDataSource
import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.domain.model.MovieSummary
import com.amro.domain.repository.MovieRepository
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult

internal class MovieRepositoryImpl(
    private val remoteDataSource: TmdbRemoteDataSource,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : MovieRepository {

    private var cachedGenres: Map<String, List<Genre>> = emptyMap()

    override suspend fun getTrendingMovies(
        timeWindow: TimeWindow,
        language: String,
    ): DomainResult<List<MovieSummary>> {
        val genreLanguage = language.substringBefore('-').ifBlank { "en" }
        val genresResult = getGenresCached(language = genreLanguage)
        val genresById: Map<Int, Genre> = when (genresResult) {
            is DomainResult.Success -> genresResult.value.associateBy { it.id }
            is DomainResult.Error -> return genresResult
        }

        val movies = mutableListOf<MovieSummary>()
        val seenIds = HashSet<Long>()

        for (page in 1..5) {
            val pageResult =
                remoteDataSource.getTrendingMovies(
                    timeWindow = timeWindow.value,
                    language = language,
                    page = page,
                )
            when (pageResult) {
                is DomainResult.Success -> {
                    for (dto in pageResult.value.results) {
                        if (!seenIds.add(dto.id)) continue
                        val mappedGenres = dto.genreIds.mapNotNull(genresById::get)
                        movies += dto.toDomain(
                            posterUrl = imageUrlBuilder.posterSmall(dto.posterPath),
                            backdropUrl = imageUrlBuilder.buildBackdropUrl(
                                dto.backdropPath,
                                TmdbImageUrlBuilder.ImageSize.W780,
                            ),
                            genres = mappedGenres,
                        )
                        if (movies.size >= 100) break
                    }

                    val totalPages = pageResult.value.totalPages ?: 1
                    if (page >= totalPages) break
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

    override suspend fun getMovieGenres(language: String): DomainResult<List<Genre>> =
        getGenresCached(language = language)

    private suspend fun getGenresCached(language: String): DomainResult<List<Genre>> {
        cachedGenres[language]?.let { return DomainResult.Success(it) }

        return when (val result = fetchGenres(language = language)) {
            is DomainResult.Success -> {
                cachedGenres = cachedGenres + (language to result.value)
                result
            }

            is DomainResult.Error -> result
        }
    }

    private suspend fun fetchGenres(language: String): DomainResult<List<Genre>> =
        when (val result = remoteDataSource.getGenres(language = language)) {
            is DomainResult.Success ->
                DomainResult.Success(result.value.genres.map { it.toDomain() })
            is DomainResult.Error -> result
        }

    override suspend fun getMovieDetail(movieId: Long, language: String): DomainResult<MovieDetail> {
        val result = remoteDataSource.getMovieDetail(movieId = movieId, language = language)

        return when (result) {

            is DomainResult.Success -> {
                DomainResult.Success(
                    result.value.toDomain(
                        posterUrl = imageUrlBuilder.posterMedium(result.value.posterPath),
                        backdropUrl = imageUrlBuilder.buildBackdropUrl(
                            result.value.backdropPath,
                            TmdbImageUrlBuilder.ImageSize.W780,
                        ),
                    )
                )
            }

            is DomainResult.Error -> result
        }
    }
}
