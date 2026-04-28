package com.amro.core.links

object ExternalUrls {
    const val IMDB_TITLE_URL = "https://www.imdb.com/title/"

    fun imdbTitleUrl(imdbId: String): String = "$IMDB_TITLE_URL$imdbId/"
}
