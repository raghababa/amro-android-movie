package com.amro.data.repository

import com.amro.domain.model.Genre
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

interface GenreCache {
    suspend fun get(language: String): List<Genre>?

    suspend fun put(language: String, genres: List<Genre>)
}

internal class InMemoryGenreCache @Inject constructor() : GenreCache {
    private val mutex = Mutex()
    private var genresByLanguage: Map<String, List<Genre>> = emptyMap()

    override suspend fun get(language: String): List<Genre>? =
        mutex.withLock { genresByLanguage[language] }

    override suspend fun put(language: String, genres: List<Genre>) {
        mutex.withLock {
            genresByLanguage = genresByLanguage + (language to genres)
        }
    }
}
