package com.amro.movie_detail.ui.testtags

object MovieDetailTestTags {
    private const val PREFIX = "movie_detail"

    const val SCREEN = "${PREFIX}_screen"
    const val LOADING = "${PREFIX}_loading"
    const val ERROR = "${PREFIX}_error"
    const val CONTENT = "${PREFIX}_content"
    const val LIST = "${PREFIX}_list"

    const val BACK_BUTTON = "${PREFIX}_back_button"
    const val HEADER_SECTION = "${PREFIX}_header_section"
    const val INFO_SECTION = "${PREFIX}_info_section"
    const val STATS_SECTION = "${PREFIX}_stats_section"
    const val POSTER_PLACEHOLDER = "${PREFIX}_poster_placeholder"
    const val OPEN_IMDB_BUTTON = "${PREFIX}_open_imdb_button"

    fun genreTag(index: Int): String = "${PREFIX}_genre_tag_$index"
}
