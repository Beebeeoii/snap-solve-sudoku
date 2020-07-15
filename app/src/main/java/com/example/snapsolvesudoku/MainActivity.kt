package com.example.snapsolvesudoku

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.util.Range
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.snapsolvesudoku.fragments.MainFragment
import com.example.snapsolvesudoku.image.*
import com.example.snapsolvesudoku.solver.BoardValidator
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        copyTessData()


//
//        textViewOutput.setOnClickListener {
////            imageView.setImageBitmap(null)
//            textViewOutput.visibility = View.GONE
//        }
//
//        imageView.setOnClickListener {
//            sudokuBoard.visibility = View.GONE
//            sudokuBoard.reset()
//        }
    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
//            var photo : Bitmap = data?.extras?.get("data") as Bitmap
//            imageView.setImageBitmap(photo)
//        }
//    }

    private fun loadModelFile (activity : Activity) : MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = activity.assets.openFd("converted_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun copyTessData() {
        try {
            val dir : File = File(getExternalFilesDir(null).toString() + "/model")

            if (!dir.exists()) {
                dir.mkdirs()
            }

            val pathToDataFile = getExternalFilesDir(null).toString() + "/model" + "/model.tflite"
            if (!File(pathToDataFile).exists()) {
                val `in`: InputStream = assets.open("converted_model.tflite")
                val out: OutputStream = FileOutputStream(pathToDataFile)
                val buffer = ByteArray(1024)
                var read: Int = `in`.read(buffer)
                while (read != -1) {
                    out.write(buffer, 0, read)
                    read = `in`.read(buffer)
                }
                `in`.close()
                out.flush()
                out.close()
            }
        } catch (exception : Exception) {
            exception.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}