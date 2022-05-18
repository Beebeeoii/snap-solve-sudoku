package com.beebeeoii.snapsolvesudoku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.beebeeoii.snapsolvesudoku.databinding.FragmentMainBinding
import com.beebeeoii.snapsolvesudoku.sudoku.keyboard.SudokuKeyboardView
import com.beebeeoii.snapsolvesudoku.sudoku.keyboard.SudokuOptionsView
import com.beebeeoii.snapsolvesudoku.sudoku.solver.Solver

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
                            binding.sudokuBoard.solve(Solver.Type.BACKTRACK, 1)
                        }

                        SudokuOptionsView.Options.CLEAR_BOARD -> {
                            binding.sudokuBoard.reset()
                        }
                    }
                }
            }
        )

        return binding.root
    }
}