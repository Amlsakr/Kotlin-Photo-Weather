package com.example.kotlinweatheramlsakr.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinweatheramlsakr.data.storage.ReadImage
import com.example.kotlinweatheramlsakr.retrofit.RetrofitClient
import com.example.kotlinweatheramlsakr.weatherResponseModel.Response
import retrofit2.Call

class MainViewModel (application: Application): AndroidViewModel(application) {

    private val _images = MutableLiveData<List<Uri>>()
    val images : LiveData<List<Uri>> get() = _images

    fun loadImages (){
        val imageList = ReadImage.getImages(getApplication())
        _images.postValue(imageList)

    }




    var retrofitClient: RetrofitClient = RetrofitClient

    fun getData(latitude: Double, longitude: Double): Call<Response> {
        return retrofitClient.getCurrentWeatherCondition(latitude, longitude)
    }
}