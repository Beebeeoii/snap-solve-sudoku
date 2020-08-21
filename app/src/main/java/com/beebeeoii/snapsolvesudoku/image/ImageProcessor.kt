package com.beebeeoii.snapsolvesudoku.image

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo

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


        val blurredMat = Mat()
        Imgproc.GaussianBlur(mat, blurredMat, Size(11.0, 11.0), 0.0)


        val threshMat = Mat()
//        Imgproc.threshold(mat, threshMat, 120.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.adaptiveThreshold(blurredMat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 8.0)

        val morphedMat = Mat()
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(4.0, 4.0))
        Imgproc.morphologyEx(threshMat, morphedMat, Imgproc.MORPH_OPEN, kernel)

        val swopMat = Mat()
        Core.bitwise_not(morphedMat, swopMat)

        threshMat.release()

        return swopMat
    }
}