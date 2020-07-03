package com.example.snapsolvesudoku.image

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.lang.Exception

private const val TAG = "CellExtractor"

class CellExtractor {

    fun splitBitmap(grid : Bitmap, xCount : Int, yCount : Int) : Array<Array<Bitmap>> {
        var indiCell : Array<Array<Bitmap>> = Array<Array<Bitmap>>(9) {Array(9) { Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)} }

        var width : Int = grid.width / xCount
        var height : Int = grid.height / yCount

        for (x in 0 until xCount) {
            for (y in 0 until yCount) {
                var tempBitmap = Bitmap.createBitmap(grid, x * width, y * height, width, height)
                indiCell[y][x] = digitExtract(tempBitmap)
            }
        }

        return indiCell
    }

    fun splitIntoRows(grid : Bitmap, yCount : Int) : Array<Bitmap> {
        var indiRows : Array<Bitmap> = Array<Bitmap>(9){Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888)}

        var width = grid.width
        var height = grid.height / yCount

        for (y in 0 until yCount) {
            indiRows[y] = Bitmap.createBitmap(grid, 0, y * height, width, height)
        }

        return indiRows
    }

    fun splitIntoCols(grid : Bitmap, xCount : Int) : Array<Bitmap> {
        var indiCols : Array<Bitmap> = Array<Bitmap>(9){Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888)}

        var width = grid.width / xCount
        var height = grid.height

        for (x in 0 until xCount) {
            indiCols[x] = Bitmap.createBitmap(grid, x * width, 0, width, height)
        }

        return indiCols
    }

    fun digitExtract(cell : Bitmap) : Bitmap {
        var digitFloodFill = Mat()
        var contours = mutableListOf<MatOfPoint>()
        Utils.bitmapToMat(cell, digitFloodFill)

        Imgproc.cvtColor(digitFloodFill, digitFloodFill, Imgproc.COLOR_BGRA2GRAY)
        Core.bitwise_not(digitFloodFill, digitFloodFill)

        Imgproc.findContours(digitFloodFill, contours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE)

        if (contours.isEmpty()) {
            println("CELL EMPTY")
            return cell
        }

        var largestArea = contours[0]

        println("Contour size: ${contours.size}")

        for (counter in 0 until contours.size) {
            if (Imgproc.contourArea(largestArea) <= Imgproc.contourArea(contours[counter])) {
                largestArea = contours[counter]
                println("Area: ${Imgproc.contourArea(largestArea)}")
            }
        }

        var bound = Imgproc.boundingRect(largestArea)
        println("Length: ${bound.width}, Height: ${bound.height}")
        var temp = digitFloodFill.submat(bound)

        val borderMargin = temp.height() /2

        Core.copyMakeBorder(temp, digitFloodFill, borderMargin, borderMargin, borderMargin, borderMargin, Core.BORDER_CONSTANT, Scalar(0.0, 0.0, 0.0))

        var bitmap = Bitmap.createBitmap(digitFloodFill.width(), digitFloodFill.height(), Bitmap.Config.ARGB_8888)
        Core.bitwise_not(digitFloodFill, digitFloodFill)
        Utils.matToBitmap(digitFloodFill, bitmap)

        return bitmap
    }
}