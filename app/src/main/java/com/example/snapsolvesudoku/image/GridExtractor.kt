package com.example.snapsolvesudoku.image

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters

class GridExtractor {

    fun contourGridExtract(original : Mat) : Mat {
        val contours : ArrayList<MatOfPoint> = ArrayList(0)
        Imgproc.findContours(original, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var largestArea = contours[0]

        for (counter in 0 until contours.size) {
            if (Imgproc.contourArea(largestArea) <= Imgproc.contourArea(contours[counter])) {
                largestArea = contours[counter]
            }
        }

        val corners = identifyCorners(largestArea.toArray())

        //List of original corners
        val oCorners: ArrayList<Point> = ArrayList()
        oCorners.add(corners[0])
        oCorners.add(corners[1])
        oCorners.add(corners[2])
        oCorners.add(corners[3])

        //List of final corners
        val fCorners: ArrayList<Point> = ArrayList()
        fCorners.add(Point(0.0, 0.0))
        fCorners.add(Point(original.width().toDouble(), 0.0))
        fCorners.add(Point(0.0, original.height().toDouble()))
        fCorners.add(Point(original.width().toDouble(), original.height().toDouble()))

        //Perspective Transform
        val initialPoints = Converters.vector_Point2f_to_Mat(oCorners)
        val finalPoints = Converters.vector_Point2f_to_Mat(fCorners)

        val transform = Imgproc.getPerspectiveTransform(initialPoints, finalPoints)
        val destImg = Mat()
        Imgproc.warpPerspective(original, destImg, transform, original.size())

        return destImg
    }

    fun identifyCorners(contourPoints : Array<Point>): Array<Point> {
        val leftTopCorner = Point()
        val rightTopCorner = Point()
        val leftBottomCorner = Point()
        val rightBottomCorner = Point()

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
}