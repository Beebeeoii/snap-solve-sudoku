package com.beebeeoii.snapsolvesudoku.sudoku.exceptions

/**
 * UnsolvableBoardException is raised when the board provided is unsolvable - there are no solutions
 * that does not violate the rules of sudoku.
 *
 * @property message Message to be printed when exception is thrown.
 *
 * @author Jia Wei Lee
 */
class UnsolvableBoardException(message: String) : Exception(message)