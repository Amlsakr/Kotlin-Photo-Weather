package com.example.kotlinweatheramlsakr.viewModel

import androidx.lifecycle.ViewModel
import com.example.kotlinweatheramlsakr.retrofit.RetrofitClient
import com.example.kotlinweatheramlsakr.weatherResponseModel.Response
import retrofit2.Call

class MainViewModel : ViewModel() {


    var retrofitClient: RetrofitClient = RetrofitClient

    fun getData(latitude: Double, longitude: Double): Call<Response> {
        return retrofitClient.getCurrentWeatherCondition(latitude, longitude)
    }
}