package com.beebeeoii.snapsolvesudoku.image

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.sudokuboard.SudokuBoard2DIntArray
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

class DigitRecogniser(private var activity: Activity, board: Mat) {

    private var sudokuBoardMat: Mat = board
    lateinit var sudokuBoard2DIntArray: SudokuBoard2DIntArray

    fun processBoard(fromCamera: Boolean) : Bitmap {
        val processedBoardBitmap = Bitmap.createBitmap(sudokuBoardMat.width(), sudokuBoardMat.height(), Bitmap.Config.ARGB_8888)

        val imageProcessor = ImageProcessor()
        val processedImage = imageProcessor.processImage(sudokuBoardMat, fromCamera)

        val gridExtractor = GridExtractor()
        val gridMat = gridExtractor.contourGridExtract(processedImage)

        val gLineRemover = GridlinesRemover()
        val lines = Mat()
        Imgproc.HoughLinesP(gridMat, lines, 1.0, Math.PI / 180, 100, 50.0, 5.0)
        val gridWOLines = gLineRemover.removeGridLines(gridMat, lines)

        Core.bitwise_not(gridWOLines, gridWOLines)
        Utils.matToBitmap(gridWOLines, processedBoardBitmap)

        gridMat.release()
        lines.release()
        gridWOLines.release()

        return processedBoardBitmap
    }

    fun recogniseDigits(boardBitmap: Bitmap) {
        sudokuBoard2DIntArray =
            SudokuBoard2DIntArray()

        val modelFileDir = File("${activity.getExternalFilesDir(null).toString()}/model")
        val modelFileName = modelFileDir.list()[0]

        val tflite = Interpreter(
            File(
                "${activity.getExternalFilesDir(null).toString()}/model/${modelFileName}"
            )
        )
        val tImage = TensorImage(DataType.FLOAT32)
        val tempMat = Mat()

        val output = Array(1) { FloatArray(10) }

        val uniqueId = UniqueIdGenerator.uniqueId
        val boardDirPath = "${activity.getExternalFilesDir(null).toString()}/${uniqueId}"
        val processedPicturePath = "${boardDirPath}/${uniqueId}_processed.png"
        val boardDirFile = File(boardDirPath)
        if (!boardDirFile.exists()) {
            boardDirFile.mkdir()
        }
        val outputStream = FileOutputStream(processedPicturePath)
        boardBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
        val database = Database.invoke(activity.applicationContext)
        val historyDao = database.getHistoryDao()
        CoroutineScope(Dispatchers.IO).launch {
            historyDao.updateProcessedPicturePath(
                uniqueId = uniqueId,
                processedPicturePath = processedPicturePath)
        }

        sudokuBoard2DIntArray.uniqueId = uniqueId

        val cells = CellExtractor().splitBitmap(boardBitmap, 9, 9)
        var recognisedDigits = ""
        for (i in 0..8) {
            for (j in 0..8) {
                val cell = cells[i][j]
                Utils.bitmapToMat(cell, tempMat)

                val avgPix = Core.mean(tempMat).`val`[0]
                val byteBuffer = ByteBuffer.allocateDirect(4 * 28 * 28 * 1)

                val resizedPic = Bitmap.createScaledBitmap(cell, 28, 28, true)
                resizedPic.copyPixelsToBuffer(byteBuffer)

                val individialProcessedCellPicturePath = "${boardDirPath}/${i}${j}.png"
                val out = FileOutputStream(individialProcessedCellPicturePath)
                resizedPic.compress(Bitmap.CompressFormat.PNG, 50, out)

                tImage.load(resizedPic)

                tflite.run(convertBitmapToByteBuffer(resizedPic), output)

                val result = output[0]

                val maxConfidence = result.max()
                val prediction = maxConfidence?.let { it1 -> result.indexOf(it1) }
                val isCellEmpty = avgPix > 250

                if (prediction != null && !isCellEmpty) {
                    sudokuBoard2DIntArray.board2DIntArray[i][j] = prediction
                    recognisedDigits += prediction
                } else {
                    recognisedDigits += "0"
                }

                println("Prediction: $prediction, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${isCellEmpty}, PixVal: $avgPix")
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            historyDao.updateRecognisedDigits(
                uniqueId = uniqueId,
                recognisedDigits = recognisedDigits)
        }

        tflite.close()
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