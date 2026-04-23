package com.amro.data.di

import com.amro.data.image.TmdbImageUrlBuilder
import com.amro.data.repository.MovieRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.amro.data.network.tmdb.TmdbApi
import com.amro.domain.repository.MovieRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideMovieRepository(
        api: TmdbApi,
        imageUrlBuilder: TmdbImageUrlBuilder,
    ): MovieRepository =
        MovieRepositoryImpl(api, imageUrlBuilder)
}

