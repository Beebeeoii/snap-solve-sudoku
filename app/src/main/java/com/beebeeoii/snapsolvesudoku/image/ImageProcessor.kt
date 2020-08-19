package com.beebeeoii.snapsolvesudoku.image

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

private const val TAG = "ImageProcessor"

class ImageProcessor {
    fun processImage(mat : Mat, fromCamera: Boolean) : Mat {
        if (!fromCamera) {
            val threshMat = Mat()
            Imgproc.adaptiveThreshold(mat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 23, 2.0)

            val swopMat = Mat()
             Core.bitwise_not(threshMat, swopMat)
            return swopMat
        }

        val threshMat = Mat()
//        Imgproc.threshold(mat, threshMat, 120.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.adaptiveThreshold(mat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 111, 25.0)

        val swopMat = Mat()
        Core.bitwise_not(threshMat, swopMat)

        threshMat.release()

        return swopMat
    }
}