package com.beebeeoii.snapsolvesudoku.sudoku.solver

import com.beebeeoii.snapsolvesudoku.sudoku.board.SudokuBoard

/**
 * Solver is an abstract class that is extended by all solvers.
 *
 * @property maxSolutions The maximum number of solutions the solver should find.
 * @property solutions An ArrayList of solved SudokuBoards.
 */
abstract class Solver(private var maxSolutions: Int) : Solvable {
    /**
     * Types of solvers available to choose from.
     */
    enum class Type {
        BACKTRACK
    }

    private val solutions: ArrayList<SudokuBoard> = ArrayList(0)

    /**
     * Checks whether the number of solutions has reached the maximum solutions requested.
     *
     * @return True if the number of solutions found equals to the maximum number of solutions
     *         the solver should find. False otherwise.
     */
    protected fun hasReachedMaximumRequiredSolutions(): Boolean {
        return this.maxSolutions == solutions.size
    }

    /**
     * Inserts a solution into the ArrayList of SudokuBoards.
     */
    protected fun updateSolutions(solvedBoard: SudokuBoard) {
        solutions.add(solvedBoard)
    }

    /**
     * Returns an array of SudokuBoard.
     *
     * @return An array of SudokuBoard.
     */
    fun getSolutions(): Array<SudokuBoard> {
        return this.solutions.toTypedArray()
    }
}