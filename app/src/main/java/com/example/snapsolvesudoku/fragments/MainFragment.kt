package com.example.snapsolvesudoku.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.example.snapsolvesudoku.R
import com.example.snapsolvesudoku.SudokuBoard
import com.example.snapsolvesudoku.solver.BoardSolver
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "MainFragment"
private lateinit var mainFragmentContainer: ConstraintLayout
private lateinit var importButton: ExtendedFloatingActionButton
lateinit var sudokuBoardView: SudokuBoard
private lateinit var clear: MaterialButton
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

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        mainFragmentContainer = view.findViewById(R.id.mainFragmentContainer)
        sudokuBoardView = view.findViewById(R.id.sudokuBoard)
        importButton = view.findViewById(R.id.photoImport)
        clear = view.findViewById(R.id.clearButton)
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

        importButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToImportPictureFragment()
            it.findNavController().navigate(action)
        }

        clear.setOnClickListener {
            sudokuBoardView.selectedCell?.value = 0
            sudokuBoardView.selectedCell?.isValid = true
            sudokuBoardView.selectedCell?.isGiven = false
            sudokuBoardView.invalidate()
        }

        clear.setOnLongClickListener {
            sudokuBoardView.reset()
            sudokuBoardView.invalidate()

            solve.isClickable = true
            solve.setBackgroundColor(Color.WHITE)
            true
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

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}