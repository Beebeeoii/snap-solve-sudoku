package com.beebeeoii.snapsolvesudoku.sudoku.board

import android.util.Log
import com.beebeeoii.snapsolvesudoku.sudoku.exceptions.InvalidBoardException
import com.beebeeoii.snapsolvesudoku.sudoku.exceptions.UnsolvableBoardException
import com.beebeeoii.snapsolvesudoku.sudoku.solver.BacktrackSolver
import com.beebeeoii.snapsolvesudoku.sudoku.solver.Solver
import java.util.Optional

private const val TAG = "SudokuBoard"

/**
 * SudokuBoard represents a 9x9 sudoku board.
 *
 * @property isEditable Represents whether the sudoku board is currently editable. Defaults to true.
 * @property isValid Represents whether the sudoku board is currently valid. A valid sudoku board
 *                   follows the rule: no duplication of any digit (1-9) in any row, column and
 *                   3x3 block. Defaults to true.
 * @property selectedSudokuCell Represents the selected cell. Since it is not necessary for there to
 *                              always be a selected cell, this field is wrapped in an Optional.
 * @property sudokuCells Represents the sudoku cells initialised to be a 2D array of sudoku cells.
 *
 * @author Jia Wei Lee
 */
class SudokuBoard {
    private var isEditable: Boolean = true
    private var isValid: Boolean = true
    private var selectedSudokuCell: Optional<SudokuCell> = Optional.empty()
    private val sudokuCells: Array<Array<SudokuCell>> = arrayOf(
        arrayOf(
            SudokuCell.empty(Coordinate(0, 0)),
            SudokuCell.empty(Coordinate(0, 1)),
            SudokuCell.empty(Coordinate(0, 2)),
            SudokuCell.empty(Coordinate(0, 3)),
            SudokuCell.empty(Coordinate(0, 4)),
            SudokuCell.empty(Coordinate(0, 5)),
            SudokuCell.empty(Coordinate(0, 6)),
            SudokuCell.empty(Coordinate(0, 7)),
            SudokuCell.empty(Coordinate(0, 8))
        ),
        arrayOf(
            SudokuCell.empty(Coordinate(1, 0)),
            SudokuCell.empty(Coordinate(1, 1)),
            SudokuCell.empty(Coordinate(1, 2)),
            SudokuCell.empty(Coordinate(1, 3)),
            SudokuCell.empty(Coordinate(1, 4)),
            SudokuCell.empty(Coordinate(1, 5)),
            SudokuCell.empty(Coordinate(1, 6)),
            SudokuCell.empty(Coordinate(1, 7)),
            SudokuCell.empty(Coordinate(1, 8))
        ),
        arrayOf(
            SudokuCell.empty(Coordinate(2, 0)),
            SudokuCell.empty(Coordinate(2, 1)),
            SudokuCell.empty(Coordinate(2, 2)),
            SudokuCell.empty(Coordinate(2, 3)),
            SudokuCell.empty(Coordinate(2, 4)),
            SudokuCell.empty(Coordinate(2, 5)),
            SudokuCell.empty(Coordinate(2, 6)),
            SudokuCell.empty(Coordinate(2, 7)),
            SudokuCell.empty(Coordinate(2, 8))
        ),
        arrayOf(
            SudokuCell.empty(Coordinate(3, 0)),
            SudokuCell.empty(Coordinate(3, 1)),
            SudokuCell.empty(Coordinate(3, 2)),
            SudokuCell.empty(Coordinate(3, 3)),
            SudokuCell.empty(Coordinate(3, 4)),
            SudokuCell.empty(Coordinate(3, 5)),
            SudokuCell.empty(Coordinate(3, 6)),
            SudokuCell.empty(Coordinate(3, 7)),
            SudokuCell.empty(Coordinate(3, 8))
        ),
        arrayOf(
            SudokuCell.empty(Coordinate(4, 0)),
            SudokuCell.empty(Coordinate(4, 1)),
            SudokuCell.empty(Coordinate(4, 2)),
            SudokuCell.empty(Coordinate(4, 3)),
            SudokuCell.empty(Coordinate(4, 4)),
            SudokuCell.empty(Coordinate(4, 5)),
            SudokuCell.empty(Coordinate(4, 6)),
            SudokuCell.empty(Coordinate(4, 7)),
            SudokuCell.empty(Coordinate(4, 8))
        ),
        arrayOf(
            SudokuCell.empty(Coordinate(5, 0)),
            SudokuCell.empty(Coordinate(5, 1)),
            SudokuCell.empty(Coordinate(5, 2)),
            SudokuCell.empty(Coordinate(5, 3)),
            SudokuCell.empty(Coordinate(5, 4)),
            SudokuCell.empty(Coordinate(5, 5)),
            SudokuCell.empty(Coordinate(5, 6)),
            SudokuCell.empty(Coordinate(5, 7)),
            SudokuCell.empty(Coordinate(5, 8))
        ),
        arrayOf(
            SudokuCell.empty(Coordinate(6, 0)),
            SudokuCell.empty(Coordinate(6, 1)),
            SudokuCell.empty(Coordinate(6, 2)),
            SudokuCell.empty(Coordinate(6, 3)),
            SudokuCell.empty(Coordinate(6, 4)),
            SudokuCell.empty(Coordinate(6, 5)),
            SudokuCell.empty(Coordinate(6, 6)),
            SudokuCell.empty(Coordinate(6, 7)),
            SudokuCell.empty(Coordinate(6, 8))
        ),
        arrayOf(
            SudokuCell.empty(Coordinate(7, 0)),
            SudokuCell.empty(Coordinate(7, 1)),
            SudokuCell.empty(Coordinate(7, 2)),
            SudokuCell.empty(Coordinate(7, 3)),
            SudokuCell.empty(Coordinate(7, 4)),
            SudokuCell.empty(Coordinate(7, 5)),
            SudokuCell.empty(Coordinate(7, 6)),
            SudokuCell.empty(Coordinate(7, 7)),
            SudokuCell.empty(Coordinate(7, 8))
        ),
        arrayOf(
            SudokuCell.empty(Coordinate(8, 0)),
            SudokuCell.empty(Coordinate(8, 1)),
            SudokuCell.empty(Coordinate(8, 2)),
            SudokuCell.empty(Coordinate(8, 3)),
            SudokuCell.empty(Coordinate(8, 4)),
            SudokuCell.empty(Coordinate(8, 5)),
            SudokuCell.empty(Coordinate(8, 6)),
            SudokuCell.empty(Coordinate(8, 7)),
            SudokuCell.empty(Coordinate(8, 8))
        ))

    /**
     * Returns if the sudoku board is editable.
     *
     * @return True if sudoku board is editable. False otherwise.
     */
    fun isEditable(): Boolean {
        return this.isEditable
    }

    /**
     * Freezes the sudoku board, making it uneditable.
     */
    fun freeze() {
        this.isEditable = false
    }

    /**
     * Unfreezes the sudoku board, making it editable.
     */
    fun unfreeze() {
        this.isEditable = true
    }

    /**
     * Returns the selected cell represented by [SudokuCell].
     *
     * @return The selected sudoku cell.
     *
     * @throws NoSuchElementException If there is no selected cell.
     */
    fun getSelectedCell(): SudokuCell {
        return this.selectedSudokuCell.get()
    }

    /**
     * Returns the selected cell's coordinate represented by [Coordinate].
     *
     * @return The selected sudoku cell's coordinate.
     *
     * @throws NoSuchElementException If there is no selected cell.
     */
    fun getSelectedCellCoordinate(): Coordinate {
        return this.selectedSudokuCell.map { cell: SudokuCell -> cell.getPosition() }.get()
    }

    /**
     * Returns the cell specified by a given coordinate, represented by [Coordinate]. This is an
     * overloaded method.
     *
     * @param coordinate The coordinate at which the cell to be return is at.
     *
     * @return The sudoku cell at the given coordinate.
     */
    fun getCell(coordinate: Coordinate): SudokuCell {
        return this.sudokuCells[coordinate.row()][coordinate.col()]
    }

    /**
     * Returns the cell specified by a given coordinate, represented by the row and column indexes.
     * This is an overloaded method.
     *
     * @param row The index of the row which the cell is in.
     * @param col The index of the column which the cell is in.
     *
     * @return The sudoku cell at the given coordinate.
     */
    fun getCell(row: Int, col: Int): SudokuCell {
        return this.sudokuCells[row][col]
    }

    /**
     * Sets the value of the cell specified by a given coordinate. This is an overloaded method.
     *
     * @param coordinate The coordinate at which the cell is at.
     * @param value The value to be set.
     */
    fun setCell(coordinate: Coordinate, value: Int) {
        if (!this.isEditable) {
            return
        }

        val ogCell = this.sudokuCells[coordinate.row()][coordinate.col()]
        this.sudokuCells[coordinate.row()][coordinate.col()] = ogCell.changeValue(value)
    }

    /**
     * Sets the value of the cell specified by a given coordinate. This method is used to specify
     * if the cell is a hint (given as part of the puzzle). This is an overloaded method.
     *
     * @param coordinate The coordinate at which the cell is at.
     * @param value The value to be set.
     * @param isGiven True if the cell is given as part of the hints. False otherwise.
     */
    fun setCell(coordinate: Coordinate, value: Int, isGiven: Boolean = false) {
        if (!this.isEditable) {
            return
        }

        this.sudokuCells[coordinate.row()][coordinate.col()] = SudokuCell.of(
            value, isGiven, coordinate
        )
    }

    /**
     * Sets the selected cell.
     *
     * @param coordinate The coordinate at which the selected cell is at.
     */
    fun setSelectedCell(coordinate: Coordinate) {
        this.selectedSudokuCell = Optional.of(this.sudokuCells[coordinate.row()][coordinate.col()])
    }

    /**
     * Resets the cell at the given coordinate.
     *
     * @param coordinate The coordinate of the cell to reset.
     */
    fun clearCell(coordinate: Coordinate) {
        this.sudokuCells[coordinate.row()][coordinate.col()] = SudokuCell.empty(coordinate)
    }

    /**
     * Sets the sudoku board to be valid and all the cells to be valid.
     */
    private fun validate() {
        this.isValid = true

        for (i in 0..8) {
            for (j in 0..8) {
                this.sudokuCells[i][j] = this.sudokuCells[i][j].validate()
            }
        }
    }

    /**
     * Sets the sudoku board to be invalid.
     */
    private fun invalidate() {
        this.isValid = false
    }

    /**
     * Resets the sudoku and all its cells.
     */
    fun reset() {
        for (x in 0..8) {
            for (y in 0..8) {
                sudokuCells[x][y] = sudokuCells[x][y].reset()
            }
        }
        isEditable = true
        isValid = true
    }

    /**
     * Checks through all the cells to determine whether the sudoku board is a valid one based on
     * the rules of sudoku. Cells violating the rules will be marked as invalid and the sudoku
     * board will also be marked as invalid.
     */
    fun validateBoard() {
        fun updateBoardErrors(errorCell1: SudokuCell, errorCell2: SudokuCell) {
            Log.v(TAG, "Invalid cells: $errorCell2 $errorCell1")

            this.invalidate()
            this.sudokuCells[errorCell1.getPosition().row()][errorCell1.getPosition().col()] =
                errorCell1.invalidate()
            this.sudokuCells[errorCell2.getPosition().row()][errorCell2.getPosition().col()] =
                errorCell2.invalidate()
        }

        this.validate()

        //column
        for (i in 0..8) {
            val m = BooleanArray(9)
            val n = Array(9) {""}

            for (j in 0..8) {
                val currentCell = this.getCell(i, j)

                if (!currentCell.isEmpty()) {
                    val booleanArrayIndexForCurrentCell = currentCell.value() - 1

                    if (m[booleanArrayIndexForCurrentCell]) {
                        val errorCell: SudokuCell = this.getCell(i, j)
                        val errorCell2: SudokuCell = this.getCell(
                            Character.getNumericValue(n[booleanArrayIndexForCurrentCell][0]),
                            Character.getNumericValue(n[booleanArrayIndexForCurrentCell][1])
                        )

                        updateBoardErrors(errorCell, errorCell2)
                    } else {
                        m[booleanArrayIndexForCurrentCell] = true
                        n[booleanArrayIndexForCurrentCell] = (i.toString()) + (j.toString())
                    }
                }
            }
        }

        //row
        for (j in 0..8) {
            val m = BooleanArray(9)
            val n = Array(9) {""}

            for (i in 0..8) {
                val currentCell = this.getCell(i, j)

                if (!currentCell.isEmpty()) {
                    val booleanArrayIndexForCurrentCell = currentCell.value() - 1

                    if (m[booleanArrayIndexForCurrentCell]) {
                        val errorCell: SudokuCell = this.getCell(i, j)
                        val errorCell2: SudokuCell = this.getCell(
                            Character.getNumericValue(n[booleanArrayIndexForCurrentCell][0]),
                            Character.getNumericValue(n[booleanArrayIndexForCurrentCell][1])
                        )

                        updateBoardErrors(errorCell, errorCell2)
                    } else {
                        m[booleanArrayIndexForCurrentCell] = true
                        n[booleanArrayIndexForCurrentCell] = (i.toString()) + (j.toString())
                    }
                }
            }
        }

        //3*3 block
        for (block in 0..8) {
            val m = BooleanArray(9)
            val n = Array(9) {""}

            for (i in (block / 3 * 3) until (block / 3 * 3 + 3)) {
                for (j in (block % 3 * 3) until (block % 3 * 3 + 3)) {
                    val currentCell = this.getCell(i, j)

                    if (!currentCell.isEmpty()) {
                        val booleanArrayIndexForCurrentCell = currentCell.value() - 1

                        if (m[booleanArrayIndexForCurrentCell]) {
                            val errorCell : SudokuCell = this.getCell(i, j)
                            val errorCell2 : SudokuCell = this.getCell(
                                Character.getNumericValue(n[booleanArrayIndexForCurrentCell][0]),
                                Character.getNumericValue(n[booleanArrayIndexForCurrentCell][1])
                            )

                            updateBoardErrors(errorCell, errorCell2)
                        } else {
                            m[booleanArrayIndexForCurrentCell] = true
                            n[booleanArrayIndexForCurrentCell] = (i.toString()) + (j.toString())
                        }
                    }
                }
            }
        }
    }

    /**
     * Solves the sudoku board.
     *
     * @return An array of solved sudoku boards.
     *
     * @throws InvalidBoardException Error is thrown when the board provided is already invalid.
     * @throws UnsolvableBoardException Error is thrown when there are no solutions found.
     */
    fun solve(solverType: Solver.Type, maxSolutions: Int): Array<SudokuBoard> {
        if (!this.isValid) {
            throw InvalidBoardException("Board is invalid and cannot be solved.")
        }
        val solver: Solver
        when (solverType) {
            Solver.Type.BACKTRACK -> {
                solver = BacktrackSolver(this, maxSolutions)
            }
        }
        solver.solve()

        if (solver.getSolutions().isEmpty()) {
            throw UnsolvableBoardException("Board is unsolvable.")
        }
        return solver.getSolutions()
    }

    /**
     * Clones the sudoku board.
     *
     * @return A cloned sudoku board.
     */
    fun clone(): SudokuBoard {
        val clonedBoard = SudokuBoard()
        for (x in 0..8) {
            for (y in 0..8) {
                val currentCell = this.getCell(x, y)
                clonedBoard.setCell(Coordinate(x, y), currentCell.value(), currentCell.isGiven())
            }
        }
        return clonedBoard
    }

    /**
     * Returns a 2D array of Int representation of the sudoku board. This can be used to serialise a
     * sudoku board.
     *
     * @param hideInputDigits True if input digits are to be hidden (represented by 0).
     *                        False otherwise.
     *
     * @return A 2D array of Int.
     */
    fun to2DIntArray(hideInputDigits: Boolean): Array<IntArray> {
        val board2DArray = Array(9){IntArray(9){0}}

        for (x in 0..8) {
            for (y in 0..8) {
                if (hideInputDigits) {
                    board2DArray[x][y] = if (this.sudokuCells[x][y].isGiven()) {
                        this.sudokuCells[x][y].value()
                    } else {
                        0
                    }
                } else {
                    board2DArray[x][y] = this.sudokuCells[x][y].value()
                }
            }
        }

        return board2DArray
    }

    /**
     * Retrieves a string representation of the sudoku board.
     *
     * @return A string representation of the sudoku board.
     */
    @Override
    override fun toString(): String {
        var boardString = ""
        for (x in 0..8) {
            for (y in 0..8) {
                boardString += this.sudokuCells[x][y].toString()
            }
        }
        return boardString
    }
}