package com.amro.movie_list.ui.testtags

import com.amro.domain.model.MovieSortField
import com.amro.domain.model.SortOrder

object MovieListTestTags {
    private const val PREFIX = "movie_list"

    const val SCREEN = "${PREFIX}_screen"
    const val LOADING = "${PREFIX}_loading"
    const val ERROR = "${PREFIX}_error"
    const val CONTENT = "${PREFIX}_content"
    const val EMPTY = "${PREFIX}_empty"
    const val LIST = "${PREFIX}_list"
    const val GENRE_ERROR = "${PREFIX}_genre_error"

    const val GENRE_CHIP_ALL = "${PREFIX}_genre_chip_all"
    const val CLEAR_FILTER_BUTTON = "${PREFIX}_clear_filter_button"

    fun genreChip(genreId: Int): String = "${PREFIX}_genre_chip_$genreId"

    fun movieItem(movieId: Long): String = "${PREFIX}_movie_item_$movieId"

    fun sortField(field: MovieSortField): String = "${PREFIX}_sort_field_${field.name}"

    fun sortOrder(order: SortOrder): String = "${PREFIX}_sort_order_${order.name}"
}
