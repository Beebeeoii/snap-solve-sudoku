package com.beebeeoii.snapsolvesudoku.sudoku.exceptions

/**
 * InvalidBoardException is raised when the board provided is invalid.
 *
 * @property message Message to be printed when exception is thrown.
 *
 * @author Jia Wei Lee
 */
class InvalidBoardException(message: String) : Exception(message)