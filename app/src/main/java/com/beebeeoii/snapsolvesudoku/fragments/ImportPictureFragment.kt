package com.beebeeoii.snapsolvesudoku.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.beebeeoii.snapsolvesudoku.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.DigitRecogniser
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.UniqueIdGenerator
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
import com.beebeeoii.snapsolvesudoku.fragments.ImportPictureFragmentDirections
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File

private const val TAG = "ImportPictureFragment"

class ImportPictureFragment : BottomSheetDialogFragment() {

    private val RETRIEVE_PICTURE_CODE = 1

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.dialog_fragment_bottom_sheet, container, false)

        val openCameraButton: MaterialButton = view.findViewById(R.id.openCamera)
        val openGalleryButton: MaterialButton = view.findViewById(R.id.openGallery)

        openCameraButton.setOnClickListener {
            val action =
                ImportPictureFragmentDirections.actionImportPictureFragmentToCameraFragment()
            findNavController().navigate(action)
        }

        openGalleryButton.setOnClickListener {
            val retrieveImagesIntent = Intent()
            retrieveImagesIntent.type = "image/*"
            retrieveImagesIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(retrieveImagesIntent, "Select sudoku board image"), RETRIEVE_PICTURE_CODE)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RETRIEVE_PICTURE_CODE -> {
                val selectedImage = data?.data

                if (selectedImage != null) {
                    CropImage.activity(selectedImage)
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAllowFlipping(false)
                        .setAllowRotation(false)
                        .setAllowCounterRotation(false)
                        .setBorderLineColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
                        .setBorderCornerColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
                        .setGuidelinesColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark))
                        .start(requireContext(), this)
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val croppedImage = CropImage.getActivityResult(data)

                if (resultCode == Activity.RESULT_OK) {
                    val croppedImageURI = croppedImage.uri
                    val croppedImagePath = croppedImageURI.path
                    if (croppedImagePath != null) {
                        val croppedSudokuBoardBitmap = BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(croppedImageURI))
                        val sudokuBoardMat = Mat()
                        Utils.bitmapToMat(croppedSudokuBoardBitmap, sudokuBoardMat)

                        Imgproc.cvtColor(sudokuBoardMat, sudokuBoardMat, Imgproc.COLOR_BGR2GRAY, 1)
                        val digitRecogniser =
                            DigitRecogniser(
                                requireActivity(),
                                sudokuBoardMat
                            )
                        val sudokuBoardBitmap = GlobalScope.async {
                            digitRecogniser.processBoard(false)
                        }
                        GlobalScope.launch {
                            val uniqueId = UniqueIdGenerator.generateId().uniqueId
                            val boardDirPath = "${requireActivity().getExternalFilesDir(null).toString()}/${uniqueId}"
                            val boardDirFile = File(boardDirPath)
                            if (!boardDirFile.exists()) {
                                boardDirFile.mkdir()
                            }

                            val database = Database.invoke(requireContext())
                            val historyDao = database.getHistoryDao()
                            CoroutineScope(Dispatchers.IO).launch {
                                historyDao.insertHistoryEntry(
                                    HistoryEntity(
                                        uniqueId = uniqueId,
                                        dateTime = DateTimeGenerator.generateDateTime(
                                            DateTimeGenerator.DATE_AND_TIME
                                        ),
                                        folderPath = boardDirPath,
                                        originalPicturePath = croppedImagePath
                                    )
                                )
                            }

                            digitRecogniser.recogniseDigits(sudokuBoardBitmap.await())
                            val action =
                                ImportPictureFragmentDirections.actionImportPictureFragmentToMainFragment(
                                    digitRecogniser.sudokuBoard2DIntArray
                                )
                            findNavController().navigate(action)
                        }
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    //TODO Error handling
                    val error = croppedImage.error
                }
            }
        }
    }
}