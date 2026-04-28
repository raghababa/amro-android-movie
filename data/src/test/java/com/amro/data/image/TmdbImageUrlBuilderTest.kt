package com.amro.data.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TmdbImageUrlBuilderTest {

    private val builder = TmdbImageUrlBuilder(baseUrl = "https://image.tmdb.org/t/p/")

    @Test
    fun `buildPosterUrl returns null for null or blank`() {
        assertNull(builder.buildPosterUrl(null, TmdbImageUrlBuilder.ImageSize.W500))
        assertNull(builder.buildPosterUrl("", TmdbImageUrlBuilder.ImageSize.W500))
        assertNull(builder.buildPosterUrl("   ", TmdbImageUrlBuilder.ImageSize.W500))
    }

    @Test
    fun `buildPosterUrl normalizes leading slash and avoids duplicates`() {
        val url1 = builder.buildPosterUrl("abc.jpg", TmdbImageUrlBuilder.ImageSize.W500)
        val url2 = builder.buildPosterUrl("/abc.jpg", TmdbImageUrlBuilder.ImageSize.W500)
        assertEquals("https://image.tmdb.org/t/p/w500/abc.jpg", url1)
        assertEquals(url1, url2)
    }

    @Test
    fun `posterSmall uses w185`() {
        val url = builder.posterSmall("/abc.jpg")
        assertEquals("https://image.tmdb.org/t/p/w185/abc.jpg", url)
    }

    @Test
    fun `posterMedium uses w500`() {
        val url = builder.posterMedium("/abc.jpg")
        assertEquals("https://image.tmdb.org/t/p/w500/abc.jpg", url)
    }

    @Test
    fun `posterLarge uses w780`() {
        val url = builder.posterLarge("/abc.jpg")
        assertEquals("https://image.tmdb.org/t/p/w780/abc.jpg", url)
    }

    @Test
    fun `buildBackdropUrl uses requested size`() {
        val url = builder.buildBackdropUrl("/backdrop.jpg", TmdbImageUrlBuilder.ImageSize.W780)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", url)
    }

    @Test
    fun `buildLogoUrl uses requested size`() {
        val url = builder.buildLogoUrl("/logo.png", TmdbImageUrlBuilder.ImageSize.W154)
        assertEquals("https://image.tmdb.org/t/p/w154/logo.png", url)
    }
}

