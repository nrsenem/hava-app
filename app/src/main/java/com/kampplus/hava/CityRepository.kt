package com.kampplus.hava

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CityRepository(private val api: CityApi = ApiClient.cityApi) {

    suspend fun search(query: String): List<City> = withContext(Dispatchers.IO) {
        // Sonuç yoksa "results" gelmez; orEmpty() bunu boş listeye çevirir.
        api.search(query.trim()).results.orEmpty()
    }
}