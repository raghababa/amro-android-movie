package com.amro.data.network.tmdb

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TmdbBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TmdbBearerToken
