package com.example.snapsolvesudoku.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.example.snapsolvesudoku.R
import com.example.snapsolvesudoku.SudokuBoard
import com.example.snapsolvesudoku.SudokuBoard2DIntArray
import com.example.snapsolvesudoku.image.CellExtractor
import com.example.snapsolvesudoku.image.GridExtractor
import com.example.snapsolvesudoku.image.GridlinesRemover
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.JavaCamera2View
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo
import org.opencv.utils.Converters
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val TAG = "CameraFragment"

private lateinit var cameraView : JavaCamera2View

private lateinit var mCameraDevice: CameraDevice
private lateinit var mCaptureSession: CameraCaptureSession
private lateinit var surfaceTextureListener : TextureView.SurfaceTextureListener
private lateinit var imageReader: ImageReader
private lateinit var backgroundThread : HandlerThread
private lateinit var backgroundHandler : Handler

private lateinit var constraintLayout: ConstraintLayout
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

        val displayMetrics = DisplayMetrics()
        val windowManager = requireActivity().windowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width: Int = displayMetrics.widthPixels
        val height: Int = displayMetrics.heightPixels

        cameraView.visibility = SurfaceView.VISIBLE
        cameraView.setCvCameraViewListener(this)
        cameraView.setMaxFrameSize(height, width)

        val tflite = Interpreter(File(requireActivity().getExternalFilesDir(null).toString() + "/model/model.tflite"))
        var tImage = TensorImage(DataType.FLOAT32)
        val output = Array(1){FloatArray(10)}

        captureButton.setOnClickListener {
            cameraView.disableView()
            dialog?.setCancelable(false)

            Core.rotate(sudokuBoardMat, sudokuBoardMat, Core.ROTATE_90_CLOCKWISE)
            Core.bitwise_not(sudokuBoardMat, sudokuBoardMat)

            var pic = Bitmap.createBitmap(sudokuBoardMat.width(), sudokuBoardMat.height(), Bitmap.Config.ARGB_8888)
            val sudokuBoard2DIntArray = SudokuBoard2DIntArray()

            val processedImage = GlobalScope.async {
                Photo.fastNlMeansDenoising(sudokuBoardMat, sudokuBoardMat, 13F, 13, 23)

                val gridExtractor = GridExtractor()
                val gridMat = gridExtractor.contourGridExtract(sudokuBoardMat)

                val gLineRemover = GridlinesRemover()
                val lines = Mat()
                Imgproc.HoughLinesP(gridMat, lines, 1.0, Math.PI / 180, 150, 200.0, 25.0)
                val gridWOLines = gLineRemover.removeGridLines(gridMat, lines)

                Core.bitwise_not(gridWOLines, gridWOLines)
                Utils.matToBitmap(gridWOLines, pic)

                pic
            }

            GlobalScope.launch {
                val tflite = Interpreter(File(requireActivity().getExternalFilesDir(null).toString() + "/model/model.tflite"))
                var tImage = TensorImage(DataType.FLOAT32)
                var tempMat = Mat()

                val output = Array(1){FloatArray(10)}

                var dirFile = File(requireActivity().getExternalFilesDir(null).toString())
                var noFiles = dirFile.listFiles().size
                var out = FileOutputStream(requireActivity().getExternalFilesDir(null).toString() + "/pic" + noFiles.toString() + ".png")

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
                        var out = FileOutputStream(requireActivity().getExternalFilesDir(null).toString() + "/pic" + noFiles.toString() + ".png")
                        resizedPic.compress(Bitmap.CompressFormat.PNG, 100, out)

                        //TODO Save individual cells into a folder for the scanned board
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
//        cameraView.setLifecycleOwner(viewLifecycleOwner)

//        cameraView.layoutParams = FrameLayout.LayoutParams(width, width, Gravity.CENTER)
//        cameraView.setPreviewStreamSize { mutableListOfSizes ->
//            mutableListOfSizes.sortByDescending {
//                it.width.toFloat() * it.height.toFloat()
//            }
//            mutableListOfSizes.sortByDescending {
//                it.width.toFloat() / it.height.toFloat()
//            }
//            var previewSize = mutableListOfSizes.first()
//            var sizesString = ""
//
//            for (size in mutableListOfSizes) {
//                Log.d(TAG, "Camera Size Available: ${size.width} x ${size.height} - Ratio: ${size.width.toFloat() / size.height.toFloat()}")
//                sizesString += "${size.width} x ${size.height} - Ratio: ${size.width.toFloat() / size.height.toFloat()}\n"
//            }
//
//            mutableListOfSizes
//        }
//        cameraView.addFrameProcessor {
//            Log.d(TAG, "onCreateView: ${it.format} ${it.size.width} ${it.size.height}")
//
//            val frameMat = Mat(it.size.height + it.size.height / 2, it.size.width, CvType.CV_8UC1)
//            frameMat.put(0, 0, it.getData<ByteArray>())
//            Imgproc.cvtColor( frameMat, frameMat, Imgproc.COLOR_YUV2GRAY_NV21)
//            Core.rotate(frameMat, frameMat, Core.ROTATE_90_CLOCKWISE)
//
//            val imgprocessor = ImageProcessor()
//            val processedMat = imgprocessor.processImage(frameMat)
//
////            val gridExtractor = GridExtractor()
////            val gridMat = gridExtractor.contourGridExtract(processedMat)
////
////            val gLineRemover = GridlinesRemover()
////            val lines = Mat()
////            Imgproc.HoughLinesP(gridMat, lines, 1.0, Math.PI / 180, 150, 200.0, 25.0)
////            val gridWOLines = gLineRemover.removeGridLines(gridMat, lines)
////
////            Core.bitwise_not(gridMat, gridMat)
//            var board = Bitmap.createBitmap(processedMat.width(), processedMat.height(), Bitmap.Config.ARGB_8888)
//            Utils.matToBitmap(frameMat, board)
//            Log.d(TAG, "onCreateView: ${processedMat.width()} ${processedMat.height()}")
//            image.setImageBitmap(board)
//
//
////            val cells = CellExtractor().splitBitmap(gridWOLines, 9, 9)
////
////            for (i in 0..8) {
////                for (j in 0..8) {
////                    val cell = cells[i][j]
//////                                            Utils.bitmapToMat(cell, tempMat)
////                    var tempBitmap = Bitmap.createBitmap(cell.width(), cell.height(), Bitmap.Config.ARGB_8888)
////                    Utils.matToBitmap(cell, tempBitmap)
////
////                    val avgPix = Core.mean(cell).`val`[0]
////                    var byteBuffer = ByteBuffer.allocateDirect(4* 28*28*1)
////
////                    val resizedPic = Bitmap.createScaledBitmap(tempBitmap, 28, 28, true)
////                    resizedPic.copyPixelsToBuffer(byteBuffer)
//////                        GlobalScope.launch {
//////                            resizedPic.compress(Bitmap.CompressFormat.PNG, 100, out)
//////                        }
////                    //TODO Save individual cells into a folder for the scanned board
////                    tImage.load(resizedPic)
////
////                    tflite.run(convertBitmapToByteBuffer(resizedPic), output)
////
////                    val result = output[0]
////
////                    val maxConfidence = result.max()
////                    val prediction = maxConfidence?.let { it1 -> result.indexOf(it1) }
////                    val isCellEmpty = avgPix > 250
////
////                    if (prediction != null && !isCellEmpty) {
////                        sudokuBoardTest.cells[i][j].value = prediction
////                        sudokuBoardTest.invalidate()
////                    }
////
////                    println("Prediction: $prediction, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${isCellEmpty}, PixVal: $avgPix")
////                }
////            }
////
////            sudokuBoardTest.reset()
//        }
//
//        cameraView.addCameraListener(object : CameraListener() {
//            override fun onPictureTaken(result: PictureResult) {
//                result.toBitmap {
//                    dialog?.setCancelable(false)
//
//                    val sudokuBoard2DIntArray = SudokuBoard2DIntArray()
//
//                    val processedImage = GlobalScope.async {
//                        val imgprocessor = ImageProcessor()
//                        val processedMat = imgprocessor.processImage(it!!)
//
//                        val gridExtractor = GridExtractor()
//                        val gridMat = gridExtractor.contourGridExtract(processedMat)
//
//                        val gLineRemover = GridlinesRemover()
//                        val lines = Mat()
//                        Imgproc.HoughLinesP(gridMat, lines, 1.0, Math.PI / 180, 150, 200.0, 25.0)
//                        val gridWOLines = gLineRemover.removeGridLines(gridMat, lines)
//
//
//                        Core.bitwise_not(gridWOLines, gridWOLines)
//                        Utils.matToBitmap(gridWOLines, it!!)
//
//                        it!!
//                    }
//
//                    GlobalScope.launch {
//                        val tflite = Interpreter(File(requireActivity().getExternalFilesDir(null).toString() + "/model/model.tflite"))
//                        var tImage = TensorImage(DataType.FLOAT32)
//                        var tempMat = Mat()
//
//                        val output = Array(1){FloatArray(10)}
//
//                        var dirFile = File(requireActivity().getExternalFilesDir(null).toString())
//                        var noFiles = dirFile.listFiles().size
//                        var out = FileOutputStream(requireActivity().getExternalFilesDir(null).toString() + "/pic" + noFiles.toString() + ".png")
//
//                        val processedBoard = processedImage.await()
//                        processedBoard.compress(Bitmap.CompressFormat.PNG, 100, out)
//
//                        val cells = CellExtractor().splitBitmap(processedBoard, 9, 9)
//
//                        for (i in 0..8) {
//                            for (j in 0..8) {
//                                val cell = cells[i][j]
//                                Utils.bitmapToMat(cell, tempMat)
//
//                                val avgPix = Core.mean(tempMat).`val`[0]
//                                var byteBuffer = ByteBuffer.allocateDirect(4* 28*28*1)
//
//                                val resizedPic = Bitmap.createScaledBitmap(cell, 28, 28, true)
//                                resizedPic.copyPixelsToBuffer(byteBuffer)
//                                //                        GlobalScope.launch {
//                                //                            resizedPic.compress(Bitmap.CompressFormat.PNG, 100, out)
//                                //                        }
//                                //TODO Save individual cells into a folder for the scanned board
//                                tImage.load(resizedPic)
//
//                                tflite.run(convertBitmapToByteBuffer(resizedPic), output)
//
//                                val result = output[0]
//
//                                val maxConfidence = result.max()
//                                val prediction = maxConfidence?.let { it1 -> result.indexOf(it1) }
//                                val isCellEmpty = avgPix > 250
//
//                                if (prediction != null && !isCellEmpty) {
//                                    sudokuBoard2DIntArray.board2DIntArray[i][j] = prediction
//                                }
//
//                                println("Prediction: $prediction, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${isCellEmpty}, PixVal: $avgPix")
//                            }
//                        }
//
//                        tflite.close()
//
//                        val action = CameraFragmentDirections.actionCameraFragmentToMainFragment(sudokuBoard2DIntArray)
//                        findNavController().navigate(action)
//                    }
//                }
//
//            }
//        })



//        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
//            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
//                Log.wtf(TAG, "onSurfaceTextureSizeChanged: Size changed")
//            }
//
//            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
//                Log.d(TAG, "onSurfaceTextureUpdated: ")
//            }
//
//            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
//                Log.d(TAG, "onSurfaceTextureDestroyed: ")
//                return true
//            }
//
//            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
//                Log.d(TAG, "onSurfaceTextureAvailable: ")
//
//            }
//        }

//        captureButton.setOnClickListener {
//            crossfade()
//            val startTime = System.currentTimeMillis()
//            dialog?.setCancelable(false)
//
//            var pic = cameraView.bitmap
//            val sudokuBoard2DIntArray = SudokuBoard2DIntArray()
//
//            val processedImage = GlobalScope.async {
//                val imgprocessor = ImageProcessor()
//                val processedMat = imgprocessor.processImage(pic)
//
//                val gridExtractor = GridExtractor()
//                val gridMat = gridExtractor.contourGridExtract(processedMat)
//
//                val gLineRemover = GridlinesRemover()
//                val lines = Mat()
//                Imgproc.HoughLinesP(gridMat, lines, 1.0, Math.PI / 180, 150, 200.0, 25.0)
//                val gridWOLines = gLineRemover.removeGridLines(gridMat, lines)
//
//
//                Core.bitwise_not(gridWOLines, gridWOLines)
//                Utils.matToBitmap(gridWOLines, pic)
//
//                pic
//            }
//
//            GlobalScope.launch {
//                val tflite = Interpreter(File(requireActivity().getExternalFilesDir(null).toString() + "/model/model.tflite"))
//                var tImage = TensorImage(DataType.FLOAT32)
//                var tempMat = Mat()
//
//                val output = Array(1){FloatArray(10)}
//
//                var dirFile = File(requireActivity().getExternalFilesDir(null).toString())
//                var noFiles = dirFile.listFiles().size
//                var out = FileOutputStream(requireActivity().getExternalFilesDir(null).toString() + "/pic" + noFiles.toString() + ".png")
//
//                val processedBoard = processedImage.await()
//                processedBoard.compress(Bitmap.CompressFormat.PNG, 100, out)
//
//                val cells = CellExtractor().splitBitmap(processedBoard, 9, 9)
//
//                for (i in 0..8) {
//                    for (j in 0..8) {
//                        val cell = cells[i][j]
//                        Utils.bitmapToMat(cell, tempMat)
//
//                        val avgPix = Core.mean(tempMat).`val`[0]
//                        var byteBuffer = ByteBuffer.allocateDirect(4* 28*28*1)
//
//                        val resizedPic = Bitmap.createScaledBitmap(cell, 28, 28, true)
//                        resizedPic.copyPixelsToBuffer(byteBuffer)
////                        GlobalScope.launch {
////                            resizedPic.compress(Bitmap.CompressFormat.PNG, 100, out)
////                        }
//                        //TODO Save individual cells into a folder for the scanned board
//                        tImage.load(resizedPic)
//
//                        tflite.run(convertBitmapToByteBuffer(resizedPic), output)
//
//                        val result = output[0]
//
//                        val maxConfidence = result.max()
//                        val prediction = maxConfidence?.let { it1 -> result.indexOf(it1) }
//                        val isCellEmpty = avgPix > 250
//
//                        if (prediction != null && !isCellEmpty) {
//                            sudokuBoard2DIntArray.board2DIntArray[i][j] = prediction
//                        }
//
//                        println("Prediction: $prediction, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${isCellEmpty}, PixVal: $avgPix")
//                    }
//                }
//
//                tflite.close()
//
//                val action = CameraFragmentDirections.actionCameraFragmentToMainFragment(sudokuBoard2DIntArray)
//                val endTime = System.currentTimeMillis()
//                val timeTaken = (endTime - startTime) / 1000
//                findNavController().navigate(action)
//            }
//        }

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

//    private fun startCameraSession() {
//        val cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        if (cameraManager.cameraIdList.isEmpty()) {
//            // no cameras
//            return
//        }
//
//        val firstCamera = cameraManager.cameraIdList[0]
//        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//
//        cameraManager.openCamera(firstCamera, object: CameraDevice.StateCallback() {
//            var frameWidth = 0
//            var frameHeight = 0
//            override fun onDisconnected(p0: CameraDevice) {
//                Log.wtf(TAG, "Camera Closed")
//                p0.close()
//            }
//            override fun onError(p0: CameraDevice, p1: Int) {
//                Log.wtf(TAG, "Camera Closed")
//                p0.close()
//            }
//
//            override fun onOpened(cameraDevice: CameraDevice) {
//                mCameraDevice = cameraDevice
//
//                // use the camera
//                val cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraDevice.id)
//
//                cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
//                    streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)?.let { yuvSizes ->
//                        val displayMetrics = DisplayMetrics()
//                        val windowManager = requireActivity().windowManager
//
//                        windowManager.defaultDisplay.getMetrics(displayMetrics)
//                        val width: Int = displayMetrics.widthPixels
//                        println(width)
//                        yuvSizes.sortByDescending {
//                            it.width.toFloat() * it.height.toFloat()
//                        }
//                        yuvSizes.sortBy {
//                            it.width.toFloat() / it.height.toFloat()
//                        }
//                        var previewSize = yuvSizes.first()
//                        var sizesString = ""
//
//                        for (size in yuvSizes) {
//                            Log.d(TAG, "Camera Size Available: ${size.width} x ${size.height} - Ratio: ${size.width.toFloat() / size.height.toFloat()}")
//                            sizesString += "${size.width} x ${size.height} - Ratio: ${size.width.toFloat() / size.height.toFloat()}\n"
//                        }
//
//                        // cont.
//                        val displayRotation = windowManager.defaultDisplay.rotation
//                        val swappedDimensions = areDimensionsSwapped(displayRotation, cameraCharacteristics)
//                        // swap width and height if needed
//                        val rotatedPreviewWidth = if (swappedDimensions) previewSize.height else previewSize.width
//                        val rotatedPreviewHeight = if (swappedDimensions) previewSize.width else previewSize.height
//
//                        Log.d(TAG, "Camera Dimensions After adjustment: $rotatedPreviewWidth $rotatedPreviewHeight")
//                        frameWidth = rotatedPreviewWidth
//                        frameHeight = rotatedPreviewHeight
//
//                        val tflite = Interpreter(File(requireActivity().getExternalFilesDir(null).toString() + "/model/model.tflite"))
//                        var tImage = TensorImage(DataType.FLOAT32)
//                        val output = Array(1){FloatArray(10)}
//
//                        imageReader = ImageReader.newInstance(frameWidth, frameHeight, ImageFormat.YUV_420_888, 2)
//                        imageReader.setOnImageAvailableListener({
//                            imageReader.acquireLatestImage()?.let {
//                                var byteBuffer = it.planes[0].buffer
//                                var bytes = ByteArray(byteBuffer.remaining())
//                                byteBuffer.get(bytes)
//
//                                var mat = Mat(it.height, it.width, CvType.CV_8UC1)
//                                mat.put(0, 0, bytes)
//                                Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE)
//
//                                GlobalScope.launch {
//                                    val imgprocessor = ImageProcessor()
//                                    val processedMat = imgprocessor.processImage(mat)
//
//                                    val gridExtractor = GridExtractor()
//                                    val gridMat = gridExtractor.contourGridExtract(processedMat)
//
//                                    val gLineRemover = GridlinesRemover()
//                                    val lines = Mat()
//                                    Imgproc.HoughLinesP(gridMat, lines, 1.0, Math.PI / 180, 150, 200.0, 25.0)
//                                    val gridWOLines = gLineRemover.removeGridLines(gridMat, lines)
//
//                                    Core.bitwise_not(gridWOLines, gridWOLines)
//
//                                    val cells = CellExtractor().splitBitmap(gridWOLines, 9, 9)
//
//                                    for (i in 0..8) {
//                                        for (j in 0..8) {
//                                            val cell = cells[i][j]
////                                            Utils.bitmapToMat(cell, tempMat)
//                                            var tempBitmap = Bitmap.createBitmap(cell.width(), cell.height(), Bitmap.Config.ARGB_8888)
//                                            Utils.matToBitmap(cell, tempBitmap)
//
//                                            val avgPix = Core.mean(cell).`val`[0]
//                                            var byteBuffer = ByteBuffer.allocateDirect(4* 28*28*1)
//
//                                            val resizedPic = Bitmap.createScaledBitmap(tempBitmap, 28, 28, true)
//                                            resizedPic.copyPixelsToBuffer(byteBuffer)
////                        GlobalScope.launch {
////                            resizedPic.compress(Bitmap.CompressFormat.PNG, 100, out)
////                        }
//                                            //TODO Save individual cells into a folder for the scanned board
//                                            tImage.load(resizedPic)
//
//                                            tflite.run(convertBitmapToByteBuffer(resizedPic), output)
//
//                                            val result = output[0]
//
//                                            val maxConfidence = result.max()
//                                            val prediction = maxConfidence?.let { it1 -> result.indexOf(it1) }
//                                            val isCellEmpty = avgPix > 250
//
//                                            if (prediction != null && !isCellEmpty) {
//                                                sudokuBoardTest.cells[i][j].value = prediction
//                                                sudokuBoardTest.invalidate()
//                                            }
//
//                                            println("Prediction: $prediction, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${isCellEmpty}, PixVal: $avgPix")
//                                        }
//                                    }
//                                }
////                                var tempBit = Bitmap.createBitmap(it.height, it.width, Bitmap.Config.ARGB_8888)
////                                Utils.matToBitmap(mat, tempBit)
//////
////                                var dirFile = File(requireActivity().getExternalFilesDir(null).toString())
////                                var noFiles = dirFile.listFiles().size
////                                var out = FileOutputStream(requireActivity().getExternalFilesDir(null).toString() + "/aaa" + noFiles.toString() + ".png")
////                                tempBit.compress(Bitmap.CompressFormat.PNG, 100, out)
//
////                                var image = Yuv.rgb(it)
////                                Core.rotate(image, image, Core.ROTATE_90_CLOCKWISE)
//
////                                val processedImage = GlobalScope.async {
////                                    val imgprocessor = ImageProcessor()
////                                    val processedMat = imgprocessor.processImage(mat)
////
////                                    val gridExtractor = GridExtractor()
////                                    val gridMat = gridExtractor.contourGridExtract(processedMat)
////
////                                    val gLineRemover = GridlinesRemover()
////                                    val lines = Mat()
////                                    Imgproc.HoughLinesP(gridMat, lines, 1.0, Math.PI / 180, 150, 200.0, 25.0)
////                                    val gridWOLines = gLineRemover.removeGridLines(gridMat, lines)
////
////                                    Core.bitwise_not(gridWOLines, gridWOLines)
////
////                                    gridWOLines
////                                }
////
////                                GlobalScope.launch {
////                                    val processedBoard = processedImage.await()
////
////                                    val cells = CellExtractor().splitBitmap(processedBoard, 9, 9)
////
////                                    for (i in 0..8) {
////                                        for (j in 0..8) {
////                                            val cell = cells[i][j]
//////                                            Utils.bitmapToMat(cell, tempMat)
////                                            var tempBitmap = Bitmap.createBitmap(cell.width(), cell.height(), Bitmap.Config.ARGB_8888)
////                                            Utils.matToBitmap(cell, tempBitmap)
////
////                                            val avgPix = Core.mean(cell).`val`[0]
////                                            var byteBuffer = ByteBuffer.allocateDirect(4* 28*28*1)
////
////                                            val resizedPic = Bitmap.createScaledBitmap(tempBitmap, 28, 28, true)
////                                            resizedPic.copyPixelsToBuffer(byteBuffer)
//////                        GlobalScope.launch {
//////                            resizedPic.compress(Bitmap.CompressFormat.PNG, 100, out)
//////                        }
////                                            //TODO Save individual cells into a folder for the scanned board
////                                            tImage.load(resizedPic)
////
////                                            tflite.run(convertBitmapToByteBuffer(resizedPic), output)
////
////                                            val result = output[0]
////
////                                            val maxConfidence = result.max()
////                                            val prediction = maxConfidence?.let { it1 -> result.indexOf(it1) }
////                                            val isCellEmpty = avgPix > 250
////
////                                            if (prediction != null && !isCellEmpty) {
////                                                sudokuBoardTest.cells[i][j].value = prediction
////                                                sudokuBoardTest.invalidate()
////                                            }
////
////                                            println("Prediction: $prediction, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${isCellEmpty}, PixVal: $avgPix")
////                                        }
////                                    }
////
////                                }
//                                it.close()
//
////                                var tempBit = Bitmap.createBitmap(it.height, it.width, Bitmap.Config.ARGB_8888)
////                                Utils.matToBitmap(image, tempBit)
//////
////                                var dirFile = File(requireActivity().getExternalFilesDir(null).toString())
////                                var noFiles = dirFile.listFiles().size
////                                var out = FileOutputStream(requireActivity().getExternalFilesDir(null).toString() + "/aaa" + noFiles.toString() + ".png")
////                                tempBit.compress(Bitmap.CompressFormat.PNG, 100, out)
////                                it.close()
//                            }
//                        }, null)
//
//                        var surfaceTexture : SurfaceTexture = cameraView.surfaceTexture
//                        surfaceTexture.setDefaultBufferSize(frameWidth, frameHeight)
//                        Log.d(TAG, "Camera Dimensions: $frameWidth $frameHeight")
//                        var previewSurface = Surface(surfaceTexture)
//
//                        cameraView.layoutParams = FrameLayout.LayoutParams(
//                            width, width, Gravity.CENTER
//                        )
//
//                        val captureCallback = object : CameraCaptureSession.StateCallback()
//                        {
//                            override fun onConfigureFailed(session: CameraCaptureSession) {
//                                session.close()
//                            }
//
//                            override fun onConfigured(session: CameraCaptureSession) {
//                                mCaptureSession = session
//                                // session configured
//                                val previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//                                    .apply {
//                                        addTarget(previewSurface)
//                                        addTarget(imageReader.surface)
//                                        set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY)
////                                        set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range<Int>(15,15))
//                                    }
//
//                                mCaptureSession.setRepeatingRequest(
//                                    previewRequestBuilder.build(),
//                                    object: CameraCaptureSession.CaptureCallback() {},
//                                    Handler { true }
//                                )
//                            }
//                        }
//                        mCameraDevice.createCaptureSession(mutableListOf(previewSurface, imageReader.surface), captureCallback, Handler { true })
//                    }
//                }
//            }
//        }, Handler { true })
//    }

    private fun areDimensionsSwapped(displayRotation: Int, cameraCharacteristics: CameraCharacteristics): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                // invalid display rotation
            }
        }
        return swappedDimensions
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
//        cameraView.surfaceTextureListener = surfaceTextureListener
//        startCameraSession()
    }

    override fun onStop() {
        super.onStop()
        closeCamera()
        closeBackgroundThread()
    }

    private fun closeCamera() {
//        mCameraDevice.close()
//        mCaptureSession.close()
        Log.wtf(TAG, "closeCamera")
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {
        cameraView.disableView()

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {        
        val ogMat = inputFrame!!.rgba()
        val centrePoint = Point(ogMat.size().width / 2, ogMat.size().height / 2)
        Imgproc.cvtColor(ogMat, ogMat, Imgproc.COLOR_RGBA2GRAY)

        Log.d(TAG, "onCameraFrame: CENTRE COORD (${centrePoint.x}, ${centrePoint.y})")

        var blurMat = Mat()
        Imgproc.GaussianBlur(ogMat, blurMat, Size(9.0, 9.0), 0.0)

        var threshMat = Mat()
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 2.0)

        var contours : ArrayList<MatOfPoint> = ArrayList(0)
        Imgproc.findContours(threshMat, contours, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        val ogArea = ogMat.size().width * ogMat.size().height
        val minArea = ogArea * 0.04
        val maxArea = ogArea * 0.3
        Log.d(TAG, "onCameraFrame: $minArea $maxArea ${contours.size}")
        Imgproc.cvtColor(ogMat, ogMat, Imgproc.COLOR_GRAY2RGBA)

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

        Imgproc.drawMarker(ogMat, Point(centrePoint.x, centrePoint.y), Scalar(204.0, 193.0, 90.0),Imgproc.MARKER_CROSS, 20,3)
        Imgproc.drawContours(ogMat, realContours, indexOfBoard, Scalar(255.0, 0.0, 255.0),3)

        try {
            val boundingRect = Imgproc.boundingRect(realContours[indexOfBoard])
            sudokuBoardMat = Mat(threshMat, Rect((boundingRect.x - 10), (boundingRect.y - 10), (boundingRect.width + 20), (boundingRect.height + 20)))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        blurMat.release()
        threshMat.release()

        return ogMat
    }
}