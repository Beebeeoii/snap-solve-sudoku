package com.example.snapsolvesudoku

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
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
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.snapsolvesudoku.image.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var cameraView :TextureView
    private lateinit var button : Button
    private lateinit var imageView : ImageView
    private lateinit var textViewOutput : TextView

    private lateinit var surfaceTextureListener : TextureView.SurfaceTextureListener
    private lateinit var backgroundThread : HandlerThread
    private lateinit var backgroundHandler : Handler

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        cameraView = findViewById(R.id.cameraView)
        button = findViewById(R.id.openCamera)
        imageView = findViewById(R.id.imageView)
        textViewOutput = findViewById(R.id.output)

        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                Log.wtf(TAG, "onSurfaceTextureSizeChanged: Size changed")
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                //TODO("Not yet implemented")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                return false
            }

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {

            }
        }

        copyTessData()

        button.setOnClickListener {
            val pic = cameraView.bitmap

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

            val cells = cellExtractor.splitBitmap(pic, 9, 9)
            val rows = cellExtractor.splitIntoRows(pic, 9)
            val cols = cellExtractor.splitIntoCols(pic, 9)

            var ocr = DigitRecogniser(getExternalFilesDir(null).toString())
            var output = ""
//            var result = ocr.getDigits(pic)
//            print(result)
//            println("ROWS")
//            for (i in 0..8) {
//                output += ocr.getDigits((rows[i])) + "\n"
////                var result = ocr.getDigits(rows[i])
////                println(result)
//            }
//

//            println("COLS")
//            for (i in 0..8) {
//                var result = ocr.getDigits(cols[i])
//                println(result)
//            }
            var text = ""

            for (i in 0..8) {
                for (j in 0..8) {
                    var pic = cells[i][j]
                    var tempMat = Mat()
                    Utils.bitmapToMat(pic, tempMat)

                    var avgPix = Core.mean(tempMat).`val`[0]

                    val resizedPic = Bitmap.createScaledBitmap(pic, 28, 28, true)
                    var byteBuffer = ByteBuffer.allocateDirect(4* 28*28*1)
                    resizedPic.copyPixelsToBuffer(byteBuffer)

                    var tImage = TensorImage(DataType.FLOAT32)

                    tImage.load(resizedPic)

                    val output = Array(1){FloatArray(10)}

                    val tflite = Interpreter(File(getExternalFilesDir(null).toString() + "/tessdata/converted_model.tflite"))
                    tflite.run(convertBitmapToByteBuffer(resizedPic), output)
                    tflite.close()

                    val result = output[0]
                    for (x in result) {
                        print("$x ")
                    }
                    println()

                    val maxConfidence = result.max()
                    val number = maxConfidence?.let { it1 -> result.indexOf(it1) }
                    val resultString = "Prediction: $number, Confidence: ${maxConfidence}, Cell: $i $j, isEmpty: ${avgPix > 250}, PixVal: $avgPix"
                    println(resultString)

                    var dirFile = File(getExternalFilesDir(null).toString())
                    var noFiles = dirFile.listFiles().size
                    var out : FileOutputStream = FileOutputStream(getExternalFilesDir(null).toString() + "/pic" + noFiles.toString() + ".png")
                    resizedPic.compress(Bitmap.CompressFormat.PNG, 100, out)



                    text += if (avgPix < 250) {
                        number.toString()
                    } else {
                        "_"
                    }

                    text += " "
//
//                    var result = ocr.getDigits(cells[i][j])
//                    if (result != "") {
//                        print("$result ")
//                        println(probBuffer.intArray[0])
//
//                    }

//                    if (result != "") {
//                        output += result
//                        print(result)
//                    } else {
//                        output == "_"
//                        print("_")
//                    }
//                    output += " "
//                    print(" ")

                }
                text += "\n"
//                output += "\n"
//                println()
            }
            textViewOutput.text = text

            imageView.setImageBitmap(pic)
        }

        textViewOutput.setOnClickListener {
            imageView.setImageBitmap(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            var photo : Bitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(photo)
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


    private fun loadModelFile (activity : Activity) : MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = activity.assets.openFd("converted_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun startCameraSession() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (cameraManager.cameraIdList.isEmpty()) {
            // no cameras
            return
        }

        val firstCamera = cameraManager.cameraIdList[0]
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
            override fun onDisconnected(p0: CameraDevice) { }
            override fun onError(p0: CameraDevice, p1: Int) { }

            override fun onOpened(cameraDevice: CameraDevice) {
                // use the camera
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)

                cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
                    streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)?.let { yuvSizes ->
                        var previewSize = yuvSizes.last()
                        val displayMetrics = DisplayMetrics()
                        windowManager.defaultDisplay.getMetrics(displayMetrics)
                        val width: Int = displayMetrics.widthPixels
                        println(width)
                        for (size in yuvSizes) {

                            if (size.height == size.width && size.width <= width) {
                                previewSize = size
                                Log.d(TAG, "onOpened: " + size.width  + " " + size.height)
                                break
                            }
                        }

                        var fixedWidth = (width / previewSize.width)

                        // cont.
                        val displayRotation = windowManager.defaultDisplay.rotation
                        val swappedDimensions = areDimensionsSwapped(displayRotation, cameraCharacteristics)
                        // swap width and height if needed
                        val rotatedPreviewWidth = if (swappedDimensions) previewSize.height else previewSize.width
                        val rotatedPreviewHeight = if (swappedDimensions) previewSize.width else previewSize.height

                        frameWidth = rotatedPreviewWidth * fixedWidth
                        frameHeight = rotatedPreviewHeight * fixedWidth

                        var surfaceTexture : SurfaceTexture = cameraView.surfaceTexture
                        surfaceTexture.setDefaultBufferSize(frameWidth, frameHeight)
                        Log.d(TAG, "onOpened: $frameWidth $frameHeight")
                        var previewSurface : Surface = Surface(surfaceTexture)
                        cameraView.layoutParams = FrameLayout.LayoutParams(
                            frameWidth, frameHeight, Gravity.CENTER
                        )

                        val captureCallback = object : CameraCaptureSession.StateCallback()
                        {
                            override fun onConfigureFailed(session: CameraCaptureSession) {}

                            override fun onConfigured(session: CameraCaptureSession) {
                                // session configured
                                val previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                    .apply {
                                        addTarget(previewSurface)
                                    }

                                previewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range<Int>(15,15))
                                session.setRepeatingRequest(
                                    previewRequestBuilder.build(),
                                    object: CameraCaptureSession.CaptureCallback() {},
                                    Handler { true }
                                )
                            }
                        }
                        cameraDevice.createCaptureSession(mutableListOf(previewSurface), captureCallback, Handler { true })
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

    private fun copyTessData() {
        try {
            val dir : File = File(getExternalFilesDir(null).toString() + "/tessdata")

            if (!dir.exists()) {
                dir.mkdirs()
            }

            val pathToDataFile = getExternalFilesDir(null).toString() + "/tessdata" + "/engbest.traineddata"
            if (!File(pathToDataFile).exists()) {
                val `in`: InputStream = assets.open("engbest.traineddata")
                val out: OutputStream = FileOutputStream(pathToDataFile)
                val buffer = ByteArray(1024)
                var read: Int = `in`.read(buffer)
                while (read != -1) {
                    out.write(buffer, 0, read)
                    read = `in`.read(buffer)
                }
                `in`.close()
                out.flush()
                out.close()
            }
        } catch (exception : Exception) {
            exception.printStackTrace()
        }
    }

    private fun openBackgroundThread() {
        backgroundThread = HandlerThread("cameraBackgroundThread")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.getLooper())
    }

    private fun closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
        }
    }

    private fun closeCamera() {

    }

    override fun onResume() {
        super.onResume()
        openBackgroundThread()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        if (cameraView.isAvailable) {
            cameraView.surfaceTextureListener = surfaceTextureListener
            startCameraSession()
        } else {
            cameraView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onStop() {
        super.onStop()
        closeCamera()
        closeBackgroundThread()
    }
}