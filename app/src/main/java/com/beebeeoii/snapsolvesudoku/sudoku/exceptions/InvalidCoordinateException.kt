package com.beebeeoii.snapsolvesudoku.sudoku.exceptions

/**
 * InvalidCoordinateException is raised when the coordinate provided is not between 0 and 8
 * (inclusive).
 *
 * @property message Message to be printed when exception is thrown.
 *
 * @author Jia Wei Lee
 */
class InvalidCoordinateException(message: String) : Exception(message)