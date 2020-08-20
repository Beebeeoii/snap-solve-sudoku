package com.beebeeoii.snapsolvesudoku.sudokuboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.beebeeoii.snapsolvesudoku.solver.BoardValidator

private const val TAG = "SudokuBoardView"

class SudokuBoard(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var cellHeight: Float = 0.0F
    private var cellWidth: Float = 0.0F

    var cells: Array<Array<Cell>> = arrayOf(
        arrayOf(
            Cell(row = 0, col = 0),
            Cell(row = 0, col = 1),
            Cell(row = 0, col = 2),
            Cell(row = 0, col = 3),
            Cell(row = 0, col = 4),
            Cell(row = 0, col = 5),
            Cell(row = 0, col = 6),
            Cell(row = 0, col = 7),
            Cell(row = 0, col = 8)
        ),
        arrayOf(
            Cell(row = 1, col = 0),
            Cell(row = 1, col = 1),
            Cell(row = 1, col = 2),
            Cell(row = 1, col = 3),
            Cell(row = 1, col = 4),
            Cell(row = 1, col = 5),
            Cell(row = 1, col = 6),
            Cell(row = 1, col = 7),
            Cell(row = 1, col = 8)
        ),
        arrayOf(
            Cell(row = 2, col = 0),
            Cell(row = 2, col = 1),
            Cell(row = 2, col = 2),
            Cell(row = 2, col = 3),
            Cell(row = 2, col = 4),
            Cell(row = 2, col = 5),
            Cell(row = 2, col = 6),
            Cell(row = 2, col = 7),
            Cell(row = 2, col = 8)
        ),
        arrayOf(
            Cell(row = 3, col = 0),
            Cell(row = 3, col = 1),
            Cell(row = 3, col = 2),
            Cell(row = 3, col = 3),
            Cell(row = 3, col = 4),
            Cell(row = 3, col = 5),
            Cell(row = 3, col = 6),
            Cell(row = 3, col = 7),
            Cell(row = 3, col = 8)
        ),
        arrayOf(
            Cell(row = 4, col = 0),
            Cell(row = 4, col = 1),
            Cell(row = 4, col = 2),
            Cell(row = 4, col = 3),
            Cell(row = 4, col = 4),
            Cell(row = 4, col = 5),
            Cell(row = 4, col = 6),
            Cell(row = 4, col = 7),
            Cell(row = 4, col = 8)
        ),
        arrayOf(
            Cell(row = 5, col = 0),
            Cell(row = 5, col = 1),
            Cell(row = 5, col = 2),
            Cell(row = 5, col = 3),
            Cell(row = 5, col = 4),
            Cell(row = 5, col = 5),
            Cell(row = 5, col = 6),
            Cell(row = 5, col = 7),
            Cell(row = 5, col = 8)
        ),
        arrayOf(
            Cell(row = 6, col = 0),
            Cell(row = 6, col = 1),
            Cell(row = 6, col = 2),
            Cell(row = 6, col = 3),
            Cell(row = 6, col = 4),
            Cell(row = 6, col = 5),
            Cell(row = 6, col = 6),
            Cell(row = 6, col = 7),
            Cell(row = 6, col = 8)
        ),
        arrayOf(
            Cell(row = 7, col = 0),
            Cell(row = 7, col = 1),
            Cell(row = 7, col = 2),
            Cell(row = 7, col = 3),
            Cell(row = 7, col = 4),
            Cell(row = 7, col = 5),
            Cell(row = 7, col = 6),
            Cell(row = 7, col = 7),
            Cell(row = 7, col = 8)
        ),
        arrayOf(
            Cell(row = 8, col = 0),
            Cell(row = 8, col = 1),
            Cell(row = 8, col = 2),
            Cell(row = 8, col = 3),
            Cell(row = 8, col = 4),
            Cell(row = 8, col = 5),
            Cell(row = 8, col = 6),
            Cell(row = 8, col = 7),
            Cell(row = 8, col = 8)
        ))
    var selectedCell: Cell? = null

    var isEditable: Boolean = true
    var isValid: Boolean = true
    var uniqueId = ""
    var timeTakenToSolve = 0

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val backgroundPaint = Paint()
        backgroundPaint.color = Color.WHITE
        canvas?.drawRect(0F, 0F, width.toFloat(), height.toFloat(), backgroundPaint)

        val majorGridDividerPaint = Paint()
        majorGridDividerPaint.color = Color.BLACK
        for (x in 1 until 9) {

            if (x % 3 == 0) { //major divider
                canvas?.drawRect(((x * cellWidth) - 1.5).toFloat(), 0F, ((x * cellWidth) + 1.5).toFloat(), height.toFloat(), majorGridDividerPaint)
                canvas?.drawRect(0F, ((x * cellWidth) - 1.5).toFloat(), width.toFloat(), ((x * cellWidth) + 1.5).toFloat(), majorGridDividerPaint)
            } else { //minor divider
                canvas?.drawRect(((x * cellWidth) - 0.5).toFloat(), 0F, ((x * cellWidth) + 0.5).toFloat(), height.toFloat(), majorGridDividerPaint)
                canvas?.drawRect(0F, ((x * cellWidth) - 0.5).toFloat(), width.toFloat(), ((x * cellWidth) + 0.5).toFloat(), majorGridDividerPaint)

            }
        }

        canvas?.drawRect((width - 3).toFloat(), 0F, width.toFloat(), height.toFloat(), majorGridDividerPaint)
        canvas?.drawRect(0F, 0F, 3F, height.toFloat(), majorGridDividerPaint)
        canvas?.drawRect(0F, (height - 3).toFloat(), width.toFloat(), height.toFloat(), majorGridDividerPaint)
        canvas?.drawRect(0F, 0F, width.toFloat(), 3F, majorGridDividerPaint)

        val selectedGridPaint = Paint()
        selectedGridPaint.color = Color.LTGRAY
        selectedGridPaint.alpha = 80

        if (selectedCell != null && isEditable) {
            canvas?.drawRect(
                selectedCell!!.column * cellWidth,
                0F,
                (selectedCell!!.column + 1) * cellWidth,
                height.toFloat(),
                selectedGridPaint)
            canvas?.drawRect(0F,
                selectedCell!!.row * cellHeight,
                width.toFloat(),
                (selectedCell!!.row + 1) * cellHeight,
                selectedGridPaint)
        }

        val digitPaint = Paint()
        digitPaint.textSize = cellHeight / 2

        val offsetWidth = (cellWidth - digitPaint.measureText("9")) / 2
        val offsetHeight = (cellHeight + digitPaint.textSize) / 2

        val errorPaint = Paint()
        errorPaint.color = Color.RED
        errorPaint.alpha = 50

        val sameDigitPaint = Paint()
        sameDigitPaint.color = Color.rgb(82,26,74)
        sameDigitPaint.alpha = 50
        for (x in 0..8) {
            for (y in 0..8) {
                var cell = cells[x][y]

                if (cell.value != 0) {
                    if (cell.isGiven) {
                        digitPaint.color = Color.rgb(82,26,74)
                    } else {
                        digitPaint.color = Color.DKGRAY
                        digitPaint.alpha = 80
                    }
                    val avgY = (cell.row * cellHeight) + offsetHeight
                    val avgX = (cell.column * cellWidth) + offsetWidth
                    canvas?.drawText(
                        cell.value.toString(),
                        avgX,
                        avgY, digitPaint)
                }

                if (!cell.isValid) {
                    canvas?.drawRect(
                        cell.column * cellWidth,
                        cell.row * cellHeight,
                        (cell.column + 1) * cellWidth,
                        (cell.row + 1) * cellHeight, errorPaint)
                }

                if (cell.value == selectedCell?.value && cell.isValid && selectedCell?.value != 0) {
                    canvas?.drawRect(
                        cell.column * cellWidth,
                        cell.row * cellHeight,
                        (cell.column + 1) * cellWidth,
                        (cell.row + 1) * cellHeight, sameDigitPaint)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Float
        val height: Float

        width = widthSize.toFloat()

        height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize.toFloat()
        } else {
            width
        }

        cellHeight = (height / 9.0).toFloat()
        cellWidth = (width / 9.0).toFloat()

        setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEditable) {
            return true
        }

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                selectedCell = getCellFromCoord(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                selectedCell = getCellFromCoord(x, y)
                invalidate()
            }
        }

        return true
    }

    private fun getCellFromCoord(x: Float, y: Float): Cell? {
        val row: Int = (y / cellHeight).toInt()
        val col: Int = (x / cellWidth).toInt()

        if (row in 0..8 && col in 0..8) {
            return cells[row][col]
        }

        return null
    }

    fun to2DIntArray(): Array<IntArray> {
        val board2DArray = Array(9){IntArray(9){0}}

        for (x in 0..8) {
            for (y in 0..8) {
                board2DArray[x][y] = cells[x][y].value
            }
        }

        return board2DArray
    }

    fun givenTo2DIntArray(): Array<IntArray> {
        val board2DArray = Array(9){IntArray(9){0}}

        for (x in 0..8) {
            for (y in 0..8) {
                if (cells[x][y].isGiven) {
                    board2DArray[x][y] = cells[x][y].value
                } else {
                    board2DArray[x][y] = 0
                }

            }
        }

        return board2DArray
    }

    fun reset() {
        for (x in 0..8) {
            for (y in 0..8) {
                cells[x][y].value = 0
                cells[x][y].isValid = true
                cells[x][y].isGiven = false
            }
        }
        isEditable = true
        isValid = true
        uniqueId = ""
        timeTakenToSolve = 0
    }

    override fun toString(): String {
        var boardString = ""
        for (x in 0..8) {
            for (y in 0..8) {
                boardString += cells[x][y].value.toString()
            }
        }
        return boardString
    }

    override fun invalidate() {
        val boardValidator =
            BoardValidator(this.to2DIntArray())
        boardValidator.validateBoard()

        this.isValid = boardValidator.boardErrors.size <= 0

        for (i in 0..8) {
            for (j in 0..8) {
                this.cells[i][j].isValid = true
            }
        }

        for (cell in boardValidator.boardErrors) {
            this.cells[cell[0]][cell[1]].isValid = false
        }

        super.invalidate()
    }
}