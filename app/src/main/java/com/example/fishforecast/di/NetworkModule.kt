package com.example.fishforecast.di

import com.example.fishforecast.data.remote.FishCatalogApi
import com.example.fishforecast.data.remote.WeatherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // Файлы карт весят сотни мегабайт, поэтому чтение не ограничено таймаутом.
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    }

    /**
     * Справочник видов приходит строкой: его разбирает и проверяет тот же
     * код, что читает файл из ассетов, — формат один, значит и разбор
     * должен быть один.
     */
    @Provides
    @Singleton
    fun provideFishCatalogApi(okHttpClient: OkHttpClient): FishCatalogApi {
        return Retrofit.Builder()
            // Адрес задаёт рыболов, базовый нужен только формально.
            .baseUrl(WeatherApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(FishCatalogApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherApi(okHttpClient: OkHttpClient): WeatherApi {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .baseUrl(WeatherApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(WeatherApi::class.java)
    }
}
