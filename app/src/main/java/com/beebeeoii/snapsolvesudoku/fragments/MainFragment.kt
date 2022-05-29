package com.beebeeoii.snapsolvesudoku.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.databinding.FragmentMainBinding
import com.beebeeoii.snapsolvesudoku.sudoku.exceptions.InvalidBoardException
import com.beebeeoii.snapsolvesudoku.sudoku.exceptions.UnsolvableBoardException
import com.beebeeoii.snapsolvesudoku.sudoku.keyboard.SudokuKeyboardView
import com.beebeeoii.snapsolvesudoku.sudoku.keyboard.SudokuOptionsView
import com.beebeeoii.snapsolvesudoku.sudoku.solver.Solver

private const val TAG = "MainFragment"

/**
 * MainFragment is the hub of the app.
 */
class MainFragment : Fragment() {
    /**
     * Creates the view of the fragment via data binding.
     */
    @Override
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.sudokuKeyboard.setOnSudokuKeyboardListener(object :
            SudokuKeyboardView.ISudokuKeyboardListener {
                @Override
                override fun onInput(input: Int) {
                    binding.sudokuBoard.setCell(input)
                }
            }
        )

        binding.sudokuOptions.setOnSudokuOptionsListener(object :
            SudokuOptionsView.ISudokuOptionsListener {
                @Override
                override fun onOptionClick(option: SudokuOptionsView.Options) {
                    when (option) {
                        SudokuOptionsView.Options.CLEAR_CELL -> {
                            binding.sudokuBoard.clearCell()
                        }

                        SudokuOptionsView.Options.SOLVE -> {
                            try {
                                binding.sudokuBoard.solve(Solver.Type.BACKTRACK, 1)
                            } catch (err: InvalidBoardException) {
                                //TODO Use snackbar notif
                                Log.d(TAG, err.toString())
                            } catch (err: UnsolvableBoardException) {
                                //TODO Use snackbar notif
                                Log.d(TAG, err.toString())
                            }
                        }

                        SudokuOptionsView.Options.CLEAR_BOARD -> {
                            binding.sudokuBoard.reset()
                        }
                    }
                }
            }
        )

        binding.photoImport.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToImportPictureFragment()
            it.findNavController().navigate(action)
        }

        binding.appBar.setOnMenuItemClickListener {
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

        return binding.root
    }
}