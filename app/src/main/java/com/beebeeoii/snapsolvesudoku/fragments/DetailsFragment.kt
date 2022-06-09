package com.beebeeoii.snapsolvesudoku.fragments

import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.databinding.FragmentDetailsBinding
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
import com.beebeeoii.snapsolvesudoku.utils.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.utils.FileDeletor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "DetailsFragment"

class DetailsFragment : Fragment() {

    private val solution2DIntArrayList = mutableListOf<Array<IntArray>>()
    private var solutionCounter = 0
    private val givenDigitsIndices = mutableListOf<IntArray>()
    private lateinit var uniqueId: String
    private lateinit var historyEntity: HistoryEntity

    private var pictureShowingOriginal = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentDetailsBinding.inflate(inflater, container, false)

        if (arguments != null && !requireArguments().isEmpty) {
            uniqueId = DetailsFragmentArgs.fromBundle(requireArguments()).uniqueId

            val database = Database.invoke(requireContext())
            val historyDao = database.getHistoryDao()

            historyDao.getSpecificEntry(uniqueId).observe(viewLifecycleOwner) {
                historyEntity = it[0]
                if (it[0].originalPicturePath != null) {
                    binding.detailsPicture.setImageBitmap(BitmapFactory.decodeFile(it[0].originalPicturePath))
                }
                binding.detailsPicture.visibility = View.VISIBLE

                val dateTimeString = it[0].dateTime
                val dateTimeObject = DateTimeGenerator.getDateTimeObjectFromDateTimeString(
                    dateTimeString
                )
                val dayOfWeek = DateTimeGenerator.generateDayOfWeek(dateTimeObject)
                val formattedDate = DateTimeGenerator.generateFormattedDateTimeString(
                    dateTimeObject
                )
                binding.detailsDate.text = formattedDate
                binding.detailsDay.text = dayOfWeek

                val recognisedDigitsString = it[0].recognisedDigits

                val solutionTextFile = it[0].solutionsPath?.let { it1 -> File(it1) }
                val inputStream = solutionTextFile?.inputStream()
                val solutionStringList = mutableListOf<String>()
                inputStream?.bufferedReader()?.forEachLine { solutionString ->
                    solutionStringList.add(solutionString)
                }

                solutionStringList.forEach { solutionString ->
                    binding.detailsSudokuBoard.setBoard(
                        solutionString,
                        recognisedDigitsString
                    )
//                    solution2DIntArrayList.add(board2DIntArray)
                }
//                solutionCounterTextView.text = "${solutionCounter + 1}/${solution2DIntArrayList.size}"

//                updatePreviousNextButtonsClickable()

                //update statistics
//                statisticsNoHintsTextView.text = givenDigitsIndices.size.toString()
//                statisticsNoSolutionsTextView.text = solution2DIntArrayList.size.toString()
//                statisticsTimeTakenToSolveTextView.text = historyEntity.timeTakenTove.toString()

                //update more details
//                uniqueIdTextView.text = historyEntity.uniqueId
//                folderPathTextView.text = historyEntity.folderPath
//                originalPicturePathTextView.text = historyEntity.originalPicturePath
//                processedPicturePathTextView.text = historyEntity.processedPicturePath
//                solutionsPathTextView.text = historyEntity.solutionsPath
            }
        }

        binding.appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.appBar.setOnMenuItemClickListener {
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
//                    val sudokuBoard2DIntArray = SudokuBoard2DIntArray()
//                    sudokuBoard2DIntArray.uniqueId = detailsSudokuBoardView.uniqueId
//                    sudokuBoard2DIntArray.board2DIntArray = detailsSudokuBoardView.givenTo2DIntArray()
//
//                    val action = DetailsFragmentDirections.actionDetailsFragmentToMainFragment(sudokuBoard2DIntArray)
//                    findNavController().navigate(action)
                    true
                }

                R.id.shareHistory -> {
//                    val sudokuBoardBitmap = detailsSudokuBoardView.drawToBitmap(Bitmap.Config.ARGB_8888)
//                    ShareBoardBitmap.shareBoard(requireActivity(), requireContext(), sudokuBoardBitmap)
                    true
                }

                R.id.reportInaccuracy -> {
//                    val dialogBuilder = AlertDialog.Builder(requireContext())
//                    val viewGroup = requireActivity().findViewById<ViewGroup>(android.R.id.content)
//                    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report_inaccuracy, viewGroup, false)
//                    dialogBuilder.setView(dialogView)
//                    val dialog = dialogBuilder.create()
//                    dialog.show()
//
//                    val loadingBar = dialogView.findViewById<ProgressBar>(R.id.report_inaccuracy_dialog_loading_bar)
//                    val commentsInput = dialogView.findViewById<TextInputEditText>(R.id.report_inaccuracy_dialog_input)
//                    val submitReport = dialogView.findViewById<MaterialButton>(R.id.report_inaccuracy_dialog_submit_button)
//                    submitReport.setOnClickListener {
//                        val firestore = Firebase.firestore
//                        val comments = hashMapOf(
//                            "id" to detailsSudokuBoardView.uniqueId,
//                            "comments" to commentsInput.text.toString(),
//                            "dateTime" to DateTimeGenerator.generateDateTime(DateTimeGenerator.DATE_AND_TIME))
//                        firestore.collection(getString(R.string.firebase_collection_report_inaccuracy))
//                            .add(comments)
//                            .addOnSuccessListener { documentReference ->
//                                val snackbar = Snackbar.make(constraintLayout, "Report ID: ${documentReference.id}", Snackbar.LENGTH_LONG)
//                                snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
//                                snackbar.setAction("Copy ID") {
//                                    val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                                    clipboard.setPrimaryClip(ClipData.newPlainText("Report ID", documentReference.id))
//                                }
//                                snackbar.show()
//                            }
//                            .addOnFailureListener { exception ->
//                                exception.printStackTrace()
//                                val snackbar = Snackbar.make(constraintLayout, "Error encountered while sending report. Please try again.", Snackbar.LENGTH_SHORT)
//                                snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
//                                snackbar.show()
//                            }.addOnCompleteListener {
//                                dialog.dismiss()
//                            }
//
//                        commentsInput.visibility = View.INVISIBLE
//                        submitReport.visibility = View.INVISIBLE
//                        loadingBar.visibility = View.VISIBLE
//                        dialog.setCancelable(false)
//                    }
                    true
                }

                else -> false
            }
        }

//        boardPicture.setOnClickListener {
//            if (pictureShowingOriginal) {
//                if (historyEntity.processedPicturePath != null) {
//                    boardPicture.setImageBitmap(BitmapFactory.decodeFile(historyEntity.processedPicturePath))
//                    pictureShowingOriginal = !pictureShowingOriginal
//                }
//            } else {
//                if (historyEntity.originalPicturePath != null) {
//                    boardPicture.setImageBitmap(BitmapFactory.decodeFile(historyEntity.originalPicturePath))
//                    pictureShowingOriginal = !pictureShowingOriginal
//                }
//            }
//        }
//
//        previousBoard.setOnClickListener {
//            solutionCounter -= 1
//            for (i in 0..8) {
//                for (j in 0..8) {
//                    detailsSudokuBoardView.cells[i][j].value = solution2DIntArrayList[solutionCounter][i][j]
//                }
//            }
//            detailsSudokuBoardView.invalidate()
//            solutionCounterTextView.text = "${solutionCounter + 1}/${solution2DIntArrayList.size}"
//
//            updatePreviousNextButtonsClickable()
//        }
//
//        nextBoard.setOnClickListener {
//            solutionCounter += 1
//            for (i in 0..8) {
//                for (j in 0..8) {
//                    detailsSudokuBoardView.cells[i][j].value = solution2DIntArrayList[solutionCounter][i][j]
//                }
//            }
//            detailsSudokuBoardView.invalidate()
//            solutionCounterTextView.text = "${solutionCounter + 1}/${solution2DIntArrayList.size}"
//
//            updatePreviousNextButtonsClickable()
//        }
//
//        detailsSelection.setOnCheckedChangeListener { _, checkedId ->
//            when (checkedId) {
//                R.id.boardDetailChip -> {
//                    statisticsDetailContainer.visibility = View.GONE
//                    moreDetailsContainer.visibility = View.GONE
//
//                    sudokuBoardDetailContainer.visibility = View.VISIBLE
//                }
//
//                R.id.statisticsDetailChip -> {
//                    sudokuBoardDetailContainer.visibility = View.GONE
//                    moreDetailsContainer.visibility = View.GONE
//
//                    statisticsDetailContainer.visibility = View.VISIBLE
//                }
//
//                R.id.moreDetailsDetailChip -> {
//                    sudokuBoardDetailContainer.visibility = View.GONE
//                    statisticsDetailContainer.visibility = View.GONE
//
//                    moreDetailsContainer.visibility = View.VISIBLE
//                }
//            }
//        }

        return binding.root
    }

//    private fun updatePreviousNextButtonsClickable() {
//        previousBoard.visibility = View.VISIBLE
//        nextBoard.visibility = View.VISIBLE
//
//        if (solutionCounter == 0 && previousBoard.visibility == View.VISIBLE) {
//            previousBoard.visibility = View.INVISIBLE
//        }
//
//        if (solutionCounter == solution2DIntArrayList.size - 1 && nextBoard.visibility == View.VISIBLE) {
//            nextBoard.visibility = View.INVISIBLE
//        }
//    }
}