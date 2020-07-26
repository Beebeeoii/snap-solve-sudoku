package com.example.snapsolvesudoku.image

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters

class GridExtractor {

    fun contourGridExtract(original : Mat) : Mat {

        var contours : ArrayList<MatOfPoint> = ArrayList(0)
        Imgproc.findContours(original, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var largestArea : MatOfPoint = contours[0]

        for (counter in 0 until contours.size) {
            if (Imgproc.contourArea(largestArea) <= Imgproc.contourArea(contours[counter])) {
                largestArea = contours[counter]
            }
        }

        val corners: Array<Point> = identifyCorners(largestArea.toArray())

        //List of original corners
        val oCorners: ArrayList<Point> = ArrayList<Point>()
        oCorners.add(corners[0])
        oCorners.add(corners[1])
        oCorners.add(corners[2])
        oCorners.add(corners[3])

        //List of final corners
        val fCorners: ArrayList<Point> = ArrayList<Point>()
        fCorners.add(Point(0.0, 0.0))
        fCorners.add(Point(original.width().toDouble(), 0.0))
        fCorners.add(Point(0.0, original.height().toDouble()))
        fCorners.add(Point(original.width().toDouble(), original.height().toDouble()))

        //Perspective Transform
        var initialPoints = Converters.vector_Point2f_to_Mat(oCorners)
        var finalPoints = Converters.vector_Point2f_to_Mat(fCorners)

        val transform = Imgproc.getPerspectiveTransform(initialPoints, finalPoints)
        val destImg = Mat()
        Imgproc.warpPerspective(original, destImg, transform, original.size())

        return destImg
    }

    fun identifyCorners(contourPoints : Array<Point>): Array<Point> {
        var leftTopCorner = Point()
        var rightTopCorner = Point()
        var leftBottomCorner = Point()
        var rightBottomCorner = Point()

        var xplusy : Double
        var xminusy : Double

        var smallestxplusy = 10000000.0
        var smallestxminusy = 10000000.0
        var largestxminusy = 0.0
        var largestxplusy = 0.0

        for (counter in contourPoints.indices) {
            xplusy = contourPoints[counter].x + contourPoints[counter].y
            xminusy = contourPoints[counter].x - contourPoints[counter].y

            //finding left top corner - smallest x+y value
            if (xplusy < smallestxplusy) {
                smallestxplusy = xplusy
                leftTopCorner.x = contourPoints[counter].x
                leftTopCorner.y = contourPoints[counter].y
            }

            //finding left bottom corner - smallest x-y value
            if (xminusy < smallestxminusy) {
                smallestxminusy = xminusy
                leftBottomCorner.x = contourPoints[counter].x
                leftBottomCorner.y = contourPoints[counter].y
            }

            //finding right top corner - largest x-y value
            if (xminusy > largestxminusy) {
                largestxminusy = xminusy
                rightTopCorner.x = contourPoints[counter].x
                rightTopCorner.y = contourPoints[counter].y
            }

            //finding right bottom corner - largest x+y value
            if (xplusy > largestxplusy) {
                largestxplusy = xplusy
                rightBottomCorner.x = contourPoints[counter].x
                rightBottomCorner.y = contourPoints[counter].y
            }
        }

        return arrayOf(leftTopCorner, rightTopCorner, leftBottomCorner, rightBottomCorner)
    }

    //X and Y are swapped as the mat is rotated for preview. aka height of mat is actually the width viewed
    private fun getAverageXCoordFromCorners(corners: Array<Point>): Double {
        val topLeftCornerX = corners[0].x
        val topRightCornerX = corners[1].x
        val bottomLeftCornerX = corners[2].x
        val bottomRightCornerX = corners[3].x

        return (topLeftCornerX + topRightCornerX + bottomLeftCornerX + bottomRightCornerX) / 4
    }

    private fun getAverageYCoordFromCorners(corners: Array<Point>): Double {
        val topLeftCornerY = corners[0].y
        val topRightCornerY = corners[1].y
        val bottomLeftCornerY = corners[2].y
        val bottomRightCornerY = corners[3].y

        return (topLeftCornerY + topRightCornerY + bottomLeftCornerY + bottomRightCornerY) / 4
    }

    fun getAverageXCoordFromContour(contour: Array<Point>): Double {
        return getAverageXCoordFromCorners(identifyCorners(contour))
    }

    fun getAverageYCoordFromContour(contour: Array<Point>): Double {
        return getAverageYCoordFromCorners(identifyCorners(contour))
    }
}