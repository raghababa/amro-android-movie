package com.amro.data.di

import com.amro.data.BuildConfig
import com.amro.data.network.security.AuthInterceptor
import com.amro.data.network.security.TokenProvider
import com.amro.data.network.tmdb.TmdbApi
import com.amro.data.network.tmdb.TmdbBaseUrl
import com.amro.data.network.tmdb.TmdbBearerToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val NETWORK_TIMEOUT_SECONDS = 15L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = false
    }

    @Provides
    @Singleton
    @TmdbBaseUrl
    fun provideTmdbBaseUrl(): String = BuildConfig.TMDB_BASE_URL

    @Provides
    @Singleton
    @TmdbBearerToken
    fun provideTmdbBearerToken(): String = BuildConfig.TMDB_BEARER_TOKEN

    @Provides
    @Singleton
    fun provideTmdbTokenProvider(
        @TmdbBearerToken token: String,
    ): TokenProvider = TokenProvider { token }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenProvider: TokenProvider,
    ): AuthInterceptor =
        AuthInterceptor(tokenProvider = tokenProvider)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
                redactHeader("Authorization")
            }
            builder.addInterceptor(logging)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
        @TmdbBaseUrl baseUrl: String,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideTmdbApi(retrofit: Retrofit): TmdbApi =
        retrofit.create(TmdbApi::class.java)
}

