package com.example.kotlinweatheramlsakr.data

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import com.example.kotlinweatheramlsakr.R
import com.google.android.material.snackbar.Snackbar

class PermissionHandler (var activity: Activity) {
    // Used in checking for runtime permissions.
    companion object {
        val REQUEST_PERMISSIONS_LOCATION_CODE = 34
        val REQUEST_PERMISSIONS_WRITE_STORAGE_CODE = 35
        val REQUEST_PERMISSIONS_READ_STORAGE_CODE = 36
    }
    private val TAG = PermissionHandler::class.java.simpleName


    /**
     * Returns the current state of the permissions needed.
     */
    fun checkLocationPermission() = checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

    fun requestLocationPermission() =
        requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSIONS_LOCATION_CODE)


    fun checkReadFromStoragePermission() = checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)

    fun requestReadFromStoragePermission() =
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_PERMISSIONS_READ_STORAGE_CODE)



    fun checkWriteToStoragePermission() = checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    fun requestWriteToStoragePermission() =
        requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_PERMISSIONS_WRITE_STORAGE_CODE)


    private fun checkPermissions(permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(activity!!,  //   Manifest.permission.ACCESS_FINE_LOCATION
                permission)
    }

    private fun requestPermissions(permission: String, requestCode: Int) {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                permission)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                    activity!!.findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, View.OnClickListener { // Request permission
                        ActivityCompat.requestPermissions(activity!!, arrayOf(permission),
                                requestCode)
                    })
                    .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(activity!!, arrayOf(permission),
                    requestCode)
        }
    }
}