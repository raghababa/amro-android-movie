package com.amro.data.di

import com.amro.data.BuildConfig
import com.amro.data.image.TmdbImageBaseUrl
import com.amro.data.image.TmdbImageUrlBuilder
import com.amro.data.remote.TmdbRemoteDataSource
import com.amro.data.remote.TmdbRemoteDataSourceImpl
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
    @TmdbImageBaseUrl
    fun provideTmdbImageBaseUrl(): String = BuildConfig.TMDB_IMAGE_BASE_URL

    @Provides
    @Singleton
    fun provideTmdbRemoteDataSource(
        api: TmdbApi,
    ): TmdbRemoteDataSource =
        TmdbRemoteDataSourceImpl(api)

    @Provides
    @Singleton
    fun provideMovieRepository(
        remoteDataSource: TmdbRemoteDataSource,
        imageUrlBuilder: TmdbImageUrlBuilder,
    ): MovieRepository =
        MovieRepositoryImpl(remoteDataSource, imageUrlBuilder)
}

