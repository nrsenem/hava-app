package com.kampplus.hava

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private const val CITY_ROUTE = "city"
private const val FORECAST_ROUTE =
    "forecast?name={name}&subtitle={subtitle}&latitude={latitude}&longitude={longitude}"

@Composable
fun HavaNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = CITY_ROUTE) {

        composable(CITY_ROUTE) {
            Scaffold { innerPadding ->
                CityRoute(
                    modifier = Modifier.padding(innerPadding),
                    onCityClick = { city -> navController.navigate(forecastRoute(city)) }
                )
            }
        }

        composable(
            route = FORECAST_ROUTE,
            arguments = listOf(
                navArgument(ForecastViewModel.ARG_NAME) { type = NavType.StringType; defaultValue = "" },
                navArgument(ForecastViewModel.ARG_SUBTITLE) { type = NavType.StringType; defaultValue = "" },
                navArgument(ForecastViewModel.ARG_LATITUDE) { type = NavType.StringType; defaultValue = "0" },
                navArgument(ForecastViewModel.ARG_LONGITUDE) { type = NavType.StringType; defaultValue = "0" }
            )
        ) {
            ForecastRoute(onBack = { navController.popBackStack() })
        }
    }
}

// Türkçe karakter ve boşluklar adreste bozulmasın diye Uri.encode.
private fun forecastRoute(city: City): String = buildString {
    append("forecast")
    append("?name=").append(Uri.encode(city.name))
    append("&subtitle=").append(Uri.encode(city.subtitle))
    append("&latitude=").append(city.latitude)
    append("&longitude=").append(city.longitude)
}