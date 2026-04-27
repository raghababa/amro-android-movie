package com.amro.data.repository

import com.amro.data.image.TmdbImageUrlBuilder
import com.amro.data.mapper.toDomain
import com.amro.data.remote.TmdbRemoteDataSource
import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.domain.model.MovieSummary
import com.amro.domain.repository.LanguageCode
import com.amro.domain.repository.MovieRepository
import com.amro.domain.repository.TimeWindow
import com.amro.domain.result.DomainError
import com.amro.domain.result.DomainResult
import javax.inject.Inject

internal class MovieRepositoryImpl @Inject constructor(
    private val remoteDataSource: TmdbRemoteDataSource,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
    private val genreCache: GenreCache,
    private val genreLanguageResolver: GenreLanguageResolver,
    private val trendingMoviesConfig: TrendingMoviesConfig,
) : MovieRepository {

    override suspend fun getTrendingMovies(
        timeWindow: TimeWindow,
        language: LanguageCode,
    ): DomainResult<List<MovieSummary>> {
        val genresById = getTrendingGenresByIdOrEmpty(language = language)

        val movies = mutableListOf<MovieSummary>()
        val seenIds = HashSet<Long>()
        var page = 1

        while (movies.size < trendingMoviesConfig.movieLimit) {
            val pageResult =
                remoteDataSource.getTrendingMovies(
                    timeWindow = timeWindow.value,
                    language = language.value,
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
                        if (movies.size >= trendingMoviesConfig.movieLimit) break
                    }

                    val totalPages = pageResult.value.totalPages ?: break
                    if (page >= totalPages) break
                    page++
                }

                is DomainResult.Error -> return pageResult
            }
        }

        if (movies.isEmpty()) {
            return DomainResult.Error(DomainError.UnexpectedEmpty("Trending movies"))
        }

        return DomainResult.Success(movies.take(trendingMoviesConfig.movieLimit))
    }

    override suspend fun getMovieGenres(language: LanguageCode): DomainResult<List<Genre>> =
        getGenresCached(language = language.value)

    private suspend fun getTrendingGenresByIdOrEmpty(language: LanguageCode): Map<Int, Genre> {
        val genreLanguage = genreLanguageResolver.resolve(language)
        return when (val genresResult = getGenresCached(language = genreLanguage)) {
            is DomainResult.Success -> genresResult.value.associateBy { it.id }
            // Trending should remain usable even if genre labels are temporarily unavailable.
            is DomainResult.Error -> emptyMap()
        }
    }

    private suspend fun getGenresCached(language: String): DomainResult<List<Genre>> {
        genreCache.get(language)?.let { return DomainResult.Success(it) }

        return when (val result = fetchGenres(language = language)) {
            is DomainResult.Success -> {
                genreCache.put(language = language, genres = result.value)
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

    override suspend fun getMovieDetail(movieId: Long, language: LanguageCode): DomainResult<MovieDetail> {
        val result = remoteDataSource.getMovieDetail(movieId = movieId, language = language.value)

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
