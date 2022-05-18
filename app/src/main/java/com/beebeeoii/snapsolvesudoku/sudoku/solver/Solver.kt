package com.beebeeoii.snapsolvesudoku.sudoku.solver

import com.beebeeoii.snapsolvesudoku.sudoku.board.SudokuBoard

abstract class Solver(private var maxSolutions: Int) : Solvable {
    enum class Type {
        BACKTRACK
    }
    private val solutions: ArrayList<SudokuBoard> = ArrayList(0)

    protected fun hasReachedMaximumRequiredSolutions(): Boolean {
        return this.maxSolutions == solutions.size
    }

    protected fun updateSolutions(solvedBoard: SudokuBoard) {
        solutions.add(solvedBoard)
    }

    fun getSolutions(): Array<SudokuBoard> {
        return this.solutions.toTypedArray()
    }
}