package com.amro.data.mapper

import com.amro.data.network.tmdb.dto.TmdbGenreDto
import com.amro.data.network.tmdb.dto.TmdbMovieDetailDto
import com.amro.data.network.tmdb.dto.TmdbTrendingMovieDto
import com.amro.domain.model.Genre
import org.junit.Assert.assertEquals
import org.junit.Test

class TmdbMappersTest {

    @Test
    fun `trending movie dto maps to domain summary`() {
        val dto = TmdbTrendingMovieDto(
            id = 10,
            title = "Heat",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            genreIds = listOf(28),
            popularity = 123.4,
            releaseDate = "1995-12-15",
        )
        val genres = listOf(Genre(id = 28, name = "Action"))

        val result = dto.toDomain(
            posterUrl = "https://image.tmdb.org/t/p/w185/poster.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop.jpg",
            genres = genres,
        )

        assertEquals(10L, result.id)
        assertEquals("Heat", result.title)
        assertEquals("https://image.tmdb.org/t/p/w185/poster.jpg", result.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", result.backdropUrl)
        assertEquals(genres, result.genres)
        assertEquals(123.4, result.popularity, 0.0)
        assertEquals("1995-12-15", result.releaseDate)
    }

    @Test
    fun `genre dto maps to domain genre`() {
        val result = TmdbGenreDto(id = 35, name = "Comedy").toDomain()

        assertEquals(Genre(id = 35, name = "Comedy"), result)
    }

    @Test
    fun `movie detail dto maps nested genres and metadata`() {
        val dto = TmdbMovieDetailDto(
            id = 20,
            title = "Inception",
            tagline = "Your mind is the scene of the crime.",
            overview = "A thief steals corporate secrets through dream-sharing technology.",
            genres = listOf(
                TmdbGenreDto(id = 878, name = "Science Fiction"),
                TmdbGenreDto(id = 28, name = "Action"),
            ),
            voteAverage = 8.4,
            voteCount = 36000,
            budget = 160_000_000,
            revenue = 839_000_000,
            status = "Released",
            imdbId = "tt1375666",
            runtimeMinutes = 148,
            releaseDate = "2010-07-15",
        )

        val result = dto.toDomain(
            posterUrl = "poster-url",
            backdropUrl = "backdrop-url",
        )

        assertEquals(20L, result.id)
        assertEquals("Inception", result.title)
        assertEquals("Your mind is the scene of the crime.", result.tagline)
        assertEquals("A thief steals corporate secrets through dream-sharing technology.", result.overview)
        assertEquals("poster-url", result.posterUrl)
        assertEquals("backdrop-url", result.backdropUrl)
        assertEquals(listOf(Genre(878, "Science Fiction"), Genre(28, "Action")), result.genres)
        assertEquals(8.4, result.voteAverage, 0.0)
        assertEquals(36000, result.voteCount)
        assertEquals(160_000_000, result.budget)
        assertEquals(839_000_000, result.revenue)
        assertEquals("Released", result.status)
        assertEquals("tt1375666", result.imdbId)
        assertEquals(148, result.runtimeMinutes)
        assertEquals("2010-07-15", result.releaseDate)
    }
}
