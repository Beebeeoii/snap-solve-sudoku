package com.beebeeoii.snapsolvesudoku.sudokuboard

/**
 * NoValueException is raised when there is an attempt to retrieve a value from an empty cell.
 * See [SudokuCell.EmptySudokuCell].
 *
 * @property message Message to be printed when exception is thrown.
 *
 * @author Jia Wei Lee
 */
class NoValueException(message: String) : Exception(message)