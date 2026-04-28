package com.amro.data.repository

import com.amro.domain.repository.LanguageCode
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultGenreLanguageResolverTest {

    private val resolver = DefaultGenreLanguageResolver()

    @Test
    fun `resolve uses language code before region`() {
        val result = resolver.resolve(LanguageCode("nl-NL"))

        assertEquals("nl", result)
    }

    @Test
    fun `resolve keeps two letter language code`() {
        val result = resolver.resolve(LanguageCode.EN)

        assertEquals("en", result)
    }
}
