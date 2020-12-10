package com.example.kotlinweatheramlsakr.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinweatheramlsakr.R
import com.example.kotlinweatheramlsakr.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

 private  lateinit var activityDetailsBinding: ActivityDetailsBinding
    var uri: Uri? = null
    private lateinit var  scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityDetailsBinding = ActivityDetailsBinding.inflate(layoutInflater)
        val view: View = activityDetailsBinding.root
        setContentView(view)
        uri = Uri.parse(intent.getStringExtra("uri"))
        activityDetailsBinding.detailsimageView.setImageURI(uri)
        scaleGestureDetector = ScaleGestureDetector(
            this,
            ScaleListener()
        )
    }

    fun shareImageView(view: View) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "image/jpeg"
        startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
    }

    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(motionEvent)
        return true
    }

    inner  class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            scaleFactor = scaleGestureDetector.scaleFactor
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f))
            activityDetailsBinding.detailsimageView.setScaleX(scaleFactor)
            activityDetailsBinding.detailsimageView.setScaleY(scaleFactor)
            return true
        }
    }
}