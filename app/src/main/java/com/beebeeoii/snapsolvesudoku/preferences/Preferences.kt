package com.beebeeoii.snapsolvesudoku.preferences

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.fragments.SettingsFragmentDirections
import com.beebeeoii.snapsolvesudoku.utils.DateTimeGenerator
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

        val updateModelButton: Preference? = findPreference("modelUpdate")
        val rateButton: Preference? = findPreference("rate")
        val feedbackButton: Preference? = findPreference("feedback")
        val donateButton: Preference? = findPreference("donate")

        updateModelButton?.setOnPreferenceClickListener {
            val dateTimeObjects = mutableListOf<LocalDateTime>()
            val modelFileDir = File("${requireActivity().getExternalFilesDir(null).toString()}/model")
            val modelFileName = modelFileDir.list()[0]
            val modelDateTimeCreated = DateTimeGenerator.getDateTimeObjectFromDateTimeString(modelFileName)

            var modelHasUpdate: Boolean

            val viewGroup = requireActivity().findViewById<ViewGroup>(android.R.id.content)
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_update_model, viewGroup, false)

            val firebaseStorage = Firebase.storage
            val storageReference = firebaseStorage.reference
            storageReference.child("models").listAll()
                .addOnSuccessListener { listResult ->
                    listResult.items.forEach { item ->
                        dateTimeObjects.add(DateTimeGenerator.getDateTimeObjectFromDateTimeString(item.name))
                    }

                    modelHasUpdate = dateTimeObjects.maxOrNull()?.isAfter(modelDateTimeCreated)!!

                    val progressBar = dialogView.findViewById<ProgressBar>(R.id.updateModelLoading)
                    val statusText = dialogView.findViewById<MaterialTextView>(R.id.updateModelStatus)
                    val button = dialogView.findViewById<MaterialButton>(R.id.updateModelActionButton)

                    if (modelHasUpdate) {
                        statusText.text = "Update is available"
                        button.text = "Update"
                        progressBar.visibility = View.INVISIBLE
                        statusText.visibility = View.VISIBLE

                        button.setOnClickListener {
                            dialog.setCancelable(false)

                            progressBar.visibility = View.VISIBLE
                            statusText.visibility = View.INVISIBLE

                            val updatedModelName = dateTimeObjects.maxOrNull()
                                ?.let { it1 -> DateTimeGenerator.generateDateTimeString(it1, DateTimeGenerator.Mode.DATE_AND_TIME) }
                            val updatedModelReference = storageReference
                                .child("models/${updatedModelName}")
                            val localFile = File("${requireActivity().getExternalFilesDir(null).toString()}/model/$updatedModelName")

                            updatedModelReference.getFile(localFile)
                                .addOnProgressListener {
                                    progressBar.isIndeterminate = false
                                    progressBar.progress = (it.bytesTransferred / it.totalByteCount * 100).toInt()
                                }
                                .addOnSuccessListener {
                                    Toast.makeText(requireActivity(), "Updated", Toast.LENGTH_SHORT).show()
                                    deleteOutdatedModelFile(modelFileName)
                                    dialog.dismiss()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireActivity(), "Update failed. Please try again", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                        }
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

        rateButton?.setOnPreferenceClickListener {
            val packageName = requireActivity().packageName
            val rateIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            )
            rateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(rateIntent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                ))
            }

            true
        }

        feedbackButton?.setOnPreferenceClickListener {
            // TODO remove hardcode string
            val url = "https://github.com/Beebeeoii/snap-solve-sudoku/issues"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
            true
        }

        donateButton?.setOnPreferenceClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToDonateFragment()
            findNavController().navigate(action)
            true
        }
    }

    private var closeDialog: View.OnClickListener = View.OnClickListener {
        dialog.dismiss()
    }

    private fun deleteOutdatedModelFile(outdatedModelFileName: String) : Boolean {
        val dir = requireActivity().getExternalFilesDir(null).toString()
        return File("$dir/model/$outdatedModelFileName").delete()
    }
}