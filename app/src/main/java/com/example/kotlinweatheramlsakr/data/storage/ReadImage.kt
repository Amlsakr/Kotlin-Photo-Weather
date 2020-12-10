package com.example.kotlinweatheramlsakr.data.storage

import android.app.Activity
import android.net.Uri
import android.os.Environment
import com.example.kotlinweatheramlsakr.R
import java.io.File
import java.util.*

class ReadImage {

    companion object {
        fun getImages(activity: Activity): List<Uri> {
            var fileNames: List<String>? = null
            val imageUris: MutableList<Uri> = ArrayList()
            val path = File(Environment.getExternalStorageDirectory().toString() + '/' + activity.getString(R.string.app_name))
            if (path.exists()) {
                fileNames = Arrays.asList(*path.list())
            }
            if (fileNames != null) {
                for (i in fileNames.indices) {

                    ///Now set this bitmap on imageview
                    imageUris.add(Uri.parse(path.toString() + "/" + fileNames[i]))
                }
            }
            return imageUris
        }
    }
}