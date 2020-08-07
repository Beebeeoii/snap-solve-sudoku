package com.beebeeoii.snapsolvesudoku.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Resources
import android.graphics.Bitmap
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
import com.beebeeoii.snapsolvesudoku.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.DigitRecogniser
import com.beebeeoii.snapsolvesudoku.UniqueIdGenerator
import com.beebeeoii.snapsolvesudoku.*
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
import com.beebeeoii.snapsolvesudoku.image.GridExtractor
import com.beebeeoii.snapsolvesudoku.fragments.CameraFragmentDirections
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

private const val TAG = "CameraFragment"

private lateinit var cameraView : JavaCamera2View

private lateinit var backgroundThread : HandlerThread
private lateinit var backgroundHandler : Handler

private lateinit var constraintLayout: ConstraintLayout
private lateinit var guideTextView: MaterialTextView
private lateinit var loadingProgressBar: ProgressBar

private lateinit var captureButton: ExtendedFloatingActionButton

private var sudokuBoardMat: Mat? = null

class CameraFragment : BottomSheetDialogFragment(), CameraBridgeViewBase.CvCameraViewListener2 {

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        cameraView = view.findViewById(R.id.cameraView)
        constraintLayout = view.findViewById(R.id.cameraFragmentContainer)
        captureButton = view.findViewById(R.id.captureButton)
        guideTextView = view.findViewById(R.id.guideTextView)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)

        sudokuBoardMat = null

        val displayMetrics = DisplayMetrics()
        val windowManager = requireActivity().windowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        cameraView.visibility = SurfaceView.VISIBLE
        cameraView.setCvCameraViewListener(this)
        cameraView.setMaxFrameSize(height, width)

        captureButton.setOnClickListener {
            if (sudokuBoardMat == null) {
                guideTextView.text = "No board detected"
                guideTextView.invalidate()
            } else {
                cameraView.disableView()
                dialog?.setCancelable(false)

                crossfade()

                Core.rotate(
                    sudokuBoardMat,
                    sudokuBoardMat, Core.ROTATE_90_CLOCKWISE)
                val originalSudokuBitmap = Bitmap.createBitmap(sudokuBoardMat!!.width(), sudokuBoardMat!!.height(), Bitmap.Config.ARGB_8888)

                val digitRecogniser =
                    DigitRecogniser(
                        requireActivity(),
                        sudokuBoardMat!!
                    )
                val sudokuBoardBitmap = GlobalScope.async {
                    digitRecogniser.processBoard(true)
                }
                GlobalScope.launch {
                    Utils.matToBitmap(sudokuBoardMat!!, originalSudokuBitmap)
                    val uniqueId = UniqueIdGenerator.generateId().uniqueId
                    val boardDirPath = "${requireActivity().getExternalFilesDir(null).toString()}/${uniqueId}"
                    val boardDirFile = File(boardDirPath)
                    if (!boardDirFile.exists()) {
                        boardDirFile.mkdir()
                    }
                    val originalPicturePath = "${boardDirPath}/${uniqueId}_original.png"
                    val out = FileOutputStream(originalPicturePath)
                    originalSudokuBitmap.compress(Bitmap.CompressFormat.PNG, 50, out)

                    val database = Database.invoke(requireContext())
                    val historyDao = database.getHistoryDao()
                    CoroutineScope(Dispatchers.IO).launch {
                        historyDao.insertHistoryEntry(
                            HistoryEntity(
                                uniqueId = uniqueId,
                                dateTime = DateTimeGenerator.generateDateTime(DateTimeGenerator.DATE_AND_TIME),
                                folderPath = boardDirPath,
                                originalPicturePath = originalPicturePath
                            )
                        )
                    }

                    digitRecogniser.recogniseDigits(sudokuBoardBitmap.await())
                    val action = CameraFragmentDirections.actionCameraFragmentToMainFragment(digitRecogniser.sudokuBoard2DIntArray)
                    findNavController().navigate(action)
                }
            }
        }
        return view
    }

    private fun crossfade() {
        val shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        loadingProgressBar.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }

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

    private fun openBackgroundThread() {
        backgroundThread = HandlerThread("cameraBackgroundThread")
        backgroundThread.start()
        backgroundHandler = Handler(
            backgroundThread.looper)
    }

    private fun closeBackgroundThread() {
        backgroundThread.quitSafely()
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

        val blurMat = Mat()
        Imgproc.GaussianBlur(ogGRAYMat, blurMat, Size(9.0, 9.0), 0.0)

        val threshMat = Mat()
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 2.0)

        val contours : ArrayList<MatOfPoint> = ArrayList(0)
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