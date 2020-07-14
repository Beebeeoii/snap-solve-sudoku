package com.example.snapsolvesudoku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.snapsolvesudoku.MainActivity
import com.example.snapsolvesudoku.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

private const val TAG = "ImportPictureFragment"

class ImportPictureFragment : BottomSheetDialogFragment() {
    fun newInstance(): ImportPictureFragment? {
        return ImportPictureFragment()
    }

    @Nullable
    override fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.dialog_fragment_bottom_sheet, container, false)

        val openCameraButton: MaterialButton = view.findViewById(R.id.openCamera)
        val openGalleryButton: MaterialButton = view.findViewById(R.id.openGallery)

        openCameraButton.setOnClickListener {
            this.dismiss()
            val navController = findNavController()
            val action = ImportPictureFragmentDirections.actionImportPictureFragmentToCameraFragment()
            navController.navigate(action)
        }

        openGalleryButton.setOnClickListener {
            this.dismiss()
        }

        return view
    }
}