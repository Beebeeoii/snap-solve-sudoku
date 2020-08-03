package com.beebeeoii.snapsolvesudoku

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
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

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_about, container, false)

        constraintLayout = rootView.findViewById(R.id.aboutConstraintLayout)
        appBar = rootView.findViewById(R.id.appBar)
        developer = rootView.findViewById(R.id.aboutDeveloper)
        rate = rootView.findViewById(R.id.aboutRate)
        github = rootView.findViewById(R.id.aboutGithub)
        donate = rootView.findViewById(R.id.aboutDonate)
        share = rootView.findViewById(R.id.aboutShare)
        reportBug = rootView.findViewById(R.id.aboutReport)
        featureRequest = rootView.findViewById(R.id.aboutFeature)
        faq = rootView.findViewById(R.id.aboutFAQ)
        changelog = rootView.findViewById(R.id.aboutChangelog)
        upcomingFeatures = rootView.findViewById(R.id.aboutUpcomingFeatures)
        libraries = rootView.findViewById(R.id.aboutLibraries)

        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        developer.setOnClickListener {
            FinestWebView.Builder(requireActivity()).show("https://github.com/Beebeeoii")
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
                .show("https://github.com/Beebeeoii/SnapSolveSudoku")
        }

        donate.setOnClickListener {

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
            IssueReporterLauncher.forTarget("Beebeeoii", "SnapSolveSudoku")
                .theme(R.style.Theme_IssueReporter_Light)
                .guestToken("0e6959e1ba556a92cc3ae04994d2ac6f4b0af85b")
                .guestEmailRequired(false)
                .minDescriptionLength(10)
                .homeAsUpEnabled(true)
                .launch(requireContext())
        }

        featureRequest.setOnClickListener {

        }

        faq.setOnClickListener {
            FinestWebView.Builder(requireActivity()).show("https://github.com/Beebeeoii/SnapSolveSudoku/blob/v2/FAQ.md")
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

        }

        libraries.setOnClickListener {
            LibsBuilder()
                .withAboutAppName("Snap Solve Sudoku")
                .withAboutDescription("A big thank you to all OSS developers!")
                .withAboutMinimalDesign(false)
                .start(requireContext())
        }

        return rootView
    }
}