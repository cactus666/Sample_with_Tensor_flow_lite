package com.pickingwords.mushroomchecker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.R.attr.orientation
import android.graphics.Matrix
import android.graphics.RectF
import android.hardware.Camera.CameraInfo
import android.view.*
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_90
import android.view.Surface.ROTATION_0


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, View.OnClickListener {

    fun setPreviewSize() {
        // получаем размеры экрана
        val display: Display = windowManager.defaultDisplay
        val widthIsMax = display.width > display.height

        // определяем размеры превью камеры
        val size = camera!!.parameters.previewSize

        val rectDisplay = RectF()
        val rectPreview = RectF()

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0f, 0f, display.width.toFloat(), display.height.toFloat())

        // RectF первью
        if(size.width.toFloat() < display.width && size.height.toFloat() < display.height) {
            if (widthIsMax) {
                // превью в горизонтальной ориентации
                rectPreview.set(0f, 0f, size.width.toFloat(), size.height.toFloat())
            } else {
                // превью в вертикальной ориентации
                rectPreview.set(0f, 0f, size.height.toFloat(), size.width.toFloat())
            }
        }
        else {
            rectPreview.set(0f, 0f, display.width.toFloat(), display.height.toFloat())
        }

        Log.d("convert", "bottom - ${rectPreview.bottom.toInt()}, right - ${rectPreview.right.toInt()}")

        val matrix = Matrix()
        // подготовка матрицы преобразования
        // если экран будет "втиснут" в превью (третий вариант из урока)
        matrix.setRectToRect(rectDisplay, rectPreview, Matrix.ScaleToFit.START)
        matrix.invert(matrix)

        // преобразование
        matrix.mapRect(rectPreview)

        // установка размеров surface из получившегося преобразования
        sview.layoutParams.height = rectPreview.bottom.toInt()
        sview.layoutParams.width = rectPreview.right.toInt()
        Log.d("convert", "bottom - ${rectPreview.bottom.toInt()}, right - ${rectPreview.right.toInt()}")
    }

    fun setCameraDisplayOrientation(cameraId: Int = 0) {
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
        result = result % 360
        camera!!.setDisplayOrientation(result)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        Log.d("state", "surfaceChanged, ${camera}")
        if (previewRunning) {
            camera!!.stopPreview()
        }
//        val camParams = camera!!.getParameters()
//        val size = camParams.supportedPreviewSizes[0]
//        camParams.setPreviewSize(size.width, size.height)
//        camera!!.setParameters(camParams)
        try {
            camera!!.setPreviewDisplay(holder)
            camera!!.startPreview()
            previewRunning = true
            setCameraDisplayOrientation()
            setPreviewSize()
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
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 50)
            else {
                camera = Camera.open(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
            finish()
        }
        Log.d("state", "surfaceCreated, ${camera}")
    }





    private val mPicture = PictureCallback { data, camera ->
        dir_image2 = File("${Environment.getExternalStorageDirectory()}${File.separator}My Custom Folder")
        dir_image2!!.mkdirs()


        val tmpFile = File(dir_image2, "TempImage.jpg")
        try {
            fos = FileOutputStream(tmpFile)
            fos!!.write(data)
            fos!!.close()
        } catch (e:FileNotFoundException) {
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        } catch (e:IOException) {
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        }

        options = BitmapFactory.Options()
        options!!.inPreferredConfig = Bitmap.Config.ARGB_8888

       // bmp1 = decodeFile(tmpFile)
        //bmp = Bitmap.createScaledBitmap(bmp1, CamView.getWidth(), CamView.getHeight(), true)
        //camera_image.setImageBitmap(bmp)
        //tmpFile.delete()
        //TakeScreenshot()
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.button_1 -> {
                v.isClickable = false
                v.visibility = View.INVISIBLE  //<-----HIDE HERE
                camera!!.takePicture(null, null, mPicture)
            }
        }
    }


    protected var CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0
    private var SurView: SurfaceView? = null
    private var camHolder: SurfaceHolder? = null
    private var previewRunning: Boolean = false
//    private var button1: Button? = null
    var context: Context = this
    var camera: Camera? = null
//    private var camera_image: ImageView? = null
    private var bmp: Bitmap? = null
    private var bmp1: Bitmap? = null
    private var bos: ByteArrayOutputStream? = null
    private var options: BitmapFactory.Options? = null
    private var o: BitmapFactory.Options? = null
    private var o2: BitmapFactory.Options? = null
    private var fis: FileInputStream? = null
    var fis2: ByteArrayInputStream? = null
    private var fos: FileOutputStream? = null
    private var dir_image2: File? = null
    private var dir_image: File? = null
//    private var CamView: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        CamView = findViewById(R.id.camview) as RelativeLayout

        SurView = findViewById<SurfaceView>(R.id.sview)
        camHolder = SurView!!.holder
        camHolder!!.addCallback(this)
        camHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        button_1.setOnClickListener(this)

    }



}
