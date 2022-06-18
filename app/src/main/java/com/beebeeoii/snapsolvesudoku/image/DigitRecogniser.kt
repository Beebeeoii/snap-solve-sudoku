package com.beebeeoii.snapsolvesudoku.image

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.sudoku.SudokuBoard2DIntArray
import com.beebeeoii.snapsolvesudoku.utils.UniqueIdGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "DigitRecogniser"

class DigitRecogniser(private var activity: Activity, board: Mat) {

    private var sudokuBoardMat: Mat = board
    lateinit var sudokuBoard2DIntArray: SudokuBoard2DIntArray

    fun processBoard(fromCamera: Boolean): Bitmap {
        val processedBoardBitmap = Bitmap.createBitmap(sudokuBoardMat.width(), sudokuBoardMat.height(), Bitmap.Config.ARGB_8888)

        val gridExtractor = GridExtractor()
        val gridMat = gridExtractor.contourGridExtract(sudokuBoardMat)

        val imageProcessor = ImageProcessor()
        val processedImage = imageProcessor.processImage(gridMat, fromCamera)

        val gLineRemover = GridlinesRemover()
        val lines = Mat()
        Imgproc.HoughLinesP(processedImage, lines, 1.0, Math.PI / 180, 100, 50.0, 5.0)
        val gridWOLines = gLineRemover.removeGridLines(processedImage, lines)

        Core.bitwise_not(gridWOLines, gridWOLines)
        Utils.matToBitmap(gridWOLines, processedBoardBitmap)

        gridMat.release()
        lines.release()
        gridWOLines.release()

        return processedBoardBitmap
    }

    fun recogniseDigits(boardBitmap: Bitmap): String {
        val modelFileDir = File("${activity.getExternalFilesDir(null).toString()}/model")
        val modelFileName = modelFileDir.list()?.get(0)

        val tflite = Interpreter(
            File(
                "${activity.getExternalFilesDir(null).toString()}/model/${modelFileName}"
            )
        )
        val tImage = TensorImage(DataType.FLOAT32)
        val tempMat = Mat()
        val output = Array(1) { FloatArray(10) }

        val cells = CellExtractor().splitBitmap(boardBitmap, 9, 9)
        var recognisedDigits = ""
        for (i in 0..8) {
            for (j in 0..8) {
                val cell = cells[i][j]
                Utils.bitmapToMat(cell, tempMat)

                val avgPix = Core.mean(tempMat).`val`[0]
                val resizedPic = Bitmap.createScaledBitmap(cell, 28, 28, true)

                tImage.load(resizedPic)
                tflite.run(convertBitmapToByteBuffer(resizedPic), output)

                val result = output[0]
                val maxConfidence = result.maxOrNull()
                val prediction = maxConfidence?.let { it1 -> result.indexOfFirst { it == it1 } }
                val isCellEmpty = avgPix > 250

                if (prediction != null && !isCellEmpty) {
                    recognisedDigits += prediction
                } else {
                    recognisedDigits += "0"
                }

                Log.d(TAG,"Prediction: $prediction, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${isCellEmpty}, PixVal: $avgPix")
            }
        }

        tflite.close()
        return recognisedDigits
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer? {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 28 * 28 )
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(28*28)
        bitmap.getPixels(
            intValues,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )
        var pixel = 0
        for (i in 0 until 28) {
            for (j in 0 until 28) {
                val `val` = intValues[pixel++]
                val red = Color.red(`val`)
                val green = Color.green(`val`)
                val blue = Color.blue(`val`)

                val grayValue = ((red + green + blue) / (3 * 255.0)).toFloat()

                byteBuffer.putFloat(grayValue)
            }
        }
        return byteBuffer
    }
}