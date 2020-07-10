package com.example.snapsolvesudoku.image

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo

private const val TAG = "ImageProcessor"

class ImageProcessor {

    fun processImage(bitmap : Bitmap) : Mat {
        var originalMat = Mat()
        Utils.bitmapToMat(bitmap, originalMat)


        var grayMat = Mat()
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY, 1)

        Photo.fastNlMeansDenoising(grayMat, grayMat, 13F, 13, 23)

        var blurMat = Mat()
        Imgproc.GaussianBlur(grayMat, blurMat, Size(15.0, 15.0), 0.0)

        var threshMat = Mat()
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 2.0)

        var swopMat = Mat()
        Core.bitwise_not(threshMat, swopMat)

        Imgproc.dilate(swopMat,swopMat,Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, Size(3.0, 3.0), Point(2.0, 2.0)))

        return swopMat
    }
}