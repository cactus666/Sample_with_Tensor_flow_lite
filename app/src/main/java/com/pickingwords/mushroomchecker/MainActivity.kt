package com.pickingwords.mushroomchecker

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.Camera.PictureCallback
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pickingwords.mushroomchecker.action_with_bitmap.resizeBitmap
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.concurrent.thread


class MainActivity: Activity(), SurfaceHolder.Callback, View.OnClickListener {

    override fun onClick(v: View) {
        when(v.id) {
            R.id.check -> {
                startProgressBar()
                camera!!.takePicture(null, null, mPicture)
            }
            R.id.again -> {
                windowView.onRestart()
                result.visibility = View.GONE
                again.visibility = View.GONE
                check.visibility = View.VISIBLE
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
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        holder = surfaceView.holder
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        holder.addCallback(this)

        check.setOnClickListener(this)
        again.setOnClickListener(this)

        introduction.post{
            check.post{
                windowView.initializeSizeLimitation(
                    introduction.y.toInt() + introduction.height,
                    check.y.toInt()
                )
            }
        }
        settingModel()
    }

    private fun settingModel() {
        val tfliteModel = loadModelFile(this)
        tflite = Interpreter(tfliteModel)//, Interpreter.Options())
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

        thread{

            val bmp = BitmapFactory.decodeByteArray(data, 0, data.size)

            Log.i("getBitmap", "w = ${windowView.width}, h = ${windowView.height}, sw = ${surfaceView.width}, sh = ${surfaceView.height} percent: ${windowView.getTopLimitPercent()}, ${windowView.getBottomLimitPercent()}, ${windowView.getLeftLimitPercent()}, ${windowView.getRightLimitPercent()}")
            Log.i("getBitmap", "newPercent: ${windowView.getTopLimitPercent() * windowView.width} == ${( windowView.getTopLimitPercent() * windowView.width + (surfaceView.width - windowView.width) / 2 ) / surfaceView.width}")

//        val newBitmap = croppedBitmap(
//            bmp,
//            ( windowView.getTopLimitPercent() * windowView.height + (surfaceView.height - windowView.height) / 2 ) / surfaceView.height,
//            ( windowView.getBottomLimitPercent() * windowView.height + (surfaceView.height - windowView.height) / 2 ) / surfaceView.height,
//            ( windowView.getLeftLimitPercent() * windowView.width + (surfaceView.width - windowView.width) / 2 ) / surfaceView.width,
//            ( windowView.getRightLimitPercent() * windowView.width + (surfaceView.width - windowView.width) / 2 ) / surfaceView.width
//        )

//        Log.i("getBitmap", "size = w - ${bmp.width}, ${bmp.height}")
//        Log.i("getBitmap", "size2 = w - ${newBitmap.width}, ${newBitmap.height}")

            val resizeBitmap = resizeBitmap(bmp)


            checkBitmapInModel(resizeBitmap)
        }
    }

    private fun checkBitmapInModel(testBitmap: Bitmap) {
        var dataFromImage = FloatArray(480 * 480 * 3) {0.0f}
        var indexColor = 0
        for (x in 0 until testBitmap.width) {
            for (y in 0 until testBitmap.height) {
                dataFromImage[indexColor] = (Color.red(testBitmap.getPixel(x,y)).toFloat()/ 255.0).toFloat()
                dataFromImage[indexColor + 1] = (Color.green(testBitmap.getPixel(x,y)).toFloat()/ 255.0).toFloat()
                dataFromImage[indexColor + 2] = (Color.blue(testBitmap.getPixel(x,y)).toFloat() / 255.0).toFloat()
                indexColor += 3
            }
        }
        runInference(dataFromImage)
    }

    private fun runInference(dataFromImage: FloatArray) {
        val inputData: ByteBuffer = ByteBuffer.allocateDirect(
            1 // 1 dimension
                    * 480 //6 attributes/columns
                    * 480 //1 row
                    * 3 //4 bytes per number as the number is float
        )
        inputData.order(ByteOrder.nativeOrder())

        val inputBuffer = ByteBuffer.allocateDirect(3 * 4 * 480 * 480)
            .apply { order(ByteOrder.nativeOrder()) }

        fun FloatArray.unwindToByteBuffer() {
            inputBuffer.rewind()
            for (f in this) {
                inputBuffer.putFloat(f)
            }
        }

        dataFromImage.unwindToByteBuffer()

        val labelProbArray: Array<FloatArray> = Array(1) { FloatArray(1) }

        tflite.run(inputBuffer, labelProbArray)

        val prediction = (labelProbArray[0][0])

        runOnUiThread {
            showPrediction(prediction)
        }
    }

    private fun loadModelFile(activity: Activity): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun showPrediction(prediction: Float) {
        Log.d("result", "prediction = $prediction")
        stopProgressBar()
        if (prediction > 0.6) {
            showWinMessage()
        }
        else{
            if (prediction < 0.4){
                showHarmfulMessage()
            }
            else {
                showFailMessage()
            }
        }
    }


    private fun startProgressBar() {
        check.visibility = View.GONE
        progress_bar.visibility = View.VISIBLE
    }

    private fun stopProgressBar() {
        progress_bar.visibility = View.GONE
        again.visibility = View.VISIBLE
    }

    private fun showWinMessage() {
        result.setTextColor(ContextCompat.getColor(applicationContext, R.color.color_stroke_win))
        result.setText(R.string.win)
        result.visibility = View.VISIBLE
        windowView.onWin()
    }

    private fun showHarmfulMessage() {
        result.setTextColor(ContextCompat.getColor(applicationContext, R.color.color_stroke_harmful))
        result.setText(R.string.harmful)
        result.visibility = View.VISIBLE
        windowView.onHarmful()
    }

    private fun showFailMessage() {
        result.setTextColor(ContextCompat.getColor(applicationContext, R.color.color_stroke_fail))
        result.setText(R.string.fail)
        result.visibility = View.VISIBLE
        windowView.onFail()
    }

}
