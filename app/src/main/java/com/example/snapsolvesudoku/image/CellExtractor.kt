package com.example.snapsolvesudoku.image

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

private const val TAG = "CellExtractor"

class CellExtractor {

    fun splitBitmap(grid : Bitmap, xCount : Int, yCount : Int) : Array<Array<Bitmap>> {
        var indiCell : Array<Array<Bitmap>> = Array(9) {Array(9) { Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)} }

        var width : Int = grid.width / xCount
        var height : Int = grid.height / yCount

        Log.d(TAG, "splitBitmap: $width $height")

        for (row in 0 until xCount) {
            for (col in 0 until yCount) {
                val cellBitmap = Bitmap.createBitmap(grid, col * width, row * height, width, height)
                val cellMat = Mat()
                Utils.bitmapToMat(cellBitmap, cellMat)

                Log.d(TAG, "splitBitmap: Cell $row $col info:")

                val avgPix = Core.mean(cellMat).`val`[0]
                Log.d(TAG, "splitBitmap: avgpix:$avgPix")

                indiCell[row][col] = digitExtract(cellBitmap)
            }
        }

        return indiCell
    }

    private fun digitExtract(originalCellBitmap : Bitmap) : Bitmap {
        val cellArea = originalCellBitmap.height * originalCellBitmap.width
        val minAreaForDigits = 0.07 * cellArea

        var originalCellMat = Mat()
        var contours = mutableListOf<MatOfPoint>()
        Utils.bitmapToMat(originalCellBitmap, originalCellMat)

        Log.d(TAG, "digitExtract: cell area $cellArea")

        Imgproc.cvtColor(originalCellMat, originalCellMat, Imgproc.COLOR_BGRA2GRAY)
        Core.bitwise_not(originalCellMat, originalCellMat)
        
        Imgproc.findContours(originalCellMat, contours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        if (contours.isEmpty()) {
            return originalCellBitmap
        }

        contours.sortByDescending {
            Imgproc.contourArea(it)
        }

        val largestContour = contours[0]
        val croppingBound = Imgproc.boundingRect(largestContour)

        if (croppingBound.area() < minAreaForDigits) {
            Log.d(TAG, "digitExtract: Rejected Cell Area: ${croppingBound.area()} Percentage: ${croppingBound.area() / cellArea * 100}")
            var emptyBitmap = Bitmap.createBitmap(originalCellMat.width(), originalCellMat.height(), Bitmap.Config.ARGB_8888)
            emptyBitmap.eraseColor(Color.WHITE)
            return emptyBitmap
        } else {
            var croppedDigitMat = Mat(originalCellMat, croppingBound)
            val margin = croppedDigitMat.height() / 2

            Log.d(TAG, "digitExtract: Accepted Cell Area: ${croppingBound.area()} Percentage: ${croppingBound.area() / cellArea * 100}")

            Core.copyMakeBorder(croppedDigitMat, croppedDigitMat, margin, margin, margin, margin, Core.BORDER_CONSTANT, Scalar(0.0, 0.0, 0.0))

            var croppedDigitBitmap = Bitmap.createBitmap(croppedDigitMat.width(), croppedDigitMat.height(), Bitmap.Config.ARGB_8888)

            Core.bitwise_not(croppedDigitMat, croppedDigitMat)
            Utils.matToBitmap(croppedDigitMat, croppedDigitBitmap)

            return croppedDigitBitmap
        }
    }
}