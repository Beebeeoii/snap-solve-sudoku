package com.example.snapsolvesudoku.solver

class BoardValidator (private val board : Array<IntArray>) {

    var isBoardValid : Boolean = true
    var boardErrors : ArrayList<IntArray> = arrayListOf()

    private val EMPTY = 0
    private val ROWS = 9
    private val COLS = 9

    fun getNoDigits() : Int{
        var noDigits = 0

        for (row in board) {
            for (digit in row) {
                if (digit != EMPTY) {
                    noDigits ++
                }
            }
        }

        return noDigits
    }

    private fun isBoardSizeCorrect() : Boolean {
        if (board.size != ROWS) {
            return false
        }

        for (row in board) {
            if (row.size != COLS) {
                return false
            }
        }
        return true
    }

    fun validateBoard() {
        if (!isBoardSizeCorrect()) {
            isBoardValid = false
            return
        }

        fun updateBoardErrors(errorCell1 : IntArray, errorCell2 : IntArray) {
            println("${errorCell1[0]} ${errorCell1[1]} ${errorCell2[0]} ${errorCell2[1]}")
            boardErrors.add(errorCell1)
            boardErrors.add(errorCell2)

            if (isBoardValid) {
                isBoardValid = false
            }
        }

        //column
        for (i in 0..8) {
            val m = BooleanArray(9)
            val n = Array(9) {""}

            for (j in 0..8) {
                if (board[i][j] != EMPTY) {
                    if (m[board[i][j] - 1]) {
                        val numberRepeatedIndex = board[i][j] - 1

                        val errorCell : IntArray = intArrayOf(i, j)
                        val errorCell2 : IntArray = intArrayOf(Character.getNumericValue(n[numberRepeatedIndex][0]), Character.getNumericValue(n[numberRepeatedIndex][1]))

                        updateBoardErrors(errorCell, errorCell2)

                    } else {
                        m[board[i][j] - 1] = true
                        n[board[i][j] - 1] = (i.toString()) + (j.toString())
                    }
                }
            }
        }

        //row
        for (j in 0..8) {
            val m = BooleanArray(9)
            val n = Array(9) {""}

            for (i in 0..8) {
                if (board[i][j] != EMPTY) {
                    if (m[board[i][j] - 1]) {
                        val numberRepeatedIndex = board[i][j] - 1

                        val errorCell : IntArray = intArrayOf(i, j)
                        val errorCell2 : IntArray = intArrayOf(Character.getNumericValue(n[numberRepeatedIndex][0]), Character.getNumericValue(n[numberRepeatedIndex][1]))
                        updateBoardErrors(errorCell, errorCell2)
                    } else {
                        m[board[i][j] - 1] = true
                        n[board[i][j] - 1] = (i.toString()) + (j.toString())
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
                    if (board[i][j] != EMPTY) {
                        if (m[board[i][j] - 1]) {
                            val numberRepeatedIndex = board[i][j] - 1

                            val errorCell : IntArray = intArrayOf(i, j)
                            val errorCell2 : IntArray = intArrayOf(Character.getNumericValue(n[numberRepeatedIndex][0]), Character.getNumericValue(n[numberRepeatedIndex][1]))
                            updateBoardErrors(errorCell, errorCell2)
                        } else {
                            m[board[i][j] - 1] = true
                            n[board[i][j] - 1] = (i.toString()) + (j.toString())
                        }
                    }
                }
            }
        }
    }
}