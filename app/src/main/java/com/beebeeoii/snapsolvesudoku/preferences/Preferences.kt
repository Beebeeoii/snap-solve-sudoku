package com.beebeeoii.snapsolvesudoku.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.beebeeoii.snapsolvesudoku.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.time.LocalDateTime

private lateinit var dialog: AlertDialog

class Preferences : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        val dateTimeObjects = mutableListOf<LocalDateTime>()
        val modelFileDir = File("${requireActivity().getExternalFilesDir(null).toString()}/model")
        val modelDateTimeCreated = DateTimeGenerator.getDateTimeObjectFromString(modelFileDir.list()[0])

        val updateModelButton: Preference? = findPreference("modelUpdate")
        updateModelButton?.setOnPreferenceClickListener {
            var modelHasUpdate: Boolean

            val viewGroup = requireActivity().findViewById<ViewGroup>(android.R.id.content)
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_model, viewGroup, false)

            val firebaseStorage = Firebase.storage
            val storageReference = firebaseStorage.reference
            storageReference.child("models").listAll()
                .addOnSuccessListener { listResult ->
                    listResult.items.forEach { item ->
                        dateTimeObjects.add(DateTimeGenerator.getDateTimeObjectFromString(item.name))
                    }

                    modelHasUpdate = dateTimeObjects.max()?.isAfter(modelDateTimeCreated)!!

                    val progressBar = dialogView.findViewById<ProgressBar>(R.id.updateModelLoading)
                    val statusText = dialogView.findViewById<MaterialTextView>(R.id.updateModelStatus)
                    val button = dialogView.findViewById<MaterialButton>(R.id.updateModelActionButton)

                    if (modelHasUpdate) {
                        statusText.text = "Update is available"
                        button.text = "Update"

                        button.setOnClickListener(updateModel)

                        progressBar.visibility = View.INVISIBLE
                        statusText.visibility = View.VISIBLE
                    } else {
                        progressBar.visibility = View.INVISIBLE
                        statusText.visibility = View.VISIBLE

                        button.setOnClickListener(closeDialog)
                    }
                }

            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setView(dialogView)
            dialog = dialogBuilder.create()
            dialog.show()

            true
        }
    }

    private var updateModel: View.OnClickListener = View.OnClickListener {
        Toast.makeText(requireActivity(), "Updated", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

    private var closeDialog: View.OnClickListener = View.OnClickListener {
        dialog.dismiss()
    }
}