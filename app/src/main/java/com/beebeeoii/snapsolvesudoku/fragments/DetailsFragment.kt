package com.beebeeoii.snapsolvesudoku.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.beebeeoii.snapsolvesudoku.utils.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.sudokuboard.SudokuBoard
import com.beebeeoii.snapsolvesudoku.db.Database
import com.beebeeoii.snapsolvesudoku.db.HistoryEntity
import com.beebeeoii.snapsolvesudoku.sudokuboard.SudokuBoard2DIntArray
import com.beebeeoii.snapsolvesudoku.utils.FileDeletor
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private lateinit var constraintLayout: ConstraintLayout
private lateinit var appBar: MaterialToolbar
private lateinit var boardPicture: AppCompatImageView
private lateinit var dayTextView: MaterialTextView
private lateinit var dateTextView: MaterialTextView
private lateinit var detailsSudokuBoardView: SudokuBoard
private lateinit var previousBoard: MaterialButton
private lateinit var solutionCounterTextView: MaterialTextView
private lateinit var nextBoard: MaterialButton
private const val TAG = "DetailsFragment"

class DetailsFragment : Fragment() {

    private val solution2DIntArrayList = mutableListOf<Array<IntArray>>()
    private var solutionCounter = 0
    private val givenDigitsIndices = mutableListOf<IntArray>()
    private lateinit var uniqueId: String
    private lateinit var historyEntity: HistoryEntity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_details, container, false)

        constraintLayout = rootView.findViewById(R.id.detailsConstraintLayout)
        appBar = rootView.findViewById(R.id.appBar)
        boardPicture = rootView.findViewById(R.id.detailsPicture)
        dayTextView = rootView.findViewById(R.id.detailsDay)
        dateTextView = rootView.findViewById(R.id.detailsDate)
        detailsSudokuBoardView = rootView.findViewById(R.id.detailsSudokuBoard)
        previousBoard = rootView.findViewById(R.id.detailsPreviousBoard)
        solutionCounterTextView = rootView.findViewById(R.id.detailsBoardTracker)
        nextBoard = rootView.findViewById(R.id.detailsNextBoard)

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
            })
        }

        appBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.saveToHistory -> {
                    val database = Database.invoke(requireContext())
                    val historyDao = database.getHistoryDao()
                    CoroutineScope(Dispatchers.IO).launch {
                        if (historyDao.doesEntryExist(uniqueId)) {
                            val snackbar = Snackbar.make(constraintLayout, "Already saved to history!", Snackbar.LENGTH_SHORT)
                            snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                            snackbar.show()
                        } else {

                        }
                    }
                    true
                }

                R.id.deleteHistory -> {
                    val dialogBuilder = AlertDialog.Builder(requireContext())
                    dialogBuilder.setTitle("Delete history")
                    dialogBuilder.setMessage("This action cannot be undone.")
                    dialogBuilder.setPositiveButton("Delete") { dialogInterface: DialogInterface, i: Int ->
                        val database = Database.invoke(requireContext())
                        val historyDao = database.getHistoryDao()

                        CoroutineScope(Dispatchers.IO).launch {
                            historyDao.deleteHistoryEntry(historyEntity)
                        }

                        FileDeletor.deleteFileOrDirectory(File(historyEntity.folderPath))


                        requireActivity().onBackPressed()

                        Toast.makeText(requireContext(), "History deleted successfully!", Toast.LENGTH_SHORT).show()
                    }

                    dialogBuilder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
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

                else -> false
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