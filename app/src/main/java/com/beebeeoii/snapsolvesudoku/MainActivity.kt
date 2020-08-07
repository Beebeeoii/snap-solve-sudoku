package com.beebeeoii.snapsolvesudoku

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                }
                else -> super.onManagerConnected(status)
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

        copyModelData()

    }

    private fun copyModelData() {
        val modelFileName = "070820090621"
        val modelPath = "${getExternalFilesDir(null).toString()}/model"
        try {
            val dir = File(modelPath)

            if (!dir.exists()) {
                dir.mkdirs()
            } else {
                return
            }

            val pathToDataFile = "${modelPath}/${modelFileName}"
            if (!File(pathToDataFile).exists()) {
                val `in` = assets.open(modelFileName)
                val out = FileOutputStream(pathToDataFile)
                val buffer = ByteArray(1024)
                var read = `in`.read(buffer)
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
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
}