package com.beebeeoii.snapsolvesudoku.sudoku

import java.io.Serializable

class SudokuBoard2DIntArray : Serializable{

    var board2DIntArray: Array<IntArray> = Array(9){IntArray(9){0}}
    var uniqueId: String = ""
}