package com.beebeeoii.snapsolvesudoku.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.*
import androidx.annotation.Nullable
import androidx.navigation.fragment.findNavController
import com.beebeeoii.snapsolvesudoku.databinding.FragmentCameraBinding
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
import com.beebeeoii.snapsolvesudoku.image.DigitRecogniser
import com.beebeeoii.snapsolvesudoku.image.GridExtractor
import com.beebeeoii.snapsolvesudoku.utils.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.utils.UniqueIdGenerator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

private const val TAG = "CameraFragment"

private lateinit var backgroundThread : HandlerThread
private lateinit var backgroundHandler : Handler

private var sudokuBoardMat: Mat? = null

class CameraFragment : BottomSheetDialogFragment(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var binding: FragmentCameraBinding

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        sudokuBoardMat = null

        binding.cameraView.visibility = SurfaceView.VISIBLE
        binding.cameraView.setCvCameraViewListener(this)

        binding.captureButton.setOnClickListener {
            if (sudokuBoardMat == null) {
                binding.guideTextView.text = "No board detected"
                binding.guideTextView.invalidate()
            } else {
                binding.cameraView.disableView()
                dialog?.setCancelable(false)

                crossFade()

                Core.rotate(sudokuBoardMat, sudokuBoardMat, Core.ROTATE_90_CLOCKWISE)
                val originalSudokuBitmap = Bitmap.createBitmap(
                    sudokuBoardMat!!.width(),
                    sudokuBoardMat!!.height(),
                    Bitmap.Config.ARGB_8888
                )

                val digitRecogniser = DigitRecogniser(requireActivity(), sudokuBoardMat!!)
                val sudokuBoardBitmap = GlobalScope.async {
                    digitRecogniser.processBoard(true)
                }
                val uniqueId = UniqueIdGenerator.generateId()

                val processFrame = GlobalScope.launch {
                    Utils.matToBitmap(sudokuBoardMat!!, originalSudokuBitmap)

                    val boardDirPath = "${requireActivity().getExternalFilesDir(null).toString()}/${uniqueId}"
                    val boardDirFile = File(boardDirPath)
                    if (!boardDirFile.exists()) {
                        boardDirFile.mkdir()
                    }
                    val originalPicturePath = "${boardDirPath}/${uniqueId}_original.png"
                    val processedPicturePath = "${boardDirPath}/${uniqueId}_processed.png"
                    val out = FileOutputStream(originalPicturePath)
                    val processedOut = FileOutputStream(processedPicturePath)
                    originalSudokuBitmap.compress(Bitmap.CompressFormat.PNG, 50, out)
                    sudokuBoardBitmap.await().compress(Bitmap.CompressFormat.PNG, 50, processedOut)

                    val database = Database.invoke(requireContext())
                    val historyDao = database.getHistoryDao()

                    historyDao.insertHistoryEntry(
                        HistoryEntity(
                            uniqueId = uniqueId,
                            dateTime = DateTimeGenerator.generateDateTimeString(
                                LocalDateTime.now(),
                                DateTimeGenerator.Mode.DATE_AND_TIME
                            ),
                            recognisedDigits = digitRecogniser
                                .recogniseDigits(sudokuBoardBitmap.await()),
                            solutionsPath = null,
                            timeTakenToSolve = null,
                            folderPath = boardDirPath,
                            originalPicturePath = originalPicturePath,
                            processedPicturePath = null
                        )
                    )
                }

                runBlocking {
                    processFrame.join()
                    val action = CameraFragmentDirections
                        .actionCameraFragmentToMainFragment(uniqueId)
                    findNavController().navigate(action)
                }
            }
        }
        return binding.root
    }

    private fun crossFade() {
        val shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        binding.loadingProgressBar.apply {
            alpha = 0f
            visibility = View.VISIBLE

            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }

        binding.captureButton.animate()
            .alpha(0f)
            .setDuration(shortAnimationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.captureButton.visibility = View.GONE
                    binding.guideTextView.visibility = View.GONE
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            bottomSheetDialog.behavior.peekHeight = Resources.getSystem()
                .displayMetrics.heightPixels
        }
    }

    private fun openBackgroundThread() {
        backgroundThread = HandlerThread("cameraBackgroundThread")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun closeBackgroundThread() {
        backgroundThread.quitSafely()
    }

    override fun onResume() {
        super.onResume()
        openBackgroundThread()
        binding.cameraView.setCameraPermissionGranted()
        binding.cameraView.enableView()
    }

    override fun onStop() {
        super.onStop()
        binding.cameraView.disableView()
        closeBackgroundThread()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        Log.d(TAG, "onCameraViewStarted: Camera Width - ${width}px Camera Height - ${height}px")
    }

    override fun onCameraViewStopped() {
        binding.cameraView.disableView()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val ogRGBMat = inputFrame!!.rgba()

        val centrePoint = Point(ogRGBMat.size().width / 2, ogRGBMat.size().height / 2)

        val ogGRAYMat = Mat()
        Imgproc.cvtColor(ogRGBMat, ogGRAYMat, Imgproc.COLOR_BGR2GRAY, 1)

        val blurredMat = Mat()
        Imgproc.GaussianBlur(ogGRAYMat, blurredMat, Size(7.0, 7.0), 0.0)
//        Log.d(TAG, "onCameraFrame: CENTRE COORD (${centrePoint.x}, ${centrePoint.y})")

        val threshMat = Mat()

//        Photo.fastNlMeansDenoising(ogGRAYMat, ogGRAYMat, 5F, 7, 21)
//        Imgproc.threshold(ogGRAYMat, threshMat, 120.0, 255.0, Imgproc.THRESH_BINARY)
        Imgproc.adaptiveThreshold(blurredMat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 4.0)

        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(4.0, 4.0))
        val closed = Mat()
        Imgproc.morphologyEx(threshMat, closed, Imgproc.MORPH_OPEN, kernel)

        val contours : ArrayList<MatOfPoint> = ArrayList(0)
        Imgproc.findContours(threshMat, contours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        val ogArea = ogGRAYMat.size().width * ogGRAYMat.size().height
        val minArea = ogArea * 0.04
        val maxArea = ogArea * 0.3
//        Log.d(TAG, "onCameraFrame: $minArea $maxArea ${contours.size}")

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

        Imgproc.drawMarker(ogRGBMat, Point(centrePoint.x, centrePoint.y), Scalar(204.0, 193.0, 90.0), Imgproc.MARKER_CROSS, 20, 3)
        Imgproc.drawContours(ogRGBMat, realContours, indexOfBoard, Scalar(255.0, 0.0, 255.0), 3)

        try {
            val boundingRect = Imgproc.boundingRect(realContours[indexOfBoard])
            sudokuBoardMat = Mat(ogGRAYMat, Rect((boundingRect.x - 5), (boundingRect.y - 5), (boundingRect.width + 10), (boundingRect.height + 10)))
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        threshMat.release()

        return ogRGBMat
    }
}