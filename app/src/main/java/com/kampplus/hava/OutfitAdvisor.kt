package com.kampplus.hava

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/** Ekranda gösterilecek giyim önerisi. */
data class OutfitAdvice(
    val title: String,       // "Serin bir gün"
    val items: List<String>, // "🧥 Hafif ceket"
    val tip: String? = null  // isteğe bağlı küçük not
)

object OutfitAdvisor {

    private val RAIN_CONDITIONS = setOf(
        WeatherCondition.DRIZZLE,
        WeatherCondition.RAIN,
        WeatherCondition.RAIN_SHOWERS,
        WeatherCondition.THUNDERSTORM
    )
    private val SNOW_CONDITIONS = setOf(
        WeatherCondition.SNOW,
        WeatherCondition.SNOW_SHOWERS
    )

    fun advise(forecast: Forecast, now: LocalDateTime = LocalDateTime.now()): OutfitAdvice {
        val current = forecast.current
        // Hissedilen sıcaklık gelmediyse gerçek sıcaklığa düş.
        val feels = current.apparentTemperatureC ?: current.temperatureC
        val condition = WeatherCondition.fromWeatherCode(current.weatherCode)

        // Önümüzdeki 12 saat
        val hourNow = now.truncatedTo(ChronoUnit.HOURS)
        val next12 = forecast.hourly.filter { !it.time.isBefore(hourNow) }.take(12)

        val rainExpected = condition in RAIN_CONDITIONS || next12.any {
            (it.precipitationProbability ?: 0) >= 50 ||
                    WeatherCondition.fromWeatherCode(it.weatherCode) in RAIN_CONDITIONS
        }
        val snowExpected = condition in SNOW_CONDITIONS || next12.any {
            WeatherCondition.fromWeatherCode(it.weatherCode) in SNOW_CONDITIONS
        }
        val windy = (current.windSpeedKmh ?: 0.0) >= 30.0
        val sunny = condition == WeatherCondition.CLEAR || condition == WeatherCondition.MAINLY_CLEAR

        // 1) Sıcaklığa göre ana kıyafet
        val (title, base) = when {
            feels < 0 -> "Çok soğuk" to listOf("🧥 Kalın mont", "🧣 Atkı", "🧤 Eldiven", "🧢 Bere")
            feels < 10 -> "Soğuk" to listOf("🧥 Mont", "🧣 Atkı ve bere", "👖 Uzun pantolon")
            feels < 17 -> "Serin" to listOf("🧥 Hafif mont ya da ceket", "👕 Uzun kollu", "👖 Pantolon")
            feels < 24 -> "Ilıman" to listOf("👕 Tişört", "🧥 Yanına ince hırka", "👖 Pantolon")
            feels < 30 -> "Sıcak" to listOf("👕 Tişört", "🩳 Şort ya da ince pantolon")
            else -> "Çok sıcak" to listOf("👕 İnce, açık renk tişört", "🩳 Şort", "💧 Yanına su al")
        }

        // 2) Hava durumuna göre eklemeler
        val extras = buildList {
            if (rainExpected) {
                add("☂️ Şemsiye")
                add("🧥 Su geçirmez ceket")
            }
            if (snowExpected) add("🥾 Su geçirmez bot")
            if (windy) add("🌬️ Rüzgârlık")
            if (sunny && feels >= 22) {
                add("🕶️ Güneş gözlüğü")
                add("🧴 Güneş kremi")
            }
        }

        val tip = when {
            rainExpected && snowExpected -> "Yağış var, ıslanmamaya dikkat et."
            rainExpected -> "Önümüzdeki saatlerde yağmur bekleniyor."
            snowExpected -> "Kar bekleniyor, kaymayan ayakkabı giy."
            windy -> "Rüzgâr kuvvetli; hissedilen sıcaklık daha düşük olabilir."
            else -> null
        }

        return OutfitAdvice(title = "$title bir gün", items = base + extras, tip = tip)
    }
}