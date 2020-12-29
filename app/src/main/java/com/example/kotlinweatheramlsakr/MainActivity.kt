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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kotlinweatheramlsakr.view.PermissionHandler
import com.example.kotlinweatheramlsakr.view.PermissionHandler.Companion.REQUEST_PERMISSIONS_LOCATION_CODE
import com.example.kotlinweatheramlsakr.view.PermissionHandler.Companion.REQUEST_PERMISSIONS_READ_STORAGE_CODE
import com.example.kotlinweatheramlsakr.view.PermissionHandler.Companion.REQUEST_PERMISSIONS_WRITE_STORAGE_CODE
import com.example.kotlinweatheramlsakr.databinding.ActivityMainBinding
import com.example.kotlinweatheramlsakr.view.DetailsActivity
import com.example.kotlinweatheramlsakr.view.adapter.MainAdapter
import com.example.kotlinweatheramlsakr.view.adapter.RecyclerViewItemClickListener
import com.example.kotlinweatheramlsakr.viewModel.MainViewModel
import com.example.kotlinweatheramlsakr.data.weatherResponseModel.Response
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), RecyclerViewItemClickListener {

    val REQUEST_IMAGE_CAPTURE = 1
    private val TAG = MainActivity::class.java.simpleName
    protected lateinit var mLastLocation: Location
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationRequest: LocationRequest
    private val mainViewModel: MainViewModel by viewModels()
    private var placeName: String = ""
    private var temperature = 0.0
    private var condition: String = ""
    private lateinit var mainAdapter: MainAdapter
    private lateinit var permissionHandler: PermissionHandler
   // private lateinit var writeImage: WriteImage
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
        permissionHandler = PermissionHandler(this)
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
            mainViewModel.getData(latitude, longitude)
            mainViewModel.weatherData.observe(this, Observer<Response> { response ->
                placeName = response.name.toString()
                temperature = response.main?.temp!!
                condition = response.weather?.get(0)?.description!!

            })

            mainViewModel.loadImages()
            mainAdapter = MainAdapter(this)
            binding.recyclerView.layoutManager = GridLayoutManager(applicationContext, 2)
            binding.recyclerView.adapter = mainAdapter
            mainAdapter.notifyDataSetChanged()
            mainViewModel.images.observe(this, Observer<List<Uri>> { images ->
                mainAdapter.pictureItems = images
                mainAdapter.notifyDataSetChanged()
            })

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

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    fun takePhoto(view: View) {
        startLocationUpdates()
        getLastLocation()

        if (::mLastLocation.isInitialized) {
            if (!permissionHandler.checkWriteToStoragePermission()) {
                permissionHandler.requestWriteToStoragePermission()
            } else {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                } catch (e: ActivityNotFoundException) {
                }
            }
        } else {
            showSnackbar("Sorry, We Can not detect your location")
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
                mainViewModel.loadImages()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val extras = data!!.extras
            val imageBitmap = extras!!["data"] as Bitmap?
            if (placeName != null && condition != null && temperature != null) {
                val bitmap =
                    mainViewModel.writeTextOnDrawable(imageBitmap!!, placeName, temperature, condition)
                mainViewModel.saveImage(bitmap!!)
            }
        }
    }

    override fun onClick(imageUri: String) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("uri", imageUri)
        startActivity(intent)
    }

    private fun showSnackbar(text: String) {
        val container = binding.activityMain
        if (container != null) {
            Snackbar.make(
                findViewById(R.id.activity_main),
                text,
                Snackbar.LENGTH_LONG
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