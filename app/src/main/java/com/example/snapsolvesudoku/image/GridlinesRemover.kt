package com.example.snapsolvesudoku.image

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

private const val TAG = "GridlinesRemover"

class GridlinesRemover {

    fun removeGridLines(grid : Mat, lines : Mat) : Mat {
        val HORIZONTAL_LINE_POSITIONS : Double = grid.rows() / 9.0
        val HORIZONTAL_MIN_THRESHOLD : Double = (HORIZONTAL_LINE_POSITIONS / 100.0) * 15.0
        val HORIZONTAL_MAX_THRESHOLD : Double = (HORIZONTAL_LINE_POSITIONS / 100.0) * 85.0

        val VERTICAL_LINE_POSITIONS : Double = grid.cols() / 9.0
        val VERTICAL_MIN_THRESHOLD : Double = (HORIZONTAL_LINE_POSITIONS / 100.0) * 15.0
        val VERTICAL_MAX_THRESHOLD : Double = (HORIZONTAL_LINE_POSITIONS / 100.0) * 85.0

        val HORIZONTAL_SAME_LINE : Double = (grid.rows() / 100.0) * 1.0
        val VERTICAL_SAME_LINE : Double = (grid.cols() / 100.0) * 1.0

        val LINE_THICKNESS : Int = ((grid.rows() / 100.0) * 1.0).toInt()

        for (i in 0 until lines.rows()) {
            val vec : DoubleArray = lines.get(i, 0)
            val x1 = vec[0]
            val y1 = vec[1]
            val x2 = vec[2]
            val y2 = vec[3]

            val startLine = Point(x1, y1)
            val endLine = Point(x2, y2)

            val isHorizontal = (startLine.y - endLine.y < HORIZONTAL_SAME_LINE) && (startLine.y - endLine.y > -HORIZONTAL_SAME_LINE)
            val isVertical = (startLine.x - endLine.x < VERTICAL_SAME_LINE) && (startLine.x - endLine.x > -VERTICAL_SAME_LINE)

            if (isHorizontal && (startLine.y % HORIZONTAL_LINE_POSITIONS <= HORIZONTAL_MIN_THRESHOLD || startLine.y % HORIZONTAL_LINE_POSITIONS >= HORIZONTAL_MAX_THRESHOLD)) {
                startLine.x = 0.0
                endLine.x = grid.rows().toDouble()
            } else if (isVertical && (startLine.x % VERTICAL_LINE_POSITIONS <= VERTICAL_MIN_THRESHOLD || startLine.x % VERTICAL_LINE_POSITIONS >= VERTICAL_MAX_THRESHOLD)) {
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