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

        val denoisedMat = Mat()
        Photo.fastNlMeansDenoising(mat, denoisedMat, 13F, 13, 23)

        val blurMat = Mat()
        Imgproc.GaussianBlur(denoisedMat, blurMat, Size(7.0, 7.0), 0.0)

        denoisedMat.release()

        val threshMat = Mat()
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 23, 2.0)

        blurMat.release()

        val swopMat = Mat()
        Core.bitwise_not(threshMat, swopMat)

        threshMat.release()

        return swopMat
    }
}