package com.kampplus.hava


import com.google.gson.annotations.SerializedName

// Arama sonucu yoksa API "results" alanını hiç göndermez, o yüzden nullable.
data class CitySearchResponse(
    val results: List<City>?
)

data class City(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("country_code")
    val countryCode: String?,
    val country: String?,
    val admin1: String?
) {
    // "Ankara, Türkiye"
    val subtitle: String
        get() = listOfNotNull(admin1, country).distinct().joinToString(", ")
}