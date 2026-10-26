package com.kampplus.hava

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime

data class ForecastResponse(
    val current: CurrentResponse? = null,
    val hourly: HourlyResponse? = null,
    val daily: DailyResponse? = null
)

data class CurrentResponse(
    val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerializedName("relative_humidity_2m") val relativeHumidity: Int? = null,
    @SerializedName("wind_speed_10m") val windSpeed: Double? = null
)

data class HourlyResponse(
    val time: List<String>? = null,
    @SerializedName("temperature_2m") val temperature: List<Double?>? = null,
    @SerializedName("weather_code") val weatherCode: List<Int?>? = null,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int?>? = null
)

data class DailyResponse(
    val time: List<String>? = null,
    @SerializedName("weather_code") val weatherCode: List<Int?>? = null,
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double?>? = null,
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double?>? = null,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int?>? = null
)

fun ForecastResponse.toForecast(): Forecast {
    val c = current ?: throw IllegalStateException("Yanıtta current bloğu yok")
    return Forecast(
        current = CurrentWeather(
            temperatureC = c.temperature,
            weatherCode = c.weatherCode,
            observedAt = LocalDateTime.parse(c.time),
            apparentTemperatureC = c.apparentTemperature,
            humidityPercent = c.relativeHumidity,
            windSpeedKmh = c.windSpeed
        ),
        hourly = hourly.toHourlyForecasts(),
        daily = daily.toDailyForecasts()
    )
}

// Aynı index aynı saate aittir: times[i], temperature[i], weatherCode[i]...
private fun HourlyResponse?.toHourlyForecasts(): List<HourlyForecast> {
    val times = this?.time ?: return emptyList()
    return times.indices.mapNotNull { i ->
        val temp = temperature?.getOrNull(i) ?: return@mapNotNull null
        val code = weatherCode?.getOrNull(i) ?: return@mapNotNull null
        HourlyForecast(
            time = LocalDateTime.parse(times[i]),
            temperatureC = temp,
            weatherCode = code,
            precipitationProbability = precipitationProbability?.getOrNull(i)
        )
    }
}

private fun DailyResponse?.toDailyForecasts(): List<DailyForecast> {
    val times = this?.time ?: return emptyList()
    return times.indices.mapNotNull { i ->
        val max = temperatureMax?.getOrNull(i) ?: return@mapNotNull null
        val min = temperatureMin?.getOrNull(i) ?: return@mapNotNull null
        val code = weatherCode?.getOrNull(i) ?: return@mapNotNull null
        DailyForecast(
            date = LocalDate.parse(times[i]),
            minTemperatureC = min,
            maxTemperatureC = max,
            weatherCode = code,
            precipitationProbability = precipitationProbabilityMax?.getOrNull(i)
        )
    }
}