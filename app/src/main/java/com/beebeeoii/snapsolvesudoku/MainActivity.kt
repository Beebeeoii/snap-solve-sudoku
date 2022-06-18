package com.beebeeoii.snapsolvesudoku

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileOutputStream

private const val TAG = "MainActivity"

//TODO Documentation
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

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        copyModelData()
    }

    @Override
    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private fun copyModelData() {
        // TODO Remove hardcoding of model file name
        val modelFileName = "070820090621"
        val modelPath = "${this.getExternalFilesDir(null).toString()}/model"
        try {
            val dir = File(modelPath)

            if (!dir.exists()) {
                dir.mkdirs()
            } else {
                return
            }

            val pathToDataFile = "${modelPath}/${modelFileName}"
            if (!File(pathToDataFile).exists()) {
                val `in` = this.assets.open(modelFileName)
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
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}