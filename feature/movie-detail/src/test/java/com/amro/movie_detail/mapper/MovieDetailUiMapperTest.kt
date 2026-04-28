package com.amro.movie_detail.mapper

import com.amro.core.links.ExternalUrls
import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class MovieDetailUiMapperTest {

    private val mapper = MovieDetailUiMapper(
        currencyFormatter = CurrencyFormatter(),
        dateFormatter = DateFormatter(localeProvider = { Locale.US }),
    )

    @Test
    fun `maps detail fields and formats display values`() {
        val result = mapper.toUi(movieDetail(
            budget = 160_000_000,
            revenue = 839_000_000,
            imdbId = "tt1375666",
            runtimeMinutes = 148,
        ))

        assertEquals(1L, result.id)
        assertEquals("Inception", result.title)
        assertEquals("Your mind is the scene of the crime.", result.tagline)
        assertEquals(listOf("Science Fiction", "Action"), result.genres)
        assertEquals("$160M", result.budget)
        assertEquals("$839M", result.revenue)
        assertEquals(ExternalUrls.imdbTitleUrl("tt1375666"), result.imdbUrl)
        assertEquals(148, result.runtimeMinutes)
        assertEquals("Jul 15, 2010", result.releaseDate)
    }

    @Test
    fun `maps blank and non-positive optional values to null`() {
        val result = mapper.toUi(movieDetail(
            tagline = " ",
            overview = "",
            budget = 0,
            revenue = -1,
            status = " ",
            imdbId = "",
            runtimeMinutes = 0,
            releaseDate = " ",
        ))

        assertNull(result.tagline)
        assertNull(result.overview)
        assertNull(result.budget)
        assertNull(result.revenue)
        assertNull(result.status)
        assertNull(result.imdbUrl)
        assertNull(result.runtimeMinutes)
        assertNull(result.releaseDate)
    }

    @Test
    fun `removes blank genre names`() {
        val result = mapper.toUi(movieDetail(
            genres = listOf(Genre(878, "Science Fiction"), Genre(0, " "))
        ))

        assertEquals(listOf("Science Fiction"), result.genres)
    }

    @Test
    fun `keeps invalid non blank release date unchanged`() {
        val result = mapper.toUi(movieDetail(releaseDate = "coming soon"))

        assertEquals("coming soon", result.releaseDate)
    }

    @Test
    fun `maps null imdb id to null`() {
        val result = mapper.toUi(movieDetail(imdbId = null))

        assertNull(result.imdbUrl)
    }

    @Test
    fun `formats large USD as compact billions`() {
        val result = mapper.toUi(movieDetail(budget = 2_200_000_000, revenue = 3_050_000_000))

        assertEquals("$2.2B", result.budget)
        assertEquals("$3.1B", result.revenue)
    }

    @Test
    fun `formats mid tier USD as millions thousands or full amount`() {
        assertEquals("$1.2M", mapper.toUi(movieDetail(budget = 1_200_000)).budget)
        assertEquals("$999.5K", mapper.toUi(movieDetail(budget = 999_500)).budget)
        assertEquals("$50,500", mapper.toUi(movieDetail(budget = 50_500)).budget)
    }

    @Test
    fun `formats USD money with US locale regardless of date locale`() {
        val mapper = MovieDetailUiMapper(
            currencyFormatter = CurrencyFormatter(),
            dateFormatter = DateFormatter(localeProvider = { Locale.forLanguageTag("nl-NL") }),
        )

        val result = mapper.toUi(movieDetail(budget = 100, releaseDate = "2010-07-15"))

        assertEquals("$100", result.budget)
        assertEquals("jul 15, 2010", result.releaseDate)
    }

    private fun movieDetail(
        tagline: String? = "Your mind is the scene of the crime.",
        overview: String? = "A thief steals corporate secrets through dream-sharing technology.",
        genres: List<Genre> = listOf(Genre(878, "Science Fiction"), Genre(28, "Action")),
        budget: Long = 0,
        revenue: Long = 0,
        status: String? = "Released",
        imdbId: String? = null,
        runtimeMinutes: Int? = null,
        releaseDate: String? = "2010-07-15",
    ): MovieDetail =
        MovieDetail(
            id = 1,
            title = "Inception",
            tagline = tagline,
            overview = overview,
            posterUrl = "poster",
            backdropUrl = "backdrop",
            genres = genres,
            voteAverage = 8.4,
            voteCount = 36000,
            budget = budget,
            revenue = revenue,
            status = status,
            imdbId = imdbId,
            runtimeMinutes = runtimeMinutes,
            releaseDate = releaseDate,
        )
}
