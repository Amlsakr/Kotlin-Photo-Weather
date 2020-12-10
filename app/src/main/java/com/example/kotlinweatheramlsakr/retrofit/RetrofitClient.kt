package com.example.kotlinweatheramlsakr.retrofit

import com.example.kotlinweatheramlsakr.weatherResponseModel.Response
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {

     val API_KEY = "2693f4dd7922bd3018b5e9213229e726"
     val UNITS = "metric"
     val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"

    var gson = GsonBuilder()
        .setLenient()
        .create()
     var weatherApi  = Retrofit.Builder()
                              .baseUrl(WEATHER_BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .build()
                                .create(WeatherApi::class.java)




    fun getCurrentWeatherCondition(latitude: Double, longitude: Double): Call<Response> {
        return weatherApi.getWeatherCondition(latitude, longitude, API_KEY, UNITS)
    }
}