package com.pickingwords.mushroomchecker


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.IOException


class MainActivity3: Activity(), SurfaceHolder.Callback, View.OnClickListener {

    override fun onClick(v: View) {
        when(v.id) {
//            R.id.button_1 -> {
//                v.isClickable = false
//                v.visibility = View.INVISIBLE  //<-----HIDE HERE
//                camera!!.takePicture(null, null, mPicture)
//            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.d("state", "surfaceChanged, ${camera}")
        if (previewRunning) {
            camera!!.stopPreview()
        }
        try {
            camera!!.setPreviewDisplay(holder)
            camera!!.startPreview()
            previewRunning = true
            setCameraDisplayOrientation()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.d("state", "surfaceDestroyed, ${camera}")
        camera!!.stopPreview()
        camera!!.release()
        camera=null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA)
            else {
                camera = Camera.open(CAMERA_ID)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
            finish()
        }
        setPreviewSize()
    }

    private lateinit var holder: SurfaceHolder
    private var camera: Camera? = null
    private val CAMERA_ID = 0
    private var previewRunning: Boolean = false
    protected var REQUEST_PERMISSION_CAMERA = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main2)

        holder = surfaceView.holder
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        holder.addCallback(this)
//        button_1.setOnClickListener(this)
    }


    private fun setPreviewSize() {
        val size = camera!!.parameters.previewSize

//        val set = ConstraintSet()
//        set.clone(constraintLayout)
//        set.setDimensionRatio(surfaceView.id, "${size.height}:${size.width}")
//        set.applyTo(constraintLayout)

    }

    private fun setCameraDisplayOrientation(cameraId: Int = CAMERA_ID) {
        // определяем насколько повернут экран от нормального положения
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result = 0

        // получаем инфо по камере cameraId
        val info = CameraInfo()
        Camera.getCameraInfo(cameraId, info)

        // задняя камера
        if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            result = 360 - degrees + info.orientation
        } else
        // передняя камера
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = 360 - degrees - info.orientation
                result += 360
            }
        result %= 360
        camera!!.setDisplayOrientation(result)
    }

}
