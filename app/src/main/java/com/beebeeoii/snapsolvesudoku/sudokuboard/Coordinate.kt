package com.beebeeoii.snapsolvesudoku.sudokuboard

/**
 * Coordinate abstracts the position of a cell in a 9x9 sudoku board.
 *
 * @property row The index of the cell's row, starting from 0.
 * @property col The index of the cell's column, starting from 0.
 *
 * @author Jia Wei Lee
 */
class Coordinate(row: Int, col: Int) {
    /**
     * The row of the cell in the sudoku board.
     */
    private val row: Int

    /**
     * The column of the cell in the sudoku board.
     */
    private val col: Int

    /**
     * @constructor Creates a coordinate to represent the position of a cell.
     * @throws InvalidCoordinateException If coordinate provided is not between 0 and 8 (inclusive)
     */
    init {
        if (row < 0 || row > 8 || col < 0 || col > 8) {
            throw InvalidCoordinateException("Row and column must be between 0 and 8")
        }

        this.row = row
        this.col = col
    }

    /**
     * Retrieves the row which the cell is in.
     *
     * @return The row of the cell.
     */
    fun row(): Int {
        return this.row
    }

    /**
     * Retrieves the column which the cell is in.
     *
     * @return The column of the cell.
     */
    fun col(): Int {
        return this.col
    }

    /**
     * Calculates the number of pixels from the left of screen to the left of the cell.
     *
     * @param width The width of each column of the sudoku board.
     *
     * @return The offset (no. of pixels) from the left of the screen to the left of the cell.
     */
    fun calculateXLeftOffset(width: Float): Float {
        return this.col * width
    }

    /**
     * Calculates the number of pixels from the left of screen to the right of the cell.
     *
     * @param width The width of each column of the sudoku board.
     *
     * @return The offset (no. of pixels) from the left of the screen to the right of the cell.
     */
    fun calculateXRightOffset(width: Float): Float {
        return (this.col + 1) * width
    }

    /**
     * Calculates the number of pixels from the top of screen to the top of the cell.
     *
     * @param height The height of each row of the sudoku board.
     *
     * @return The offset (no. of pixels) from the top of the screen to the top of the cell.
     */
    fun calculateYTopOffset(height: Float): Float {
        return this.row() * height
    }

    /**
     * Calculates the number of pixels from the top of screen to the bottom of the cell.
     *
     * @param height The height of each row of the sudoku board.
     *
     * @return The offset (no. of pixels) from the top of the screen to the bottom of the cell.
     */
    fun calculateYBottomOffset(height: Float): Float {
        return (this.row() + 1) * height
    }

    /**
     * Retrieves a string representation of the cell.
     *
     * @return A string representation of the cell (e.g. [2, 5]).
     */
    @Override
    override fun toString(): String {
        return "[$row, $col]"
    }
}