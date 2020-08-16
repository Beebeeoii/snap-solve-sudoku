package com.beebeeoii.snapsolvesudoku.utils

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream

private const val TAG = "ImageSaver"

object ImageSaver {

    fun saveMatToMemory(mat: Mat, filePath: String, fileName: String, quality: Int) {
        val bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)

        val processedPicturePath = "${filePath}/${fileName}"
        val fileDir = File(filePath)
        if (!fileDir.exists()) {
            fileDir.mkdir()
        }
        val outputStream = FileOutputStream(processedPicturePath)
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)

        Log.d(TAG, "saveMatToMemory: $fileName saved to $filePath with ${quality}% quality")
    }

    fun saveBitmapToMemory(bitmap: Bitmap, filePath: String, fileName: String, quality: Int) {
        val processedPicturePath = "${filePath}/${fileName}"
        val fileDir = File(filePath)
        if (!fileDir.exists()) {
            fileDir.mkdir()
        }
        val outputStream = FileOutputStream(processedPicturePath)
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream)

        Log.d(TAG, "saveMatToMemory: $fileName saved to $filePath with ${quality}% quality")
    }
}