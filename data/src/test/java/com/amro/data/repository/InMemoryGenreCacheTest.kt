package com.amro.data.repository

import com.amro.domain.model.Genre
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryGenreCacheTest {

    @Test
    fun `get returns null before put`() = runTest {
        val cache = InMemoryGenreCache()

        val result = cache.get(language = "en")

        assertNull(result)
    }

    @Test
    fun `put stores genres by language`() = runTest {
        val cache = InMemoryGenreCache()
        val englishGenres = listOf(Genre(id = 28, name = "Action"))
        val dutchGenres = listOf(Genre(id = 28, name = "Actie"))

        cache.put(language = "en", genres = englishGenres)
        cache.put(language = "nl", genres = dutchGenres)

        assertEquals(englishGenres, cache.get(language = "en"))
        assertEquals(dutchGenres, cache.get(language = "nl"))
    }
}
