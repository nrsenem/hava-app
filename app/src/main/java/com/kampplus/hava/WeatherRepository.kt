package com.kampplus.hava

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val api: ForecastApi = ApiClient.forecastApi) {

    suspend fun getForecast(latitude: Double, longitude: Double): Forecast =
        withContext(Dispatchers.IO) {
            api.getForecast(latitude = latitude, longitude = longitude).toForecast()
        }
}