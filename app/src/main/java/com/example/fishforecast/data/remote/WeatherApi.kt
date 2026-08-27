package com.example.fishforecast.data.remote

import com.example.fishforecast.data.remote.dto.PressureHistoryDto
import com.example.fishforecast.data.remote.dto.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    /**
     * Час в час по выбранному району.
     *
     * Давление берётся станционное (`surface_pressure`), а не приведённое к
     * уровню моря: барометр телефона и аневроид рыболова показывают именно
     * его, и норма водоёма записана в тех же цифрах. Под Москвой (152 м)
     * расхождение между ними — 18 гПа, то есть 14 мм рт. ст.
     *
     * `timezone=auto` обязателен: без него время приходит по Гринвичу, а
     * приложение сравнивает его с местными часами устройства.
     */
    @GET(
        "v1/forecast?hourly=temperature_2m,relative_humidity_2m,weather_code," +
            "surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m," +
            "precipitation,precipitation_probability,cloud_cover,shortwave_radiation" +
            "&daily=sunrise,sunset&timezone=auto"
    )
    suspend fun getWeatherData(
        @Query("latitude") lat: Double,
        @Query("longitude") long: Double,
        /**
         * Прошедшие сутки нужны не для истории ради истории: похолодание
         * после жары видно только в сравнении с тем, что было, а вода
         * помнит погоду дольше, чем длится прогноз вперёд.
         */
        @Query("past_days") pastDays: Int = PAST_DAYS
    ): WeatherDto

    /**
     * Только давление, но на всю доступную историю.
     *
     * Норма водоёма — это его многолетнее среднее, и ждать её от рыболова
     * незачем: он вводил цифру со своего барометра, а её можно посчитать.
     * Модель хранит около семидесяти суток; дальний край приходит пустым,
     * и это учтено при усреднении.
     */
    @GET("v1/forecast?hourly=surface_pressure&forecast_days=1&timezone=auto")
    suspend fun getPressureHistory(
        @Query("latitude") lat: Double,
        @Query("longitude") long: Double,
        @Query("past_days") pastDays: Int = HISTORY_DAYS
    ): PressureHistoryDto

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"

        /**
         * Столько суток назад запрашивается вместе с прогнозом. Инерционная
         * модель воды разгоняется примерно за неделю, дальше начальная
         * ошибка уже не заметна.
         */
        const val PAST_DAYS = 7

        /** Максимум, который принимает Open-Meteo. */
        const val HISTORY_DAYS = 92
    }
}
