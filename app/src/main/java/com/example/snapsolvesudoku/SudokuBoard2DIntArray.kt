package com.example.snapsolvesudoku

import java.io.Serializable

class SudokuBoard2DIntArray : Serializable{

    var board2DIntArray: Array<IntArray> = Array(9){IntArray(9){0}}
    var uniqueId: String = ""
}