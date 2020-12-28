package com.example.kotlinweatheramlsakr.data.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.kotlinweatheramlsakr.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream

class WriteImage (var context : Context) {


    fun convertToPixels(nDP: Int): Int {
        val conversionScale = context.resources.displayMetrics.density
        return (nDP * conversionScale + 0.5f).toInt()
    }

    fun saveImage(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + context.getString(R.string.app_name))
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            Log.e("saveuri", uri.toString())
            if (uri != null) {
                try {
                    saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + '/' + context!!.getString(R.string.app_name))
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            try {
                saveImageToStream(bitmap, FileOutputStream(file))
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun contentValues(): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun writeTextOnDrawable(bm: Bitmap?, placeName: String?, temperature: Double, condition: String): Bitmap? {
        val workingBitmap = Bitmap.createBitmap(bm!!)
        val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tf = Typeface.create("Helvetica", Typeface.NORMAL)
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(79, 195, 247)
        paint.typeface = tf
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = convertToPixels(8).toFloat()
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)
        val textRect = Rect()
        paint.getTextBounds(condition, 0, condition.length, textRect)
        val canvas = Canvas(mutableBitmap)

        //If the text is bigger than the canvas , reduce the font size
        if (textRect.width() >= canvas.width - 4) //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.textSize = convertToPixels( 7).toFloat() //Scaling needs to be used for different dpi's

        //Calculate the positions
        val xPos = canvas.width / 2 - 2 //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        val yPos = (canvas.height / 3 - (paint.descent() + paint.ascent()) / 2).toInt()
        canvas.drawText(placeName!!, xPos.toFloat(), yPos.toFloat(), paint)
        canvas.drawText(temperature.toString(), xPos.toFloat(), (yPos + 30).toFloat(), paint)
        canvas.drawText(condition, xPos.toFloat(), (yPos + 60).toFloat(), paint)
        return mutableBitmap
    }

}