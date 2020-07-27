package com.example.snapsolvesudoku.image

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo

private const val TAG = "ImageProcessor"

class ImageProcessor {
    fun processImage(mat : Mat) : Mat {
        var denoisedMat = Mat()
        Photo.fastNlMeansDenoising(mat, denoisedMat, 13F, 13, 23)

        var blurMat = Mat()
        Imgproc.GaussianBlur(denoisedMat, blurMat, Size(11.0, 11.0), 0.0)

        var threshMat = Mat()
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 2.0)

        var swopMat = Mat()
        Core.bitwise_not(threshMat, swopMat)

        denoisedMat.release()
        blurMat.release()
        threshMat.release()

        return swopMat
    }
}