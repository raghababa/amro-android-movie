package com.amro.movie_detail.mapper

import com.amro.core.links.ExternalUrls
import com.amro.domain.model.Genre
import com.amro.domain.model.MovieDetail
import com.amro.movie_detail.ui.model.MovieDetailUi
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class MovieDetailUiMapper @Inject constructor(
    private val currencyFormatter: CurrencyFormatter,
    private val dateFormatter: DateFormatter,
) {
    fun toUi(detail: MovieDetail): MovieDetailUi =
        MovieDetailUi(
            id = detail.id,
            title = detail.title,
            tagline = detail.tagline.nonBlankOrNull(),
            overview = detail.overview.nonBlankOrNull(),
            posterUrl = detail.posterUrl,
            backdropUrl = detail.backdropUrl,
            genres = detail.genres.toDisplayNames(),
            voteAverage = detail.voteAverage,
            voteCount = detail.voteCount,
            budget = currencyFormatter.formatUsd(detail.budget),
            revenue = currencyFormatter.formatUsd(detail.revenue),
            status = detail.status.nonBlankOrNull(),
            imdbUrl = formatImdbUrl(detail.imdbId),
            runtimeMinutes = detail.runtimeMinutes?.takeIf { it > 0 },
            releaseDate = dateFormatter.formatReleaseDate(detail.releaseDate),
        )

    private fun formatImdbUrl(imdbId: String?): String? =
        imdbId.nonBlankOrNull()?.let(ExternalUrls::imdbTitleUrl)
}

class CurrencyFormatter @Inject constructor() {
    private val currencyFormatter: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = Currency.getInstance(USD_CURRENCY_CODE)
            // TMDB returns whole dollars; avoid noisy ".00" on large amounts.
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    fun formatUsd(value: Long): String? {
        if (value <= 0) return null

        // TMDB budget/revenue are whole USD; use short B / M / K for large amounts.
        return when {
            value >= 1_000_000_000L ->
                "${'$'}${formatCompactScaled(value / 1_000_000_000.0, integerHundreds = true)}B"
            value >= 1_000_000L ->
                "${'$'}${formatCompactScaled(value / 1_000_000.0, integerHundreds = true)}M"
            value >= 100_000L ->
                "${'$'}${formatCompactScaled(value / 1_000.0, integerHundreds = false)}K"
            else -> currencyFormatter.format(value)
        }
    }

    /**
     * Millions/billions: no cents; [100 … ∞) rounds to integer. Below 100, up to one decimal.
     * Thousands: avoid "$1000K" rounding—always trims from one decimal unless whole K.
     */
    private fun formatCompactScaled(scaled: Double, integerHundreds: Boolean): String =
        when {
            integerHundreds && scaled >= 100.0 -> String.format(Locale.US, "%.0f", scaled)
            else -> String.format(Locale.US, "%.1f", scaled).trimEnd('0').trimEnd('.')
        }
}

class DateFormatter internal constructor(
    private val localeProvider: () -> Locale,
) {
    @Inject
    constructor() : this(Locale::getDefault)

    private val locale: Locale by lazy { localeProvider() }

    private val releaseDateFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern(DISPLAY_DATE_PATTERN, locale)
    }

    fun formatReleaseDate(value: String?): String? {
        val dateText = value.nonBlankOrNull() ?: return null
        val parsedDate = runCatching {
            LocalDate.parse(dateText, DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrNull()

        return parsedDate?.format(releaseDateFormatter) ?: dateText
    }
}

private fun String?.nonBlankOrNull(): String? = this?.takeIf { it.isNotBlank() }

private fun List<Genre>.toDisplayNames(): List<String> = mapNotNull { it.name.nonBlankOrNull() }

private const val USD_CURRENCY_CODE = "USD"
private const val DISPLAY_DATE_PATTERN = "MMM d, yyyy"

