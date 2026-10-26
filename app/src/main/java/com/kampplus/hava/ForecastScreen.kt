package com.kampplus.hava

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.runtime.remember

// ViewModel'i alır, state'i toplar.
@Composable
fun ForecastRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ForecastViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ForecastScreen(
        cityName = viewModel.cityName,
        subtitle = viewModel.subtitle,
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    cityName: String,
    subtitle: String,
    uiState: LoadState<Forecast>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(cityName.ifEmpty { "Hava Tahmini" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                LoadState.Loading -> CircularProgressIndicator()
                LoadState.Empty -> Text("Bu şehir için tahmin bulunamadı.")
                is LoadState.Error -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(uiState.message)
                    Button(onClick = onRetry) { Text("Tekrar dene") }
                }
                is LoadState.Success -> ForecastContent(subtitle, uiState.data)
            }
        }
    }
}

@Composable
private fun ForecastContent(subtitle: String, forecast: Forecast) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CurrentWeatherHeader(subtitle, forecast.current, Modifier.padding(horizontal = 16.dp))
        SectionTitle("Saatlik")
        HourlyRow(forecast.hourly)
        SectionTitle("7 Günlük")
        OutfitCard(
            advice = remember(forecast) { OutfitAdvisor.advise(forecast) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Card(modifier = Modifier.padding(horizontal = 16.dp)) {
            forecast.daily.forEachIndexed { index, day ->
                if (index > 0) HorizontalDivider()
                DailyRow(day, isToday = index == 0)
            }
        }
    }
}

@Composable
private fun CurrentWeatherHeader(subtitle: String, current: CurrentWeather, modifier: Modifier = Modifier) {
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(condition.emoji, style = MaterialTheme.typography.displayMedium)
            Column {
                Text(degrees(current.temperatureC), style = MaterialTheme.typography.displaySmall)
                Text(condition.label, style = MaterialTheme.typography.titleMedium)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                current.apparentTemperatureC?.let { Metric("Hissedilen", degrees(it)) }
                current.humidityPercent?.let { Metric("Nem", "%$it") }
                current.windSpeedKmh?.let { Metric("Rüzgâr", "${it.roundToInt()} km/sa") }
            }
        }
    }
}

@Composable
private fun HourlyRow(hours: List<HourlyForecast>) {
    val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
    val upcoming = hours.filter { !it.time.isBefore(now) }.take(24)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = upcoming, key = { it.time.toString() }) { hour ->
            Card(modifier = Modifier.width(76.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(hour.time.format(HOUR_FORMAT), style = MaterialTheme.typography.labelMedium)
                    Text(
                        WeatherCondition.fromWeatherCode(hour.weatherCode).emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(degrees(hour.temperatureC), style = MaterialTheme.typography.titleMedium)
                    Text(
                        percent(hour.precipitationProbability) ?: " ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRow(day: DailyForecast, isToday: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) "Bugün" else day.date.format(DAY_FORMAT).replaceFirstChar(Char::titlecase),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            percent(day.precipitationProbability).orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
        Text(
            WeatherCondition.fromWeatherCode(day.weatherCode).emoji,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Text(
            degrees(day.minTemperatureC),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
        Text(
            degrees(day.maxTemperatureC),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
}
@Composable
private fun OutfitCard(advice: OutfitAdvice, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Bugün ne giysem?", style = MaterialTheme.typography.labelLarge)
            Text(advice.title, style = MaterialTheme.typography.titleLarge)
            advice.items.forEach { item ->
                Text(item, style = MaterialTheme.typography.bodyLarge)
            }
            advice.tip?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun degrees(celsius: Double) = "${celsius.roundToInt()}°"

private fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }

private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("tr"))