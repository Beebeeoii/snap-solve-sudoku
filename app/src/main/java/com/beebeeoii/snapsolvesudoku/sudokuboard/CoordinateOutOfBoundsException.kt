package com.beebeeoii.snapsolvesudoku.sudokuboard

/**
 * CoordinateOutOfBoundsException is raised when the canvas coordinate cannot be translated to a
 * [Coordinate]. See [SudokuBoardView.getBoardCoordFromCanvasCoord].
 *
 * @property message Message to be printed when exception is thrown.
 *
 * @author Jia Wei Lee
 */
class CoordinateOutOfBoundsException(message: String) : Exception(message)