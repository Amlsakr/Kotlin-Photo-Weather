package com.example.kotlinweatheramlsakr

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kotlinweatheramlsakr.data.PermissionHandler
import com.example.kotlinweatheramlsakr.data.PermissionHandler.Companion.REQUEST_PERMISSIONS_LOCATION_CODE
import com.example.kotlinweatheramlsakr.data.PermissionHandler.Companion.REQUEST_PERMISSIONS_READ_STORAGE_CODE
import com.example.kotlinweatheramlsakr.data.PermissionHandler.Companion.REQUEST_PERMISSIONS_WRITE_STORAGE_CODE
import com.example.kotlinweatheramlsakr.data.storage.ReadImage.Companion.getImages
import com.example.kotlinweatheramlsakr.data.storage.WriteImage
import com.example.kotlinweatheramlsakr.databinding.ActivityMainBinding
import com.example.kotlinweatheramlsakr.view.DetailsActivity
import com.example.kotlinweatheramlsakr.view.adapter.MainAdapter
import com.example.kotlinweatheramlsakr.view.adapter.RecyclerViewItemClickListener
import com.example.kotlinweatheramlsakr.viewModel.MainViewModel
import com.example.kotlinweatheramlsakr.weatherResponseModel.Response
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import java.util.*

class MainActivity : AppCompatActivity(), RecyclerViewItemClickListener {

    val REQUEST_IMAGE_CAPTURE = 1
    private val TAG = MainActivity::class.java.simpleName
    protected lateinit var mLastLocation: Location
   private lateinit var binding: ActivityMainBinding
    var imageUries: List<Uri> = ArrayList()
  private lateinit var locationRequest: LocationRequest
    private lateinit var mainViewModel: MainViewModel
    private var placeName: String = ""
    private var temperature = 0.0
    private var condition: String = ""
    private lateinit var  mainAdapter: MainAdapter
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var writeImage: WriteImage
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var latitude = 0.0
    private var longitude = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        initObjects()
        if (!permissionHandler!!.checkLocationPermission()) {
            permissionHandler.requestLocationPermission()
        } else {
            getLastLocation()
        }
    }

    private fun initObjects() {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        permissionHandler = PermissionHandler(this)
        writeImage = WriteImage(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    mLastLocation = location
                    latitude = mLastLocation.latitude
                    longitude = mLastLocation.longitude
                }
            }
        }

        locationRequest = LocationRequest.create()
        locationRequest.setInterval(10000)
        locationRequest.setFastestInterval(5000)
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient.lastLocation
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful && task.result != null) {
                    mLastLocation = task.result
                    latitude = mLastLocation.latitude
                    longitude = mLastLocation.longitude
                    Log.e(TAG, "lat" + longitude + "lon" + latitude)
                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        if (!permissionHandler.checkWriteToStoragePermission()) {
            permissionHandler.requestWriteToStoragePermission()
        } else {
            imageUries = getImages(this)
            mainAdapter = MainAdapter(imageUries, this)
            binding.recyclerView.layoutManager = GridLayoutManager(applicationContext, 2)
            binding.recyclerView.adapter = mainAdapter
            mainAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }
    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }
    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }
    fun takePhoto(view: View) {
        startLocationUpdates()
        getLastLocation()
        val call: Call<Response> = mainViewModel.getData(latitude, longitude)
        Log.e(TAG , call.request().url().toString())
        call.enqueue(object : Callback<Response> {
            override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                placeName = response.body()?.name.toString()
                temperature = response.body()?.main?.temp!!
                condition = response.body()?.weather?.get(0)?.description!!
                Log.e(TAG, placeName + temperature + condition)
            }

            override fun onFailure(call: Call<Response>, t: Throwable) {
                Log.e(TAG , t.message.toString())
                t.printStackTrace()
            }

        })


        if (!permissionHandler.checkWriteToStoragePermission()) {
            permissionHandler.requestWriteToStoragePermission()
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
            }
        }
    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_LOCATION_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
            } else {
                // Permission denied.
                showSnackbar(getString(R.string.no_location_detected))
            }
        } else if (requestCode == REQUEST_PERMISSIONS_WRITE_STORAGE_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "Capture Image was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission denied.
                showSnackbar(getString(R.string.no_write_storage_permission_detected))
            }
        } else if (requestCode == REQUEST_PERMISSIONS_READ_STORAGE_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageUries = getImages(this)
                mainAdapter = MainAdapter(imageUries, this)
                binding.recyclerView.layoutManager = GridLayoutManager(applicationContext, 2)
                binding.recyclerView.adapter = mainAdapter
                mainAdapter.notifyDataSetChanged()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val extras = data!!.extras
            val imageBitmap = extras!!["data"] as Bitmap?
            val bitmap =
                writeImage.writeTextOnDrawable(imageBitmap, placeName, temperature, condition)
            writeImage.saveImage(bitmap!!)
        }
    }
    override fun onClick(position: Int) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("uri", imageUries[position].toString())
        startActivity(intent)
    }

    private fun showSnackbar(text: String) {
        val container = binding.activityMain
        if (container != null) {
            Snackbar.make(
                findViewById(R.id.activity_main),
                text,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                        "package",
                        "com.example.weatheramlsakrtask", null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .show()
        }
    }
}