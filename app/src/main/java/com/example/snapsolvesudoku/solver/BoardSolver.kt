package com.example.snapsolvesudoku.solver

class BoardSolver(board : Array<IntArray>, maxSol : Int = 1) {

    private var board: Array<IntArray> = Array(9){IntArray(9)}
    private var maxSol: Int = 0
    private val quad : Array<IntArray> = Array(9){IntArray(10)}
    private val rr : Array<IntArray> = Array(9){IntArray(10)}
    private val cr : Array<IntArray> = Array(9){ IntArray(10) }
    private val EMPTY : Int = 0

    var boardSolutions : ArrayList<Array<IntArray>> = ArrayList(0)

    init {
        this.board = board
        this.maxSol = maxSol
    }

    fun solveBoard() {
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

            if (boardSolutions.size == maxSol) {
                return
            }

            if (xMutable == 8 && yMutable == 9) {
                updateSolutions()
                return
            }

            if (yMutable == 9) {
                yMutable = 0
                xMutable ++
            }

            if (board[xMutable][yMutable] != EMPTY) {
                solve(xMutable, yMutable + 1)
                return
            }

            for (i in 1..9) {
                val q  = quad(xMutable, yMutable)

                if (putable(xMutable, yMutable, i)) {
                    quad[q][i] = 1
                    rr[xMutable][i] = 1
                    cr[yMutable][i] = 1

                    board[xMutable][yMutable] = i
                    solve(xMutable, yMutable+1)
                    board[xMutable][yMutable] = 0

                    rr[xMutable][i] = 0
                    cr[yMutable][i] = 0
                    quad[q][i] = 0
                }
            }
        }

        for (i in 0..8) {
            for (j in 0..8) {
                if (board[i][j] != EMPTY) {
                    val q = quad(i, j)
                    val v = board[i][j]
                    rr[i][v] = 1
                    cr[j][v] = 1
                    quad[q][board[i][j]] = 1
                }
            }
        }

        solve(0, 0)
    }

    private fun updateSolutions() {
        val solutionBoard = Array(9){IntArray(9)}
        for (i in 0..8) {
            for (j in 0..8) {
                solutionBoard[i][j] = board[i][j]
            }
        }
        boardSolutions.add(solutionBoard)
    }
}