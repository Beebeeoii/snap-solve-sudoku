package com.example.snapsolvesudoku.image

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.lang.Exception

private const val TAG = "CellExtractor"

class CellExtractor {

    fun splitBitmap(grid : Bitmap, xCount : Int, yCount : Int) : Array<Array<Bitmap>> {
        var indiCell : Array<Array<Bitmap>> = Array(9) {Array(9) { Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)} }

        var width : Int = grid.width / xCount
        var height : Int = grid.height / yCount

        Log.d(TAG, "splitBitmap: $width $height")

        for (x in 0 until xCount) {
            for (y in 0 until yCount) {
                var tempBitmap = Bitmap.createBitmap(grid, x * width, y * height, width, height)
                println("Cell $x $y info:")
                var tempMat = Mat()
                Utils.bitmapToMat(tempBitmap, tempMat)
                val avgPix = Core.mean(tempMat).`val`[0]
                Log.d(TAG, "splitBitmap: avgpix:$avgPix")
                indiCell[y][x] = digitExtract(tempBitmap)
            }
        }

        return indiCell
    }

    fun splitBitmap(grid : Mat, xCount : Int, yCount : Int) : Array<Array<Mat>> {
        var indiCell : Array<Array<Mat>> = Array<Array<Mat>>(9) {Array(9) { Mat()} }

        var width : Int = grid.width() / xCount
        var height : Int = grid.height() / yCount
        Log.d(TAG, "splitBitmap: $width $height")

        for (x in 0 until xCount) {
            for (y in 0 until yCount) {
                var croppedCell = Mat(grid, Rect(x *width, y * height, width, height)).clone()
//                var tempBitmap = Bitmap.createBitmap(grid, x * width, y * height, width, height)
//                println("Cell $x $y info:")

                indiCell[y][x] = digitExtract(croppedCell)
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

        Log.d(TAG, "digitExtract: cell area ${cell.height * cell.width}")
        var digitFloodFill = Mat()
        var contours = mutableListOf<MatOfPoint>()
        Utils.bitmapToMat(cell, digitFloodFill)

        Imgproc.cvtColor(digitFloodFill, digitFloodFill, Imgproc.COLOR_BGRA2GRAY)
        Core.bitwise_not(digitFloodFill, digitFloodFill)
        
        Imgproc.findContours(digitFloodFill, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        if (contours.isEmpty()) {
            println("CELL EMPTY")
            return cell
        }

        var largestArea = contours[0]

        for (counter in 0 until contours.size) {
            if (Imgproc.contourArea(largestArea) <= Imgproc.contourArea(contours[counter])) {
                largestArea = contours[counter]
                println("Area: ${Imgproc.contourArea(largestArea)}")
            }
        }

        var bound = Imgproc.boundingRect(largestArea)
        if (Imgproc.contourArea(largestArea) < 150) {
            println("CELL NOISE")
            var bitmap = Bitmap.createBitmap(digitFloodFill.width(), digitFloodFill.height(), Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            return bitmap
        }

        println("Length: ${bound.width}, Height: ${bound.height}")
        var temp = Mat(digitFloodFill, bound)

        val borderMargin = temp.height() /2

        Core.copyMakeBorder(temp, temp, borderMargin, borderMargin, borderMargin, borderMargin, Core.BORDER_CONSTANT, Scalar(0.0, 0.0, 0.0))

        var bitmap = Bitmap.createBitmap(temp.width(), temp.height(), Bitmap.Config.ARGB_8888)
        Core.bitwise_not(temp, temp)
        Utils.matToBitmap(temp, bitmap)

        return bitmap
    }

    fun digitExtract(cell : Mat) : Mat {
        var contours = mutableListOf<MatOfPoint>()

//        Imgproc.cvtColor(cell, cell, Imgproc.COLOR)
        Core.bitwise_not(cell, cell)

        Imgproc.findContours(cell, contours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE)

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
        if (Imgproc.contourArea(largestArea) < 300) {
            println("CELL NOISE")

//            var bitmap = Bitmap.createBitmap(cell.width(), cell.height(), Bitmap.Config.ARGB_8888)
//            bitmap.eraseColor(Color.WHITE)
            return Mat(cell.rows(), cell.cols(), CvType.CV_8UC3,  Scalar(255.0, 255.0, 255.0))
        }

        println("Length: ${bound.width}, Height: ${bound.height}")
        var temp = cell.submat(bound)

        val borderMargin = temp.height() /2

        Core.copyMakeBorder(temp, cell, borderMargin, borderMargin, borderMargin, borderMargin, Core.BORDER_CONSTANT, Scalar(0.0, 0.0, 0.0))

//        var bitmap = Bitmap.createBitmap(cell.width(), cell.height(), Bitmap.Config.ARGB_8888)
//        Core.bitwise_not(cell, cell)
//        Utils.matToBitmap(cell, bitmap)

        return cell
    }
}