package com.beebeeoii.snapsolvesudoku.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.beebeeoii.snapsolvesudoku.databinding.FragmentMainBinding
import com.beebeeoii.snapsolvesudoku.sudoku.keyboard.SudokuKeyboardView

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
            override fun onInput(input: Int) {
                binding.sudokuBoard.setCell(input)
            }
        })

        return binding.root
    }
}