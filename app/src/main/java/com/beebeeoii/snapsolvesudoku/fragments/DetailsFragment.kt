package com.beebeeoii.snapsolvesudoku.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.beebeeoii.snapsolvesudoku.utils.DateTimeGenerator
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.sudokuboard.SudokuBoard
import com.beebeeoii.snapsolvesudoku.db.Database
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
//                recognisedDigitsString?.forEach { digit ->
//                    if (Character.getNumericValue(digit) != 0) {
//                        val lastIndex = if (givenDigitsIndices.size != 0) givenDigitsIndices.last() else 0
//                        givenDigitsIndices.add(recognisedDigitsString.indexOf(digit, lastIndex))
//                    }
//                }

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