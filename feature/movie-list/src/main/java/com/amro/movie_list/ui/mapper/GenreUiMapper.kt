package com.amro.movie_list.ui.mapper

import com.amro.domain.model.Genre
import com.amro.movie_list.ui.model.GenreUi
import javax.inject.Inject

class GenreUiMapper @Inject constructor() {
    fun toUi(genre: Genre): GenreUi =
        GenreUi(
            id = genre.id,
            name = genre.name,
        )
}
