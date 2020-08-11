package com.beebeeoii.snapsolvesudoku.fragments

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.beebeeoii.snapsolvesudoku.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.heinrichreimersoftware.androidissuereporter.IssueReporterLauncher
import com.michaelflisar.changelog.ChangelogBuilder
import com.mikepenz.aboutlibraries.LibsBuilder
import com.thefinestartist.finestwebview.FinestWebView

private lateinit var constraintLayout: ConstraintLayout
private lateinit var appBar: MaterialToolbar
private lateinit var developer: ConstraintLayout
private lateinit var rate: ConstraintLayout
private lateinit var github: ConstraintLayout
private lateinit var donate: ConstraintLayout
private lateinit var share: ConstraintLayout
private lateinit var reportBug: ConstraintLayout
private lateinit var featureRequest: ConstraintLayout
private lateinit var faq: ConstraintLayout
private lateinit var changelog: ConstraintLayout
private lateinit var upcomingFeatures: ConstraintLayout
private lateinit var libraries: ConstraintLayout
private const val TAG = "AboutFragment"

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_about, container, false)

        constraintLayout = rootView.findViewById(
            R.id.aboutConstraintLayout
        )
        appBar = rootView.findViewById(
            R.id.appBar
        )
        developer = rootView.findViewById(
            R.id.aboutDeveloper
        )
        rate = rootView.findViewById(
            R.id.aboutRate
        )
        github = rootView.findViewById(
            R.id.aboutGithub
        )
        donate = rootView.findViewById(
            R.id.aboutDonate
        )
        share = rootView.findViewById(
            R.id.aboutShare
        )
        reportBug = rootView.findViewById(
            R.id.aboutReport
        )
        featureRequest = rootView.findViewById(
            R.id.aboutFeature
        )
        faq = rootView.findViewById(
            R.id.aboutFAQ
        )
        changelog = rootView.findViewById(
            R.id.aboutChangelog
        )
        upcomingFeatures = rootView.findViewById(
            R.id.aboutUpcomingFeatures
        )
        libraries = rootView.findViewById(
            R.id.aboutLibraries
        )

        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        developer.setOnClickListener {
            FinestWebView.Builder(requireActivity()).show(getString(R.string.github_profile_page))
        }

        rate.setOnClickListener {
            val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireActivity().packageName}"))
            rateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(rateIntent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=${requireActivity().packageName}")))
            }
        }

        github.setOnClickListener {
            FinestWebView.Builder(requireActivity())
                .show(getString(R.string.github_project_main))
        }

        donate.setOnClickListener {
            val action = AboutFragmentDirections.actionAboutFragmentToDonateFragment()
            findNavController().navigate(action)
        }

        share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            val shareText = "[Snap Solve Sudoku]\n\n" +
                    "Hey check out this awesome OCR sudoku board solver now!\n\n" +
                    "http://play.google.com/store/apps/details?id=${requireActivity().packageName}"
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Snap Solve Sudoku")
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        reportBug.setOnClickListener {
            val gitHubReference = Firebase.firestore
                .collection(getString(R.string.firebase_collection_ENV_VAR))
                .document(getString(R.string.firebase_collection_ENV_VAR_document_github))
            gitHubReference.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        val gitHubToken = documentSnapshot.data!!["KEY"]

                        IssueReporterLauncher.forTarget(getString(R.string.dev_nick), "SnapSolveSudoku")
                            .theme(R.style.Theme_IssueReporter_Light)
                            .guestToken(gitHubToken as String?)
                            .guestEmailRequired(false)
                            .minDescriptionLength(10)
                            .homeAsUpEnabled(true)
                            .launch(requireContext())
                    }
                }
        }

        featureRequest.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val viewGroup = requireActivity().findViewById<ViewGroup>(android.R.id.content)
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feature_request, viewGroup, false)
            dialogBuilder.setView(dialogView)
            val dialog = dialogBuilder.create()
            dialog.show()

            val loadingBar = dialogView.findViewById<ProgressBar>(R.id.feature_dialog_loading_bar)
            val requestInput = dialogView.findViewById<TextInputEditText>(R.id.feature_dialog_input)
            val submitRequest = dialogView.findViewById<MaterialButton>(R.id.feature_dialog_submit_button)
            submitRequest.setOnClickListener {
                val firestore = Firebase.firestore
                val feature = hashMapOf("request" to requestInput.text.toString())
                firestore.collection(getString(R.string.firebase_collection_feature_requests))
                    .add(feature)
                    .addOnSuccessListener { documentReference ->
                        val snackbar = Snackbar.make(constraintLayout, "Request ID: ${documentReference.id}", Snackbar.LENGTH_LONG)
                        snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                        snackbar.setAction("Copy ID") {
                            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Request ID", documentReference.id))
                        }
                        snackbar.show()
                    }
                    .addOnFailureListener {e ->
                        Log.d(TAG, "onCreateView: $e")
                        val snackbar = Snackbar.make(constraintLayout, "Error encountered while sending request. Please try again.", Snackbar.LENGTH_SHORT)
                        snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                        snackbar.show()
                    }.addOnCompleteListener {
                        dialog.dismiss()
                    }

                requestInput.visibility = View.INVISIBLE
                submitRequest.visibility = View.INVISIBLE
                loadingBar.visibility = View.VISIBLE
                dialog.setCancelable(false)
            }
        }

        faq.setOnClickListener {
            FinestWebView.Builder(requireActivity()).show(getString(R.string.github_project_faq))
        }

        changelog.setOnClickListener {
            ChangelogBuilder()
                .withUseBulletList(true)
                .withOkButtonLabel("Close")
                .withSummary(true, true)
                .withTitle("Changelog")
                .buildAndShowDialog(requireActivity() as AppCompatActivity, false)
        }

        upcomingFeatures.setOnClickListener {
            FinestWebView.Builder(requireActivity()).show(getString(R.string.github_project_features))
        }

        libraries.setOnClickListener {
            LibsBuilder()
                .withAboutAppName(getString(R.string.app_name))
                .withAboutDescription("A big thank you to all OSS developers!")
                .withAboutMinimalDesign(false)
                .start(requireContext())
        }

        return rootView
    }
}