package com.beebeeoii.snapsolvesudoku.fragments

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
import com.beebeeoii.snapsolvesudoku.sudokuboard.SudokuBoard
import com.beebeeoii.snapsolvesudoku.sudokuboard.SudokuBoard2DIntArray
import com.beebeeoii.snapsolvesudoku.utils.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.utils.FileDeletor
import com.beebeeoii.snapsolvesudoku.utils.ShareBoardBitmap
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private lateinit var constraintLayout: ConstraintLayout
private lateinit var appBar: MaterialToolbar

private lateinit var boardPicture: AppCompatImageView
private lateinit var dayTextView: MaterialTextView
private lateinit var dateTextView: MaterialTextView

private lateinit var detailsSelection: ChipGroup
private lateinit var boardDetailChip: Chip
private lateinit var statisticsDetailChip: Chip
private lateinit var moreDetailsDetailChip: Chip

private lateinit var sudokuBoardDetailContainer: ConstraintLayout
private lateinit var detailsSudokuBoardView: SudokuBoard
private lateinit var previousBoard: MaterialButton
private lateinit var solutionCounterTextView: MaterialTextView
private lateinit var nextBoard: MaterialButton

private lateinit var statisticsDetailContainer: ConstraintLayout
private lateinit var statisticsNoHintsTextView: MaterialTextView
private lateinit var statisticsNoSolutionsTextView: MaterialTextView
private lateinit var statisticsTimeTakenToSolveTextView: MaterialTextView

private lateinit var moreDetailsContainer: ScrollView
private lateinit var uniqueIdContainer: LinearLayout
private lateinit var uniqueIdTextView: MaterialTextView
private lateinit var folderPathContainer: LinearLayout
private lateinit var folderPathTextView: MaterialTextView
private lateinit var originalPicturePathContainer: LinearLayout
private lateinit var originalPicturePathTextView: MaterialTextView
private lateinit var processedPicturePathContainer: LinearLayout
private lateinit var processedPicturePathTextView: MaterialTextView
private lateinit var solutionsPathContainer: LinearLayout
private lateinit var solutionsPathTextView: MaterialTextView

private const val TAG = "DetailsFragment"

class DetailsFragment : Fragment() {

    private val solution2DIntArrayList = mutableListOf<Array<IntArray>>()
    private var solutionCounter = 0
    private val givenDigitsIndices = mutableListOf<IntArray>()
    private lateinit var uniqueId: String
    private lateinit var historyEntity: HistoryEntity

    private var pictureShowingOriginal = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_details, container, false)

        constraintLayout = rootView.findViewById(R.id.detailsConstraintLayout)
        appBar = rootView.findViewById(R.id.appBar)
        boardPicture = rootView.findViewById(R.id.detailsPicture)
        dayTextView = rootView.findViewById(R.id.detailsDay)
        dateTextView = rootView.findViewById(R.id.detailsDate)

        detailsSelection = rootView.findViewById(R.id.detailsChooserChipGroup)
        boardDetailChip = rootView.findViewById(R.id.boardDetailChip)
        statisticsDetailChip = rootView.findViewById(R.id.statisticsDetailChip)
        moreDetailsDetailChip = rootView.findViewById(R.id.moreDetailsDetailChip)

        sudokuBoardDetailContainer = rootView.findViewById(R.id.detailsSudokuBoardContainer)
        detailsSudokuBoardView = rootView.findViewById(R.id.detailsSudokuBoard)
        previousBoard = rootView.findViewById(R.id.detailsPreviousBoard)
        solutionCounterTextView = rootView.findViewById(R.id.detailsBoardTracker)
        nextBoard = rootView.findViewById(R.id.detailsNextBoard)

        statisticsDetailContainer = rootView.findViewById(R.id.detailsStatisticsContainer)
        statisticsNoHintsTextView = rootView.findViewById(R.id.statisticsNoHints)
        statisticsNoSolutionsTextView = rootView.findViewById(R.id.statisticsNoSolutions)
        statisticsTimeTakenToSolveTextView = rootView.findViewById(R.id.statisticsTimeTakenToSolve)

        moreDetailsContainer = rootView.findViewById(R.id.detailsMoreDetailsContainer)
        uniqueIdContainer = rootView.findViewById(R.id.details_more_details_unique_id_container)
        uniqueIdTextView = rootView.findViewById(R.id.details_more_details_unique_id)
        folderPathContainer = rootView.findViewById(R.id.details_more_details_folder_path_container)
        folderPathTextView = rootView.findViewById(R.id.details_more_details_folder_path)
        originalPicturePathContainer = rootView.findViewById(R.id.details_more_details_original_picture_container)
        originalPicturePathTextView = rootView.findViewById(R.id.details_more_details_original_picture)
        processedPicturePathContainer = rootView.findViewById(R.id.details_more_details_processed_picture_container)
        processedPicturePathTextView = rootView.findViewById(R.id.details_more_details_processed_picture)
        solutionsPathContainer = rootView.findViewById(R.id.details_more_details_solutions_container)
        solutionsPathTextView = rootView.findViewById(R.id.details_more_details_solutions)

        if (arguments != null && !requireArguments().isEmpty) {
            uniqueId = DetailsFragmentArgs.fromBundle(requireArguments()).uniqueId

            detailsSudokuBoardView.uniqueId = uniqueId

            val database = Database.invoke(requireContext())
            val historyDao = database.getHistoryDao()

            historyDao.getSpecificEntry(uniqueId).observe(viewLifecycleOwner, Observer {
                historyEntity = it[0]
                if (it[0].originalPicturePath != null) {
                    boardPicture.setImageBitmap(BitmapFactory.decodeFile(it[0].originalPicturePath))
                }
                boardPicture.visibility = View.VISIBLE

                val dateTime = it[0].dateTime
                val dayOfWeek = DateTimeGenerator.getDayOfWeekFromDateTime(dateTime)
                val formattedDate = DateTimeGenerator.getFormattedDate(dateTime)
                dateTextView.text = formattedDate
                dayTextView.text = dayOfWeek

                val recognisedDigitsString = it[0].recognisedDigits
                if (recognisedDigitsString != null) {
                    for (i in recognisedDigitsString.indices) {
                        if (Character.getNumericValue(recognisedDigitsString[i]) != 0) {
                            givenDigitsIndices.add(intArrayOf(i/9, i%9))
                        }
                    }
                }

                val solutionTextFile = File(it[0].solutionsPath)
                val inputStream = solutionTextFile.inputStream()
                val solutionStringList = mutableListOf<String>()
                inputStream.bufferedReader().forEachLine {solutionString ->
                    solutionStringList.add(solutionString)
                }

                solutionStringList.forEach { solutionString ->
                    val board2DIntArray: Array<IntArray> = Array(9){IntArray(9){0}}
                    for (i in solutionString.indices) {
                        val digit = Character.getNumericValue(solutionString[i])
                        board2DIntArray[i / 9][i % 9] = digit

                        if (detailsSudokuBoardView.isEditable) {
                            detailsSudokuBoardView.cells[i / 9][i % 9].value = digit

                            givenDigitsIndices.stream().forEach {t ->
                                if (t!!.contentEquals(intArrayOf(i/9, i%9))) {
                                    detailsSudokuBoardView.cells[i / 9][i % 9].isGiven = true
                                }
                            }
                        }
                    }
                    solution2DIntArrayList.add(board2DIntArray)

                    detailsSudokuBoardView.isEditable = false
                    detailsSudokuBoardView.invalidate()
                }
                solutionCounterTextView.text = "${solutionCounter + 1}/${solution2DIntArrayList.size}"

                updatePreviousNextButtonsClickable()

                //update statistics
                statisticsNoHintsTextView.text = givenDigitsIndices.size.toString()
                statisticsNoSolutionsTextView.text = solution2DIntArrayList.size.toString()
                statisticsTimeTakenToSolveTextView.text = historyEntity.timeTakenToSolve.toString()

                //update more details
                uniqueIdTextView.text = historyEntity.uniqueId
                folderPathTextView.text = historyEntity.folderPath
                originalPicturePathTextView.text = historyEntity.originalPicturePath
                processedPicturePathTextView.text = historyEntity.processedPicturePath
                solutionsPathTextView.text = historyEntity.solutionsPath
            })
        }

        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        appBar.setOnMenuItemClickListener {
            when (it.itemId) {
//                R.id.saveToHistory -> {
//                    val database = Database.invoke(requireContext())
//                    val historyDao = database.getHistoryDao()
//                    CoroutineScope(Dispatchers.IO).launch {
//                        if (historyDao.doesEntryExist(uniqueId)) {
//                            val snackbar = Snackbar.make(constraintLayout, "Already saved to history!", Snackbar.LENGTH_SHORT)
//                            snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
//                            snackbar.show()
//                        }
//                    }
//                    true
//                }

                R.id.deleteHistory -> {
                    val dialogBuilder = AlertDialog.Builder(requireContext())
                    dialogBuilder.setTitle("Delete history")
                    dialogBuilder.setMessage("This action cannot be undone.")
                    dialogBuilder.setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                        val database = Database.invoke(requireContext())
                        val historyDao = database.getHistoryDao()

                        CoroutineScope(Dispatchers.IO).launch {
                            historyDao.deleteHistoryEntry(historyEntity)
                        }

                        FileDeletor.deleteFileOrDirectory(File(historyEntity.folderPath))


                        requireActivity().onBackPressed()

                        Toast.makeText(requireContext(), "History deleted successfully!", Toast.LENGTH_SHORT).show()
                    }

                    dialogBuilder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }

                    val dialog = dialogBuilder.create()
                    dialog.show()
                    true
                }

                R.id.editHistory -> {
                    val sudokuBoard2DIntArray = SudokuBoard2DIntArray()
                    sudokuBoard2DIntArray.uniqueId = detailsSudokuBoardView.uniqueId
                    sudokuBoard2DIntArray.board2DIntArray = detailsSudokuBoardView.givenTo2DIntArray()

                    val action = DetailsFragmentDirections.actionDetailsFragmentToMainFragment(sudokuBoard2DIntArray)
                    findNavController().navigate(action)
                    true
                }

                R.id.shareHistory -> {
                    val sudokuBoardBitmap = detailsSudokuBoardView.drawToBitmap(Bitmap.Config.ARGB_8888)
                    ShareBoardBitmap.shareBoard(requireActivity(), requireContext(), sudokuBoardBitmap)
                    true
                }

                R.id.reportInaccuracy -> {
                    val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    val viewGroup = requireActivity().findViewById<ViewGroup>(android.R.id.content)
                    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report_inaccuracy, viewGroup, false)
                    dialogBuilder.setView(dialogView)
                    val dialog = dialogBuilder.create()
                    dialog.show()

                    val loadingBar = dialogView.findViewById<ProgressBar>(R.id.report_inaccuracy_dialog_loading_bar)
                    val commentsInput = dialogView.findViewById<TextInputEditText>(R.id.report_inaccuracy_dialog_input)
                    val submitReport = dialogView.findViewById<MaterialButton>(R.id.report_inaccuracy_dialog_submit_button)
                    submitReport.setOnClickListener {
                        val firestore = Firebase.firestore
                        val comments = hashMapOf(
                            "id" to detailsSudokuBoardView.uniqueId,
                            "comments" to commentsInput.text.toString(),
                            "dateTime" to DateTimeGenerator.generateDateTime(DateTimeGenerator.DATE_AND_TIME))
                        firestore.collection(getString(R.string.firebase_collection_report_inaccuracy))
                            .add(comments)
                            .addOnSuccessListener { documentReference ->
                                val snackbar = Snackbar.make(constraintLayout, "Report ID: ${documentReference.id}", Snackbar.LENGTH_LONG)
                                snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                                snackbar.setAction("Copy ID") {
                                    val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Report ID", documentReference.id))
                                }
                                snackbar.show()
                            }
                            .addOnFailureListener { exception ->
                                exception.printStackTrace()
                                val snackbar = Snackbar.make(constraintLayout, "Error encountered while sending report. Please try again.", Snackbar.LENGTH_SHORT)
                                snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                                snackbar.show()
                            }.addOnCompleteListener {
                                dialog.dismiss()
                            }

                        commentsInput.visibility = View.INVISIBLE
                        submitReport.visibility = View.INVISIBLE
                        loadingBar.visibility = View.VISIBLE
                        dialog.setCancelable(false)
                    }
                    true
                }

                else -> false
            }
        }

        boardPicture.setOnClickListener {
            if (pictureShowingOriginal) {
                if (historyEntity.processedPicturePath != null) {
                    boardPicture.setImageBitmap(BitmapFactory.decodeFile(historyEntity.processedPicturePath))
                    pictureShowingOriginal = !pictureShowingOriginal
                }
            } else {
                if (historyEntity.originalPicturePath != null) {
                    boardPicture.setImageBitmap(BitmapFactory.decodeFile(historyEntity.originalPicturePath))
                    pictureShowingOriginal = !pictureShowingOriginal
                }
            }
        }

        previousBoard.setOnClickListener {
            solutionCounter -= 1
            for (i in 0..8) {
                for (j in 0..8) {
                    detailsSudokuBoardView.cells[i][j].value = solution2DIntArrayList[solutionCounter][i][j]
                }
            }
            detailsSudokuBoardView.invalidate()
            solutionCounterTextView.text = "${solutionCounter + 1}/${solution2DIntArrayList.size}"

            updatePreviousNextButtonsClickable()
        }

        nextBoard.setOnClickListener {
            solutionCounter += 1
            for (i in 0..8) {
                for (j in 0..8) {
                    detailsSudokuBoardView.cells[i][j].value = solution2DIntArrayList[solutionCounter][i][j]
                }
            }
            detailsSudokuBoardView.invalidate()
            solutionCounterTextView.text = "${solutionCounter + 1}/${solution2DIntArrayList.size}"

            updatePreviousNextButtonsClickable()
        }

        detailsSelection.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.boardDetailChip -> {
                    statisticsDetailContainer.visibility = View.GONE
                    moreDetailsContainer.visibility = View.GONE

                    sudokuBoardDetailContainer.visibility = View.VISIBLE
                }

                R.id.statisticsDetailChip -> {
                    sudokuBoardDetailContainer.visibility = View.GONE
                    moreDetailsContainer.visibility = View.GONE

                    statisticsDetailContainer.visibility = View.VISIBLE
                }

                R.id.moreDetailsDetailChip -> {
                    sudokuBoardDetailContainer.visibility = View.GONE
                    statisticsDetailContainer.visibility = View.GONE

                    moreDetailsContainer.visibility = View.VISIBLE
                }
            }
        }

        uniqueIdContainer.setOnClickListener {
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("uniqueID", uniqueIdTextView.text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Unique ID copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        folderPathContainer.setOnClickListener {
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("uniqueID", folderPathTextView.text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Folder path copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        originalPicturePathContainer.setOnClickListener {
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("uniqueID", originalPicturePathTextView.text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Original picture path copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        processedPicturePathContainer.setOnClickListener {
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("uniqueID", processedPicturePathTextView.text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Processed picture path copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        solutionsPathContainer.setOnClickListener {
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("uniqueID", solutionsPathTextView.text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(requireContext(), "Solutions path copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        return rootView
    }

    private fun updatePreviousNextButtonsClickable() {
        previousBoard.visibility = View.VISIBLE
        nextBoard.visibility = View.VISIBLE

        if (solutionCounter == 0 && previousBoard.visibility == View.VISIBLE) {
            previousBoard.visibility = View.INVISIBLE
        }

        if (solutionCounter == solution2DIntArrayList.size - 1 && nextBoard.visibility == View.VISIBLE) {
            nextBoard.visibility = View.INVISIBLE
        }
    }
}