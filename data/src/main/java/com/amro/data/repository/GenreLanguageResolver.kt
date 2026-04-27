package com.amro.data.repository

import com.amro.domain.repository.LanguageCode
import javax.inject.Inject

interface GenreLanguageResolver {
    fun resolve(language: LanguageCode): String
}

internal class DefaultGenreLanguageResolver @Inject constructor() : GenreLanguageResolver {
    override fun resolve(language: LanguageCode): String =
        language.value.substringBefore('-').ifBlank { DEFAULT_GENRE_LANGUAGE }
}

private const val DEFAULT_GENRE_LANGUAGE = "en"
