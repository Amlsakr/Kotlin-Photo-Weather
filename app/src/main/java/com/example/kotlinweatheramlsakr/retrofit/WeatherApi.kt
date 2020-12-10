package com.example.kotlinweatheramlsakr.retrofit

import com.example.kotlinweatheramlsakr.weatherResponseModel.Response
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface WeatherApi {

    @Headers("Content-Type: application/json")
    @GET("weather")
    fun getWeatherCondition(
        @Query("lat") lat: Double, @Query("lon") lon: Double,
        @Query("appid") appid: String,
        @Query("units") units: String): Call<Response>
}