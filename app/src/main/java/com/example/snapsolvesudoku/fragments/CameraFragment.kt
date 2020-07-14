package com.example.snapsolvesudoku.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.util.Range
import android.view.*
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.snapsolvesudoku.R
import com.example.snapsolvesudoku.SudokuBoard2DIntArray
import com.example.snapsolvesudoku.image.CellExtractor
import com.example.snapsolvesudoku.image.GridExtractor
import com.example.snapsolvesudoku.image.GridlinesRemover
import com.example.snapsolvesudoku.image.ImageProcessor
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "CameraFragment"

private lateinit var cameraView : TextureView
private lateinit var mCameraDevice: CameraDevice
private lateinit var mCaptureSession: CameraCaptureSession
private lateinit var surfaceTextureListener : TextureView.SurfaceTextureListener
private lateinit var backgroundThread : HandlerThread
private lateinit var backgroundHandler : Handler

private lateinit var captureButton: MaterialButton

class CameraFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        cameraView = view.findViewById(R.id.cameraView)
        captureButton = view.findViewById(R.id.captureButton)

        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                Log.wtf(TAG, "onSurfaceTextureSizeChanged: Size changed")
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                //TODO("Not yet implemented")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return true
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {

            }
        }

        captureButton.setOnClickListener {
            val pic = cameraView.bitmap
            val sudokuBoard2DIntArray = SudokuBoard2DIntArray()

            val processedImage = GlobalScope.async {
                val imgprocessor = ImageProcessor()
                val processedMat = imgprocessor.processImage(pic)

                val gridExtractor = GridExtractor()
                val gridMat = gridExtractor.contourGridExtract(processedMat)

                val gLineRemover = GridlinesRemover()
                val lines = Mat()
                Imgproc.HoughLinesP(gridMat, lines, 1.0, Math.PI / 180, 150, 200.0, 25.0)
                val gridWOLines = gLineRemover.removeGridLines(gridMat, lines)

                val cellExtractor = CellExtractor()
                Core.bitwise_not(gridWOLines, gridWOLines)
                Utils.matToBitmap(gridWOLines, pic)

                cellExtractor.splitBitmap(pic, 9, 9)
            }

            GlobalScope.launch {
                val tflite = Interpreter(File(requireActivity().getExternalFilesDir(null).toString() + "/model/model.tflite"))
                var tImage = TensorImage(DataType.FLOAT32)
                var tempMat = Mat()

                val output = Array(1){FloatArray(10)}

                var dirFile = File(requireActivity().getExternalFilesDir(null).toString())
                var noFiles = dirFile.listFiles().size
                var out = FileOutputStream(requireActivity().getExternalFilesDir(null).toString() + "/pic" + noFiles.toString() + ".png")
                pic.compress(Bitmap.CompressFormat.PNG, 100, out)

                for (i in 0..8) {
                    for (j in 0..8) {
                        val pic = processedImage.await()[i][j]

                        Utils.bitmapToMat(pic, tempMat)

                        val avgPix = Core.mean(tempMat).`val`[0]
                        var byteBuffer = ByteBuffer.allocateDirect(4* 28*28*1)

                        val resizedPic = Bitmap.createScaledBitmap(pic, 28, 28, true)
                        resizedPic.copyPixelsToBuffer(byteBuffer)
//                        GlobalScope.launch {
//                            resizedPic.compress(Bitmap.CompressFormat.PNG, 100, out)
//                        }
                        tImage.load(resizedPic)

                        tflite.run(convertBitmapToByteBuffer(resizedPic), output)

                        val result = output[0]

                        val maxConfidence = result.max()
                        val prediction = maxConfidence?.let { it1 -> result.indexOf(it1) }

                        if (prediction != null) {
                            sudokuBoard2DIntArray.board2DIntArray[i][j] = prediction
                        }

                        println("Prediction: $prediction, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${avgPix > 250}, PixVal: $avgPix")
                    }
                }

                tflite.close()

                val action = CameraFragmentDirections.actionCameraFragmentToMainFragment(sudokuBoard2DIntArray)
                findNavController().navigate(action)
            }


        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CameraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CameraFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
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

    private fun startCameraSession() {
        val cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (cameraManager.cameraIdList.isEmpty()) {
            // no cameras
            return
        }

        val firstCamera = cameraManager.cameraIdList[0]
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        cameraManager.openCamera(firstCamera, object: CameraDevice.StateCallback() {
            var frameWidth = 0
            var frameHeight = 0
            override fun onDisconnected(p0: CameraDevice) {
                Log.wtf(TAG, "Camera Closed")
                p0.close()
            }
            override fun onError(p0: CameraDevice, p1: Int) {
                Log.wtf(TAG, "Camera Closed")
                p0.close()
            }

            override fun onOpened(cameraDevice: CameraDevice) {
                mCameraDevice = cameraDevice

                // use the camera
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraDevice.id)

                cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
                    streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)?.let { yuvSizes ->
                        val displayMetrics = DisplayMetrics()
                        val windowManager = requireActivity().windowManager

                        windowManager.defaultDisplay.getMetrics(displayMetrics)
                        val width: Int = displayMetrics.widthPixels
                        println(width)
                        yuvSizes.sortByDescending {
                            it.width.toFloat() * it.height.toFloat()
                        }
                        yuvSizes.sortBy {
                            it.width.toFloat() / it.height.toFloat()
                        }
                        var previewSize = yuvSizes.first()
                        var sizesString = ""

                        for (size in yuvSizes) {
                            Log.d(TAG, "Camera Size Available: ${size.width} x ${size.height} - Ratio: ${size.width.toFloat() / size.height.toFloat()}")
                            sizesString += "${size.width} x ${size.height} - Ratio: ${size.width.toFloat() / size.height.toFloat()}\n"
                        }

                        // cont.
                        val displayRotation = windowManager.defaultDisplay.rotation
                        val swappedDimensions = areDimensionsSwapped(displayRotation, cameraCharacteristics)
                        // swap width and height if needed
                        val rotatedPreviewWidth = if (swappedDimensions) previewSize.height else previewSize.width
                        val rotatedPreviewHeight = if (swappedDimensions) previewSize.width else previewSize.height

                        Log.d(TAG, "Camera Dimensions After adjustment: $rotatedPreviewWidth $rotatedPreviewHeight")
                        frameWidth = rotatedPreviewWidth
                        frameHeight = rotatedPreviewHeight

                        var surfaceTexture : SurfaceTexture = cameraView.surfaceTexture
                        surfaceTexture.setDefaultBufferSize(frameWidth, frameHeight)
                        Log.d(TAG, "Camera Dimensions: $frameWidth $frameHeight")
                        var previewSurface : Surface = Surface(surfaceTexture)

                        cameraView.layoutParams = FrameLayout.LayoutParams(
                            width, width, Gravity.CENTER
                        )

                        val captureCallback = object : CameraCaptureSession.StateCallback()
                        {
                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                session.close()
                            }

                            override fun onConfigured(session: CameraCaptureSession) {
                                mCaptureSession = session
                                // session configured
                                val previewRequestBuilder = mCameraDevice.createCaptureRequest(
                                    CameraDevice.TEMPLATE_PREVIEW)
                                    .apply {
                                        addTarget(previewSurface)
                                    }

                                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range<Int>(15,15))
                                mCaptureSession.setRepeatingRequest(
                                    previewRequestBuilder.build(),
                                    object: CameraCaptureSession.CaptureCallback() {},
                                    Handler { true }
                                )
                            }
                        }
                        mCameraDevice.createCaptureSession(mutableListOf(previewSurface), captureCallback, Handler { true })
                    }
                }
            }
        }, Handler { true })
    }

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
        cameraView.surfaceTextureListener = surfaceTextureListener
        startCameraSession()
    }

    override fun onStop() {
        super.onStop()
        closeCamera()
        closeBackgroundThread()
    }

    private fun closeCamera() {
        mCameraDevice.close()
        mCaptureSession.close()
        Log.wtf(TAG, "closeCamera")
    }
}