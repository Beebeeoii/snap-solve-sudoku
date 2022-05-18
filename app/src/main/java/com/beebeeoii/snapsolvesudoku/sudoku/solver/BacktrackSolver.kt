package com.beebeeoii.snapsolvesudoku.sudoku.solver

import android.util.Log
import com.beebeeoii.snapsolvesudoku.sudoku.board.Coordinate
import com.beebeeoii.snapsolvesudoku.sudoku.board.SudokuBoard

private const val TAG = "BacktrackSolver"

class BacktrackSolver(
    private val sudokuBoard: SudokuBoard,
    maxSolutions: Int
) : Solver(maxSolutions) {
    private val quad : Array<IntArray> = Array(9){IntArray(10)}
    private val rr : Array<IntArray> = Array(9){IntArray(10)}
    private val cr : Array<IntArray> = Array(9){ IntArray(10) }

    @Override
    override fun solve() {
        fun quad(x : Int, y : Int) : Int {
            return 3 * (x/3) + y/3
        }

        fun putable (x : Int, y : Int, v : Int) : Boolean {
            val q = quad(x, y)
            return !(rr[x][v] == 1 || cr[y][v] == 1 || quad[q][v] != 0)
        }

        fun solve(x : Int, y : Int) {
            var xMutable = x
            var yMutable = y

            if (super.hasReachedMaximumRequiredSolutions()) {
                return
            }

            if (xMutable == 8 && yMutable == 9) {
                super.updateSolutions(this.sudokuBoard.clone())
                return
            }

            if (yMutable == 9) {
                yMutable = 0
                xMutable ++
            }

            if (!this.sudokuBoard.getCell(xMutable, yMutable).isEmpty()) {
                solve(xMutable, yMutable + 1)
                return
            }

            for (i in 1..9) {
                val q  = quad(xMutable, yMutable)

                if (putable(xMutable, yMutable, i)) {
                    quad[q][i] = 1
                    rr[xMutable][i] = 1
                    cr[yMutable][i] = 1

                    this.sudokuBoard.setCell(Coordinate(xMutable, yMutable), i)
                    solve(xMutable, yMutable+1)
                    this.sudokuBoard.clearCell(Coordinate(xMutable, yMutable))

                    rr[xMutable][i] = 0
                    cr[yMutable][i] = 0
                    quad[q][i] = 0
                }
            }
        }

        for (i in 0..8) {
            for (j in 0..8) {
                if (!this.sudokuBoard.getCell(i, j).isEmpty()) {
                    val q = quad(i, j)
                    val v = sudokuBoard.getCell(i, j).value()
                    rr[i][v] = 1
                    cr[j][v] = 1
                    quad[q][sudokuBoard.getCell(i, j).value()] = 1
                }
            }
        }

        solve(0, 0)
    }
}