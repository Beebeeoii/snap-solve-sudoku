package com.beebeeoii.snapsolvesudoku.sudoku.solver

/**
 * Solvable is a functional interface that solves a sudoku board.
 */
@FunctionalInterface
interface Solvable {
    /**
     * Solves a sudoku board.
     */
    fun solve()
}