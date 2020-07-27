package com.example.snapsolvesudoku.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.example.snapsolvesudoku.R
import com.example.snapsolvesudoku.SudokuBoard2DIntArray
import com.example.snapsolvesudoku.image.CellExtractor
import com.example.snapsolvesudoku.image.GridExtractor
import com.example.snapsolvesudoku.image.GridlinesRemover
import com.example.snapsolvesudoku.image.ImageProcessor
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG = "CameraFragment"

private lateinit var cameraView : JavaCamera2View

private lateinit var backgroundThread : HandlerThread
private lateinit var backgroundHandler : Handler

private lateinit var constraintLayout: ConstraintLayout
private lateinit var guideTextView: MaterialTextView
private lateinit var loadingProgressBar: ProgressBar

private lateinit var captureButton: ExtendedFloatingActionButton

private lateinit var sudokuBoardMat: Mat

class CameraFragment : BottomSheetDialogFragment(), CameraBridgeViewBase.CvCameraViewListener2 {

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        cameraView = view.findViewById(R.id.cameraView)
        constraintLayout = view.findViewById(R.id.cameraFragmentContainer)
        captureButton = view.findViewById(R.id.captureButton)
        guideTextView = view.findViewById(R.id.guideTextView)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)

        val displayMetrics = DisplayMetrics()
        val windowManager = requireActivity().windowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        cameraView.visibility = SurfaceView.VISIBLE
        cameraView.setCvCameraViewListener(this)
        cameraView.setMaxFrameSize(height, width)

        captureButton.setOnClickListener {
            cameraView.disableView()
            dialog?.setCancelable(false)

            crossfade()

            Core.rotate(sudokuBoardMat, sudokuBoardMat, Core.ROTATE_90_CLOCKWISE)

            var pic = Bitmap.createBitmap(sudokuBoardMat.width(), sudokuBoardMat.height(), Bitmap.Config.ARGB_8888)
            val sudokuBoard2DIntArray = SudokuBoard2DIntArray()

            val processedImage = GlobalScope.async {
                val imageProcessor = ImageProcessor()
                val processedImage = imageProcessor.processImage(sudokuBoardMat)

                val gridExtractor = GridExtractor()
                val gridMat = gridExtractor.contourGridExtract(processedImage)

                val gLineRemover = GridlinesRemover()
                val lines = Mat()
                Imgproc.HoughLinesP(gridMat, lines, 1.0, Math.PI / 180, 100, 50.0, 5.0)
                val gridWOLines = gLineRemover.removeGridLines(gridMat, lines)

                Core.bitwise_not(gridWOLines, gridWOLines)
                Utils.matToBitmap(gridWOLines, pic)

                gridMat.release()
                lines.release()
                gridWOLines.release()

                pic
            }

            GlobalScope.launch {
                val tflite = Interpreter(File(requireActivity().getExternalFilesDir(null).toString() + "/model/model.tflite"))
                var tImage = TensorImage(DataType.FLOAT32)
                var tempMat = Mat()

                val output = Array(1){FloatArray(10)}

                var dirFile = File(requireActivity().getExternalFilesDir(null).toString())
                var noFiles = dirFile.listFiles().size
                var boardDirFile = File(requireActivity().getExternalFilesDir(null).toString() + "/board" + noFiles.toString())
                if (!boardDirFile.exists()) {
                    boardDirFile.mkdir()
                }
                var out = FileOutputStream(requireActivity().getExternalFilesDir(null).toString() + "/board" + noFiles.toString() + "/og.png")

                val processedBoard = processedImage.await()
                processedBoard.compress(Bitmap.CompressFormat.PNG, 100, out)

                val cells = CellExtractor().splitBitmap(processedBoard, 9, 9)

                for (i in 0..8) {
                    for (j in 0..8) {
                        val cell = cells[i][j]
                        Utils.bitmapToMat(cell, tempMat)

                        val avgPix = Core.mean(tempMat).`val`[0]
                        var byteBuffer = ByteBuffer.allocateDirect(4* 28*28*1)

                        val resizedPic = Bitmap.createScaledBitmap(cell, 28, 28, true)
                        resizedPic.copyPixelsToBuffer(byteBuffer)

                        var dirFile = File(requireActivity().getExternalFilesDir(null).toString())
                        var noFiles = dirFile.listFiles().size
                        var boardDirFile = File(requireActivity().getExternalFilesDir(null).toString() + "/board" + (noFiles - 1).toString())
                        var boardNoFiles = boardDirFile.listFiles().size
                        var out = FileOutputStream(requireActivity().getExternalFilesDir(null).toString() + "/board" + (noFiles - 1).toString() + "/" + boardNoFiles + ".png")
                        resizedPic.compress(Bitmap.CompressFormat.PNG, 100, out)

                        tImage.load(resizedPic)

                        tflite.run(convertBitmapToByteBuffer(resizedPic), output)

                        val result = output[0]

                        val maxConfidence = result.max()
                        val prediction = maxConfidence?.let { it1 -> result.indexOf(it1) }
                        val isCellEmpty = avgPix > 250

                        if (prediction != null && !isCellEmpty) {
                            sudokuBoard2DIntArray.board2DIntArray[i][j] = prediction
                        }

                        println("Prediction: $prediction, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${isCellEmpty}, PixVal: $avgPix")
                    }
                }

                tflite.close()

                val action = CameraFragmentDirections.actionCameraFragmentToMainFragment(sudokuBoard2DIntArray)
                findNavController().navigate(action)
            }
        }
        return view
    }

    private fun crossfade() {
        val shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        loadingProgressBar.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        captureButton.animate()
            .alpha(0f)
            .setDuration(shortAnimationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    captureButton.visibility = View.GONE
                    guideTextView.visibility = View.GONE
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            val d = it as BottomSheetDialog
            d.behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer? {
        val byteBuffer =
            ByteBuffer.allocateDirect(4 * 28 * 28 )
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
                var `val` = intValues[pixel++]
                var red = Color.red(`val`)
                var green = Color.green(`val`)
                var blue = Color.blue(`val`)

                var grayValue = ((red + green + blue) / (3 * 255.0)).toFloat()

                byteBuffer.putFloat(grayValue)
            }
        }
        return byteBuffer
    }

    private fun openBackgroundThread() {
        backgroundThread = HandlerThread("cameraBackgroundThread")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
        }
    }

    override fun onResume() {
        super.onResume()
        openBackgroundThread()
        cameraView.enableView()
    }

    override fun onStop() {
        super.onStop()
        cameraView.disableView()
        closeBackgroundThread()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.d(TAG, "onCameraViewStarted: Camera Width - ${width}px Camera Height - ${height}px")
    }

    override fun onCameraViewStopped() {
        cameraView.disableView()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {        
        val ogRGBMat = inputFrame!!.rgba()
        val centrePoint = Point(ogRGBMat.size().width / 2, ogRGBMat.size().height / 2)

        val ogGRAYMat = Mat()
        Imgproc.cvtColor(ogRGBMat, ogGRAYMat, Imgproc.COLOR_BGR2GRAY, 1)

        Log.d(TAG, "onCameraFrame: CENTRE COORD (${centrePoint.x}, ${centrePoint.y})")

        var blurMat = Mat()
        Imgproc.GaussianBlur(ogGRAYMat, blurMat, Size(9.0, 9.0), 0.0)

        var threshMat = Mat()
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 2.0)

        var contours : ArrayList<MatOfPoint> = ArrayList(0)
        Imgproc.findContours(threshMat, contours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        val ogArea = ogGRAYMat.size().width * ogGRAYMat.size().height
        val minArea = ogArea * 0.04
        val maxArea = ogArea * 0.3
        Log.d(TAG, "onCameraFrame: $minArea $maxArea ${contours.size}")

        val realContours = ArrayList<MatOfPoint>(0)
        for (counter in 1 until contours.size) {
            val contourArea = Imgproc.contourArea(contours[counter], true)

            if (contourArea > minArea && contourArea < maxArea) {
                realContours.add(contours[counter])
            }
        }

        val helper = GridExtractor()

        realContours.sortBy {
            Imgproc.contourArea(it)
        }

        val indexOfBoard = realContours.indexOfFirst {
            val corners = helper.identifyCorners(it.toArray())
            val topLeftCornerX = corners[0].x
            val topRightCornerX = corners[1].x
            val bottomLeftCornerX = corners[2].x
            val bottomRightCornerX = corners[3].x
            val topLeftCornerY = corners[0].y
            val topRightCornerY = corners[1].y
            val bottomLeftCornerY = corners[2].y
            val bottomRightCornerY = corners[3].y

            val isXInRange = centrePoint.x <= (topRightCornerX + bottomRightCornerX) / 2 && centrePoint.x >= (topLeftCornerX + bottomLeftCornerX) / 2
            val isYInRange = centrePoint.y <= (bottomLeftCornerY + bottomRightCornerY) / 2 && centrePoint.y >= (topLeftCornerY + topRightCornerY) / 2
            isXInRange && isYInRange
        }

        Imgproc.drawMarker(ogRGBMat, Point(centrePoint.x, centrePoint.y), Scalar(204.0, 193.0, 90.0),Imgproc.MARKER_CROSS, 20,3)
        Imgproc.drawContours(ogRGBMat, realContours, indexOfBoard, Scalar(255.0, 0.0, 255.0),3)

        try {
            val boundingRect = Imgproc.boundingRect(realContours[indexOfBoard])
            sudokuBoardMat = Mat(ogGRAYMat, Rect((boundingRect.x - 10), (boundingRect.y - 10), (boundingRect.width + 20), (boundingRect.height + 20)))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        blurMat.release()
        threshMat.release()

        return ogRGBMat
    }
}