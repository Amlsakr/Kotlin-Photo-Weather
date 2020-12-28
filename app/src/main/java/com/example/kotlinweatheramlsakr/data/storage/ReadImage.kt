package com.example.kotlinweatheramlsakr.data.storage

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.kotlinweatheramlsakr.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReadImage {

    companion object {
        fun getImages(activity: Application): List<Uri> {
            val path = File(Environment.getExternalStorageDirectory().toString() + '/' + activity.getString(R.string.app_name))
            var imageUris: MutableList<Uri> = ArrayList()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imageUris = queryImages(activity)
            }
            var fileNames: List<String>? = null
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

        @TargetApi(Build.VERSION_CODES.Q)
        private  fun queryImages(activity: Application): MutableList<Uri> {
            var imageList = mutableListOf<Uri>()
                // TODO: Add code to fetch the images from MediaStore
                val projection = arrayOf(
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media.RELATIVE_PATH)
          //      val  selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"
              val  selection = "${MediaStore.MediaColumns.RELATIVE_PATH}   LIKE ? "

          //    val selectionArgs = arrayOf(dateToTimestamp(day = 1 ,month = 1 , year = 2020).toString())
           val selectionArgs =  arrayOf("%Pictures/" + "KotlinWeatherAmlSakr%")
                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
                       activity.contentResolver.query(
                               MediaStore.Images.Media.EXTERNAL_CONTENT_URI ,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder
                )?.use { cursor ->
                    imageList = addImagesFromCursor(cursor)

                }


            return imageList
        }

        @TargetApi(Build.VERSION_CODES.Q)
        private fun addImagesFromCursor(cursor: Cursor): MutableList<Uri> {
            val images = mutableListOf<Uri>()
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val relativePath = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)
                val dateTaken = Date(cursor.getLong(dateTakenColumn))
                val displayName = cursor.getString(displayNameColumn)
                val path = cursor.getString(relativePath)
                Log.e("path", path)
                val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)


              //  val image = Image(id, displayName, dateTaken, contentUri)
                images += contentUri

            }
            return images
        }

        @Suppress("SameParameterValue")
        @SuppressLint("SimpleDateFormat")
        private fun dateToTimestamp(day: Int, month: Int, year: Int): Long =
                SimpleDateFormat("dd.MM.yyyy").let { formatter ->
                    formatter.parse("$day.$month.$year")?.time ?: 0
                }
    }
}