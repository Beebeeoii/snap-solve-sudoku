package com.example.snapsolvesudoku.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.snapsolvesudoku.R
import com.example.snapsolvesudoku.SudokuBoard
import com.example.snapsolvesudoku.db.Database
import com.example.snapsolvesudoku.db.HistoryEntity
import com.example.snapsolvesudoku.solver.BoardSolver
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

private const val TAG = "MainFragment"
private lateinit var mainFragmentContainer: ConstraintLayout
private lateinit var importButton: ExtendedFloatingActionButton
lateinit var sudokuBoardView: SudokuBoard
private lateinit var appBar: MaterialToolbar
private lateinit var clearCell: MaterialButton
private lateinit var clearBoard: MaterialButton
private lateinit var moreDetails: MaterialButton
private lateinit var share: MaterialButton
private lateinit var solve: MaterialButton
private lateinit var input_1_button: MaterialButton
private lateinit var input_2_button: MaterialButton
private lateinit var input_3_button: MaterialButton
private lateinit var input_4_button: MaterialButton
private lateinit var input_5_button: MaterialButton
private lateinit var input_6_button: MaterialButton
private lateinit var input_7_button: MaterialButton
private lateinit var input_8_button: MaterialButton
private lateinit var input_9_button: MaterialButton

class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        mainFragmentContainer = view.findViewById(R.id.mainFragmentContainer)
        sudokuBoardView = view.findViewById(R.id.sudokuBoard)
        importButton = view.findViewById(R.id.photoImport)
        clearCell = view.findViewById(R.id.clearCellButton)
        clearBoard = view.findViewById(R.id.clearBoardButton)
        moreDetails = view.findViewById(R.id.detailsButton)
        share = view.findViewById(R.id.shareButton)
        solve = view.findViewById(R.id.solveButton)
        input_1_button = view.findViewById(R.id.input_1)
        input_2_button = view.findViewById(R.id.input_2)
        input_3_button = view.findViewById(R.id.input_3)
        input_4_button = view.findViewById(R.id.input_4)
        input_5_button = view.findViewById(R.id.input_5)
        input_6_button = view.findViewById(R.id.input_6)
        input_7_button = view.findViewById(R.id.input_7)
        input_8_button = view.findViewById(R.id.input_8)
        input_9_button = view.findViewById(R.id.input_9)
        appBar = view.findViewById(R.id.appBar)

        appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.history -> {
                    val action = MainFragmentDirections.actionMainFragmentToHistoryFragment()
                    findNavController().navigate(action)
                    true
                }
                R.id.settings -> {
                    val action = MainFragmentDirections.actionMainFragmentToSettingsFragment()
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }

        if (arguments != null && !requireArguments().isEmpty) {
            val sudokuBoard2DIntArray = MainFragmentArgs.fromBundle(requireArguments()).board2DIntArray

            for (i in 0..8) {
                for (j in 0..8) {
                    val recognisedDigit = sudokuBoard2DIntArray.board2DIntArray[i][j]
                    if (recognisedDigit != 0) {
                        sudokuBoardView.cells[i][j].value = recognisedDigit
                        sudokuBoardView.cells[i][j].isGiven = true
                    }
                }
            }
            sudokuBoardView.invalidate()

        }

        clearBoard.setOnClickListener {
            sudokuBoardView.reset()
            sudokuBoardView.invalidate()

            solve.isClickable = true
            solve.setBackgroundColor(Color.WHITE)
        }

        clearCell.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 0
            sudokuBoardView.selectedCell?.isValid = true
            sudokuBoardView.selectedCell?.isGiven = false
            sudokuBoardView.invalidate()
        }

        solve.setOnClickListener {
            if (sudokuBoardView.isValid) {
                it.isClickable = false
                it.setBackgroundColor(Color.argb(50, 82, 26, 74))
                var solver = BoardSolver(sudokuBoardView.to2DIntArray(),2)
                solver.solveBoard()

                var solution = solver.boardSolutions[0]
                for (i in 0..8) {
                    for (j in 0..8) {
                        sudokuBoardView.cells[i][j].value = solution[i][j]
                    }
                }

                sudokuBoardView.isEditable = false
                sudokuBoardView.selectedCell = null
                sudokuBoardView.invalidate()
            } else {
                val snackbar = Snackbar.make(mainFragmentContainer, "Invalid board", Snackbar.LENGTH_SHORT)
                snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
                snackbar.anchorView = importButton
                snackbar.show()
            }
        }

        moreDetails.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToDetailsFragment()
            findNavController().navigate(action)
        }

        share.setOnClickListener {
            sudokuBoardView.selectedCell = null
            sudokuBoardView.invalidate()

            val sudokuBoardBitmap = sudokuBoardView.drawToBitmap(Bitmap.Config.ARGB_8888)
            val dirFile = File(requireActivity().getExternalFilesDir(null).toString())
            val noFiles = dirFile.listFiles().size
            val filePath = requireActivity().getExternalFilesDir(null).toString() + "/boardBit_${noFiles}.png"
            val out = FileOutputStream(filePath)
            sudokuBoardBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)

            val database = Database.invoke(requireContext())
            val historyDao = database?.getHistoryDao()
            CoroutineScope(Dispatchers.IO).launch {
                historyDao.insertHistoryEntry(HistoryEntity(filePath, "Saturday", "1 August 2020", "144220", List(1){"132"}))
            }

            val snackbar = Snackbar.make(mainFragmentContainer, "Board image saved!", Snackbar.LENGTH_SHORT)
            snackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
            snackbar.anchorView = importButton
            snackbar.show()
        }

        input_1_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value  = 1
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_2_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 2
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_3_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 3
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_4_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 4
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_5_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 5
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_6_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 6
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_7_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 7
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_8_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 8
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        input_9_button.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 9
            sudokuBoardView.selectedCell?.isGiven = true
            sudokuBoardView.invalidate()
        }

        importButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToImportPictureFragment()
            it.findNavController().navigate(action)
        }

        return view
    }
}