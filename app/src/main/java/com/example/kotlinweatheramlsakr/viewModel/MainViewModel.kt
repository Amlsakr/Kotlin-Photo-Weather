package com.example.kotlinweatheramlsakr.viewModel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kotlinweatheramlsakr.data.storage.ReadImage
import com.example.kotlinweatheramlsakr.data.storage.WriteImage
import com.example.kotlinweatheramlsakr.retrofit.RetrofitClient
import com.example.kotlinweatheramlsakr.data.weatherResponseModel.Response
import retrofit2.Call
import retrofit2.Callback

class MainViewModel (application: Application): AndroidViewModel(application) {

    private val _images = MutableLiveData<List<Uri>>()
    val images : LiveData<List<Uri>> get() = _images

    private val _weatherData = MutableLiveData<Response>()
    val weatherData :LiveData<Response> get() = _weatherData

    var writeImage = WriteImage(getApplication())


    fun loadImages (){
        val imageList = ReadImage.getImages(getApplication())
        _images.postValue(imageList)

    }

    fun writeTextOnDrawable(imageBitmap :Bitmap, placeName:String, temperature:Double, condition:String): Bitmap?{
     return   writeImage.writeTextOnDrawable(imageBitmap ,placeName, temperature, condition)
    }

    fun saveImage(bitmap:Bitmap) {
        writeImage.saveImage(bitmap)
    }



    var retrofitClient: RetrofitClient = RetrofitClient

    fun getData(latitude: Double, longitude: Double): MutableLiveData<Response> {
        val call = retrofitClient.getCurrentWeatherCondition(latitude, longitude)
        call.enqueue(object : Callback<Response> {
            override fun onResponse(
                call: Call<Response>,
                response: retrofit2.Response<Response>
            ) {
//                placeName = response.body()?.name.toString()
//                temperature = response.body()?.main?.temp!!
//                condition = response.body()?.weather?.get(0)?.description!!
              //  Log.e(TAG, placeName + temperature + condition)
                _weatherData.postValue(response.body())
            }

            override fun onFailure(call: Call<Response>, t: Throwable) {
           //     Log.e(TAG, t.message.toString())
                t.printStackTrace()
            }

        })
        return _weatherData
    }
}