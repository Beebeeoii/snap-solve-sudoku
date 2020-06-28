package com.example.snapsolvesudoku.image

import android.graphics.Bitmap

private const val TAG = "CellExtractor"

class CellExtractor {

    fun splitBitmap(grid : Bitmap, xCount : Int, yCount : Int) : Array<Array<Bitmap>> {
        var indiCell : Array<Array<Bitmap>> = Array<Array<Bitmap>>(9) {Array(9) { Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)} }

        var width : Int = grid.width / xCount
        var height : Int = grid.height / yCount

        for (x in 0 until xCount) {
            for (y in 0 until yCount) {
                indiCell[y][x] = Bitmap.createBitmap(grid, x * width, y * height, width, height)
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
}