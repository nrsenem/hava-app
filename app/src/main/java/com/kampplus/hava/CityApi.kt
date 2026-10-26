package com.kampplus.hava

import retrofit2.http.GET
import retrofit2.http.Query

interface CityApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "tr"
    ): CitySearchResponse
}