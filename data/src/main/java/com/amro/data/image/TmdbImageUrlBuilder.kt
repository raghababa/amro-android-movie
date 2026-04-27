package com.amro.data.image

import javax.inject.Inject

/**
 * Builds TMDB image URLs.
 *
 * ## How TMDB image URLs work
 * TMDB returns only a `file_path` (e.g. `/abc123.jpg`) in most responses.
 * To load the actual image, clients must build a URL using:
 *
 * \[
 *   base_url + size + file_path
 * \]
 *
 * Example:
 * `https://image.tmdb.org/t/p/w500/abc123.jpg`
 *
 * ## Why sizes matter
 * Different screens need different image sizes:
 * - Lists use smaller thumbnails to save bandwidth and improve scrolling performance.
 * - Detail screens use larger posters for better quality.
 *
 * The base URL is injected from build configuration for this assignment. A production app
 * could fetch it from TMDB's `/configuration` endpoint and cache the image base URL plus
 * supported sizes.
 */
class TmdbImageUrlBuilder @Inject constructor(
    @param:TmdbImageBaseUrl private val baseUrl: String,
) {
    /**
     * Strongly typed TMDB image sizes.
     */
    enum class ImageSize(val tmdbValue: String) {
        W92("w92"),
        W154("w154"),
        W185("w185"),
        W342("w342"),
        W500("w500"),
        W780("w780"),
        ORIGINAL("original"),
    }

    fun buildPosterUrl(path: String?, size: ImageSize): String? = buildUrl(path = path, size = size)

    fun buildBackdropUrl(path: String?, size: ImageSize): String? = buildUrl(path = path, size = size)

    fun buildLogoUrl(path: String?, size: ImageSize): String? = buildUrl(path = path, size = size)

    /** Convenience for list thumbnails. */
    fun posterSmall(path: String?): String? = buildPosterUrl(path, ImageSize.W185)

    /** Convenience for detail screens. */
    fun posterMedium(path: String?): String? = buildPosterUrl(path, ImageSize.W500)

    /** Optional convenience for large poster usage. */
    fun posterLarge(path: String?): String? = buildPosterUrl(path, ImageSize.W780)

    private fun buildUrl(path: String?, size: ImageSize): String? {
        val raw = path?.trim().orEmpty()
        if (raw.isBlank()) return null

        return normalizeBase(baseUrl) + size.tmdbValue.trim('/') + normalizePath(raw)
    }

}

private fun normalizePath(path: String): String = "/" + path.trimStart('/')

private fun normalizeBase(url: String): String = url.trimEnd('/') + "/"

