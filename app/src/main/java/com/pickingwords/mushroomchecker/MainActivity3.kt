package com.pickingwords.mushroomchecker

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main2.*
import java.nio.file.Files.delete
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.widget.Toast
import java.io.File.separator
import android.os.Environment.getExternalStorageDirectory
import android.hardware.Camera.PictureCallback
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Environment
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import android.R.attr.bitmap
import android.R.attr.name
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.pickingwords.mushroomchecker.action_with_bitmap.croppedBitmap
import com.pickingwords.mushroomchecker.action_with_bitmap.resizeBitmap
import java.nio.ByteBuffer


class MainActivity3: Activity(), SurfaceHolder.Callback, View.OnClickListener {

    override fun onClick(v: View) {
        when(v.id) {
            R.id.check -> {
//                v.isClickable = false
//                v.visibility = View.INVISIBLE  //<-----HIDE HERE
                // progress bar
                camera!!.takePicture(null, null, mPicture)
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        if (previewRunning) {
            camera!!.stopPreview()
        }
        try {
            camera!!.setPreviewDisplay(holder)
            camera!!.startPreview()
            previewRunning = true
            setCameraDisplayOrientation()
        } catch (e: IOException) {
            Log.e("surface", "surfaceChanged: stack - ${e.printStackTrace()}")
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
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
            setPreviewSize()
        } catch (e: Exception) {
            Log.e("surface", "surfaceCreated: stack - ${e.printStackTrace()}")
            finish()
        }
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

        check.setOnClickListener(this)

        introduction.post{
            check.post{
                windowView.initializeSizeLimitation(
                    introduction.y.toInt() + introduction.height,
                    check.y.toInt()
                )
            }
        }

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

    private val mPicture = PictureCallback { data, camera ->
        camera.startPreview()

//        val configBmp = Bitmap.Config.valueOf(bitmap.getConfig().name())
//        val bitmap_tmp = Bitmap.createBitmap(width, height, configBmp)
//        val buffer = ByteBuffer.wrap(data)
//        bitmap_tmp.copyPixelsFromBuffer(buffer)

        val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)
//        croppedBitmap(bmp, 0.25f, 0.75f, 0.25f, 0.75f)



        Log.d("getBitmap", "w = ${windowView.width}, h = ${windowView.height}, sw = ${surfaceView.width}, sh = ${surfaceView.height} percent: ${windowView.getTopLimitPercent()}, ${windowView.getBottomLimitPercent()}, ${windowView.getLeftLimitPercent()}, ${windowView.getRightLimitPercent()}")

        Log.d("getBitmap", "newPercent: ${windowView.getTopLimitPercent() * windowView.width} == ${( windowView.getTopLimitPercent() * windowView.width + (surfaceView.width - windowView.width) / 2 ) / surfaceView.width}")


//        val newBitmap = croppedBitmap(
//            bmp,
//            ( windowView.getTopLimitPercent() * windowView.height + (surfaceView.height - windowView.height) / 2 ) / surfaceView.height,
//            ( windowView.getBottomLimitPercent() * windowView.height + (surfaceView.height - windowView.height) / 2 ) / surfaceView.height,
//            ( windowView.getLeftLimitPercent() * windowView.width + (surfaceView.width - windowView.width) / 2 ) / surfaceView.width,
//            ( windowView.getRightLimitPercent() * windowView.width + (surfaceView.width - windowView.width) / 2 ) / surfaceView.width
//        )


//        Log.d("getBitmap", "size = w - ${bmp.width}, ${bmp.height}")
//        Log.d("getBitmap", "size2 = w - ${newBitmap.width}, ${newBitmap.height}")

        val resizeBitmap = resizeBitmap(bmp)
//        Log.d("getBitmap", "sizeresizeBitmap = w - ${resizeBitmap.width}, ${resizeBitmap.height}")

            ...

        /*
//        val dir_image2 = File("${Environment.getExternalStorageDirectory()}${File.separator}My Custom Folder")
//        dir_image2.mkdirs()

        val path = "${Environment.getExternalStorageDirectory()}/Mushroom.jpg"
        val mushroomFile = File(path)
        try {
            val fos = FileOutputStream(mushroomFile)
            fos.write(data)
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.e("getPicture", "FileNotFoundException: ${e.printStackTrace()}")
        } catch (e:IOException) {
            Log.e("getPicture", "IOException: ${e.printStackTrace()}")
        }

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        val mushroomBitmap = decodeFile(mushroomFile)
        Log.d("getBitmap", "mushroomBitmap w - ${mushroomBitmap!!.width}, h - ${mushroomBitmap.height}")

//        val bmp = Bitmap.createScaledBitmap(bmp1, CamView.getWidth(), CamView.getHeight(), true)
//        camera_image.setImageBitmap(bmp)
//        tmpFile.delete()
//        TakeScreenshot()
         */
    }


    fun decodeFile(f: File): Bitmap {
//        var b: Bitmap? = null
//        try {
//            // Decode image size
//            val o = BitmapFactory.Options()
//            o.inJustDecodeBounds = true
//
//            var fis = FileInputStream(f)
//            BitmapFactory.decodeStream(fis, null, o)
//            fis.close()
//            val IMAGE_MAX_SIZE = 1000
//            var scale = 1
//            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
//                scale = Math.pow(
//                    2.0,
//                    Math.round(
//                        Math.log(
//                            IMAGE_MAX_SIZE / Math.max(
//                                o.outHeight,
//                                o.outWidth
//                            ) as Double
//                        ) / Math.log(0.5)
//                    ).toInt().toDouble()
//                ).toInt()
//            }
//
//            // Decode with inSampleSize
//            val o2 = BitmapFactory.Options()
//            o2.inSampleSize = scale
//            fis = FileInputStream(f)
//            b = BitmapFactory.decodeStream(fis, null, o2)
//            fis.close()
//
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        return b

        val inputStream = FileInputStream(f)//f.inputStream()
        val drawableFromFile = Drawable.createFromStream(inputStream, null)
        return drawableFromFile.toBitmap()

    }



}
