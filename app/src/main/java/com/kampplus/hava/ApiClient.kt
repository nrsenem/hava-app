package com.kampplus.hava

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    val cityApi: CityApi by lazy { createApi("https://geocoding-api.open-meteo.com/") }

    val forecastApi: ForecastApi by lazy { createApi("https://api.open-meteo.com/") }

    private inline fun <reified T> createApi(baseUrl: String): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(T::class.java)
}