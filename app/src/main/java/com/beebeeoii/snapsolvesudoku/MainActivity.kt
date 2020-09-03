package com.beebeeoii.snapsolvesudoku

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hosseiniseyro.apprating.AppRatingDialog
import com.hosseiniseyro.apprating.listener.RatingDialogListener
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), RatingDialogListener {

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                }
                else -> super.onManagerConnected(status)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buildRatingDialog().monitor()
        buildRatingDialog().showRateDialogIfMeetsConditions()
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private fun buildRatingDialog(): AppRatingDialog {
        return AppRatingDialog.Builder()
            .setPositiveButtonText("Submit")
            .setNegativeButtonText("Cancel")
            .setNeutralButtonText("Later")
            .setNoteDescriptions(listOf("Very Bad", "Not good", "Acceptable", "Very Good", "Excellent"))
            .setDefaultRating(4)
            .setThreshold(4)
            .setAfterInstallDay(2)
            .setNumberOfLaunches(3)
            .setRemindInterval(2)
            .setTitle("How is the OCR accuracy?")
            .setDescription("Please rate and give your feedback")
            .setStarColor(R.color.colorPrimaryLight)
            .setNoteDescriptionTextColor(R.color.colorPrimaryDark)
            .setTitleTextColor(R.color.colorPrimaryDark)
            .setDescriptionTextColor(R.color.colorPrimaryDark)
            .setCommentTextColor(R.color.colorPrimaryDark)
            .setCommentBackgroundColor(R.color.darkerWhite)
            .setDialogBackgroundColor(R.color.white)
            .setHint("Input comments here...")
            .setHintTextColor(R.color.colorPrimaryDark)
            .setCancelable(false)
            .setCanceledOnTouchOutside(false)
            .create(this)
    }

    override fun onNegativeButtonClicked() {
    }

    override fun onNeutralButtonClicked() {
    }

    override fun onPositiveButtonClickedWithComment(rate: Int, comment: String) {
        val firestore = Firebase.firestore
        val feature = hashMapOf(
            "rating" to rate,
            "comments" to comment)
        firestore.collection(getString(R.string.firebase_collection_reviews))
            .add(feature)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you for your review!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error encountered", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onPositiveButtonClickedWithoutComment(rate: Int) {
        val firestore = Firebase.firestore
        val feature = hashMapOf(
            "rating" to rate,
            "comments" to null)
        firestore.collection(getString(R.string.firebase_collection_reviews))
            .add(feature)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you for your review!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error encountered", Toast.LENGTH_SHORT).show()
            }
    }
}