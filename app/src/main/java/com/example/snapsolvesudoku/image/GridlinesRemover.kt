package com.example.snapsolvesudoku.image

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

private const val TAG = "GridlinesRemover"

class GridlinesRemover {

    fun removeGridLines(grid : Mat, lines : Mat) : Mat {
        val LINE_POSITIONS : Double = grid.rows() / 9.0
        val MIN_THRESHOLD : Double = (LINE_POSITIONS / 100.0) * 15.0
        val MAX_THRESHOLD : Double = (LINE_POSITIONS / 100.0) * 85.0
        val SAME_LINE : Double = (grid.rows() / 100.0) * 1.0
        val LINE_THICKNESS : Int = ((grid.rows() / 100.0) * 1.0).toInt()

        for (i in 0 until lines.rows()) {
            val vec : DoubleArray = lines.get(i, 0)
            val x1 = vec[0]
            val y1 = vec[1]
            val x2 = vec[2]
            val y2 = vec[3]

            val startLine : Point = Point(x1, y1)
            val endLine : Point = Point(x2, y2)

            val isHorizontal = (startLine.y - endLine.y < SAME_LINE) && (startLine.y - endLine.y > -SAME_LINE)
            val isVertical = (startLine.x - endLine.x < SAME_LINE) && (startLine.x - endLine.x > -SAME_LINE)

            if (isHorizontal && (startLine.y % LINE_POSITIONS <= MIN_THRESHOLD || startLine.y % LINE_POSITIONS >= MAX_THRESHOLD)) {
                startLine.x = 0.0
                endLine.x = grid.rows().toDouble()
            } else if (isVertical && (startLine.x % LINE_POSITIONS <= MIN_THRESHOLD || startLine.x % LINE_POSITIONS >= MAX_THRESHOLD)) {
                startLine.y = 0.0
                endLine.y = grid.cols().toDouble()
            } else {
                startLine.x = 0.0
                startLine.y = 0.0
                endLine.x = 0.0
                endLine.y = 0.0
            }

            Imgproc.line(grid, startLine, endLine, Scalar(0.0, 0.0,0.0), LINE_THICKNESS)
        }

        return grid
    }
}