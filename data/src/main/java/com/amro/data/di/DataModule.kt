package com.amro.data.di

import com.amro.data.BuildConfig
import com.amro.data.image.TmdbImageBaseUrl
import com.amro.data.remote.TmdbRemoteDataSource
import com.amro.data.remote.TmdbRemoteDataSourceImpl
import com.amro.data.repository.DefaultGenreLanguageResolver
import com.amro.data.repository.GenreCache
import com.amro.data.repository.GenreLanguageResolver
import com.amro.data.repository.InMemoryGenreCache
import com.amro.data.repository.MovieRepositoryImpl
import com.amro.data.repository.TrendingMoviesConfig
import com.amro.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTmdbRemoteDataSource(
        impl: TmdbRemoteDataSourceImpl,
    ): TmdbRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindMovieRepository(
        impl: MovieRepositoryImpl,
    ): MovieRepository

    @Binds
    @Singleton
    abstract fun bindGenreCache(
        impl: InMemoryGenreCache,
    ): GenreCache

    @Binds
    @Singleton
    abstract fun bindGenreLanguageResolver(
        impl: DefaultGenreLanguageResolver,
    ): GenreLanguageResolver
}

@Module
@InstallIn(SingletonComponent::class)
internal object DataProvidersModule {

    @Provides
    @Singleton
    @TmdbImageBaseUrl
    fun provideTmdbImageBaseUrl(): String = BuildConfig.TMDB_IMAGE_BASE_URL

    @Provides
    @Singleton
    fun provideTrendingMoviesConfig(): TrendingMoviesConfig = TrendingMoviesConfig()
}

