package com.beebeeoii.snapsolvesudoku.sudokuboard

/**
 * SudokuCell represents a cell in a 9x9 sudoku board.
 *
 * @property value The value stored in the cell.
 * @property isGiven True if value is given as part of the puzzle. False otherwise.
 * @property coordinate Position of the cell represented by [Coordinate].
 * @property isValid True if the cell does not violate any sudoku rules. False otherwise.
 *
 * @author Jia Wei Lee
 */
open class SudokuCell private constructor(
    private val value: Int, //TODO value should be encapsulated in an Optional.
    private val isGiven: Boolean,
    private val coordinate: Coordinate,
    private val isValid: Boolean
) {
    companion object {
        /**
         * Factory method to instantiate a sudoku cell with a value.
         *
         * @property value The value stored in the cell.
         * @property isGiven True if value is given as part of the puzzle. False otherwise.
         * @property coordinate Position of the cell represented by [Coordinate].
         */
        // TODO add check for value to be between 1 and 9.
        fun of(value: Int, isGiven: Boolean, coordinate: Coordinate): SudokuCell =
            SudokuCell(value,isGiven, coordinate, true)

        /**
         * Factory method to instantiate an empty sudoku cell.
         *
         * @property coordinate Position of the cell represented by [Coordinate].
         */
        fun empty(coordinate: Coordinate): SudokuCell = EmptySudokuCell(coordinate)
    }

    /**
     * EmptySudokuCell represents an empty cell in a 9x9 sudoku board.
     *
     * @property coordinate Position of the cell represented by [Coordinate].
     */
    private class EmptySudokuCell(coordinate: Coordinate) : SudokuCell(
        0, false,coordinate, true
    ) {
        /**
         * Returns the value stored in the cell.
         *
         * @throws NoValueException Since an empty cell does not contain any value.
         */
        @Override
        override fun value(): Int {
            throw NoValueException("EmptyCell has no value")
        }

        /**
         * Checks if the cell is empty.
         *
         * @return True.
         */
        @Override
        override fun isEmpty(): Boolean {
            return true
        }
    }

    /**
     * Checks if the cell is empty.
     *
     * @return False.
     */
    open fun isEmpty(): Boolean {
        return false
    }

    /**
     * Returns the value stored in the cell.
     *
     * @return The value stored in the cell.
     */
    open fun value(): Int {
        return this.value
    }

    /**
     * Returns a new SudokuCell with the new value provided.
     *
     * @param newValue New value to be stored in the sudoku cell.
     *
     * @return The new SudokuCell containing the new value.
     */
    fun changeValue(newValue: Int): SudokuCell {
        return SudokuCell(newValue, this.isGiven, this.coordinate, this.isValid)
    }

    /**
     * Checks if the cell is given as part of the puzzle.
     *
     * @return True if the cell is given as part of the puzzle. False otherwise.
     */
    fun isGiven(): Boolean {
        return this.isGiven
    }

    /**
     * Checks if the cell is valid.
     *
     * @return True if the cell does not violate any sudoku rules. False otherwise.
     */
    fun isValid(): Boolean {
        return this.isValid
    }

    /**
     * Returns a new valid sudoku cell with the same value, coordinate and isGiven flag.
     *
     * @return A new valid sudoku cell that contains the same data.
     */
    fun validate(): SudokuCell {
        if (this.isEmpty()) {
            return empty(this.coordinate)
        }

        return SudokuCell(this.value, this.isGiven, this.coordinate, true)
    }

    /**
     * Returns a new invalid sudoku cell with the same value, coordinate and isGiven flag.
     *
     * @return A new invalid sudoku cell that contains the same data.
     */
    fun invalidate(): SudokuCell {
        return SudokuCell(this.value, this.isGiven, this.coordinate, false)
    }

    /**
     * Returns a new sudoku cell with the given coordinate.
     *
     * @param coordinate The new coordinate of the sudoku cell.
     *
     * @return A new sudoku cell with a new coordinate.
     */
    fun move(coordinate: Coordinate): SudokuCell {
        return of(this.value, this.isGiven, coordinate)
    }

    /**
     * Returns the coordinate of the sudoku cell represented by [Coordinate].
     *
     * @return The coordinate of the sudoku cell.
     */
    fun getPosition(): Coordinate {
        return this.coordinate
    }

    /**
     * Returns an empty sudoku cell at the current coordinate.
     *
     * @return An empty sudoku cell at the current coordinate.
     */
    fun reset(): SudokuCell {
        return empty(this.coordinate)
    }

    /**
     * Checks whether the value held by this cell is the same as that held by the given cell.
     *
     * @param sudokuCell Another cell to be compared with.
     *
     * @return True if the values held by this cell is equal to that of the given cell.
     *         False otherwise.
     */
    fun valueEquals(sudokuCell: SudokuCell): Boolean {
        return this.value == sudokuCell.value
    }

    /**
     * Retrieves a string representation of the sudoku board.
     *
     * @return A string representation of the sudoku board.
     */
    @Override
    override fun toString(): String {
        return "${this.coordinate}: ${this.value}"
    }
}