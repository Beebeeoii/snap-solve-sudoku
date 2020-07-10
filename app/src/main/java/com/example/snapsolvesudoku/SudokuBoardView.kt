package com.example.snapsolvesudoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class SudokuBoard(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var cellHeight: Int = 0
    private var cellWidth: Int = 0

    var cells: Array<Array<Cell>> = arrayOf(
        arrayOf(Cell(row = 0, col = 0), Cell(row = 0, col = 1), Cell(row = 0, col = 2), Cell(row = 0, col = 3), Cell(row = 0, col = 4), Cell(row = 0, col = 5), Cell(row = 0, col = 6), Cell(row = 0, col = 7), Cell(row = 0, col = 8)),
        arrayOf(Cell(row = 1, col = 0), Cell(row = 1, col = 1), Cell(row = 1, col = 2), Cell(row = 1, col = 3), Cell(row = 1, col = 4), Cell(row = 1, col = 5), Cell(row = 1, col = 6), Cell(row = 1, col = 7), Cell(row = 1, col = 8)),
        arrayOf(Cell(row = 2, col = 0), Cell(row = 2, col = 1), Cell(row = 2, col = 2), Cell(row = 2, col = 3), Cell(row = 2, col = 4), Cell(row = 2, col = 5), Cell(row = 2, col = 6), Cell(row = 2, col = 7), Cell(row = 2, col = 8)),
        arrayOf(Cell(row = 3, col = 0), Cell(row = 3, col = 1), Cell(row = 3, col = 2), Cell(row = 3, col = 3), Cell(row = 3, col = 4), Cell(row = 3, col = 5), Cell(row = 3, col = 6), Cell(row = 3, col = 7), Cell(row = 3, col = 8)),
        arrayOf(Cell(row = 4, col = 0), Cell(row = 4, col = 1), Cell(row = 4, col = 2), Cell(row = 4, col = 3), Cell(row = 4, col = 4), Cell(row = 4, col = 5), Cell(row = 4, col = 6), Cell(row = 4, col = 7), Cell(row = 4, col = 8)),
        arrayOf(Cell(row = 5, col = 0), Cell(row = 5, col = 1), Cell(row = 5, col = 2), Cell(row = 5, col = 3), Cell(row = 5, col = 4), Cell(row = 5, col = 5), Cell(row = 5, col = 6), Cell(row = 5, col = 7), Cell(row = 5, col = 8)),
        arrayOf(Cell(row = 6, col = 0), Cell(row = 6, col = 1), Cell(row = 6, col = 2), Cell(row = 6, col = 3), Cell(row = 6, col = 4), Cell(row = 6, col = 5), Cell(row = 6, col = 6), Cell(row = 6, col = 7), Cell(row = 6, col = 8)),
        arrayOf(Cell(row = 7, col = 0), Cell(row = 7, col = 1), Cell(row = 7, col = 2), Cell(row = 7, col = 3), Cell(row = 7, col = 4), Cell(row = 7, col = 5), Cell(row = 7, col = 6), Cell(row = 7, col = 7), Cell(row = 7, col = 8)),
        arrayOf(Cell(row = 8, col = 0), Cell(row = 8, col = 1), Cell(row = 8, col = 2), Cell(row = 8, col = 3), Cell(row = 8, col = 4), Cell(row = 8, col = 5), Cell(row = 8, col = 6), Cell(row = 8, col = 7), Cell(row = 8, col = 8)))
    var selectedCell: Cell = Cell()

    private var isEditable: Boolean = false

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var paint = Paint()
        paint.color = Color.WHITE
        canvas?.drawRect(0F, 10F, width.toFloat(), height.toFloat(), paint)

        Log.wtf("Height", "$width $height")

        var paintDivider = Paint()
        paintDivider.color = Color.BLACK
        for (x in 0 until width step width/9) {
            canvas?.drawRect(x.toFloat(), 10F, (x + 1).toFloat(), height.toFloat(), paintDivider)
            canvas?.drawRect(0F, x.toFloat(), width.toFloat(), (x + 1).toFloat(), paintDivider)
        }

        for (x in 0 until width step width/3) {
            canvas?.drawRect(x.toFloat(), 10F, (x + 3).toFloat(), height.toFloat(), paintDivider)
            canvas?.drawRect(0F, x.toFloat(), width.toFloat(), (x + 3).toFloat(), paintDivider)
        }

        paintDivider.color = Color.LTGRAY
        paintDivider.alpha = 80

        canvas?.drawRect((selectedCell.column * cellWidth).toFloat(),
            0F,
            ((selectedCell.column + 1) * cellWidth).toFloat(),
            height.toFloat(),
            paintDivider)
        canvas?.drawRect(0F,
            (selectedCell.row * cellHeight).toFloat(),
            width.toFloat(),
            ((selectedCell.row + 1) * cellHeight).toFloat(),
            paintDivider)

        val digitPaint = Paint()
        digitPaint.textSize = 90F
        digitPaint.color = Color.BLACK
        val offsetWidth = (cellWidth - digitPaint.measureText("9")) / 2
        val offsetHeight = (cellHeight + digitPaint.textSize) / 2

        val errorPaint = Paint()
        errorPaint.color = Color.RED
        errorPaint.alpha = 50
        for (x in 0..8) {
            for (y in 0..8) {
                var cell = cells[x][y]

                if (cell.value != 0) {
                    val avgY = (cell.row * cellHeight) + offsetHeight
                    val avgX = (cell.column * cellWidth) + offsetWidth
                    canvas?.drawText(cell.value.toString(),
                        avgX,
                        avgY, digitPaint)
                }

                if (!cell.isValid) {
                    canvas?.drawRect((cell.column * cellWidth).toFloat(),
                        (cell.row * cellHeight).toFloat(),
                        ((cell.column + 1) * cellWidth).toFloat(),
                        ((cell.row + 1) * cellHeight).toFloat(), errorPaint)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        Log.wtf("Test", "$widthMode $widthSize $heightMode $heightSize")

        var width : Int
        var height : Int

        width = widthSize

        height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            width
        }

        cellHeight = height / 9
        cellWidth = width / 9

        setMeasuredDimension(width, height)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.wtf("Test", "isEditable: $isEditable, ${event?.action}")
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                selectedCell = getCellFromCoord(x, y)
                Log.wtf("cell","${selectedCell.row} ${selectedCell.column}" )
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                selectedCell = getCellFromCoord(x, y)
                invalidate()
            }
        }

        return true
    }

    private fun getCellFromCoord(x: Float, y: Float): Cell {
        val row: Int = (y / cellHeight).toInt()
        val col: Int = (x / cellWidth).toInt()

        if (row in 0..8 && col in 0..8) {
            return cells[row][col]
        }

        return Cell()
    }

    fun to2DIntArray(): Array<IntArray> {
        var board2DArray = Array(9){IntArray(9){0}}

        for (x in 0..8) {
            for (y in 0..8) {
                board2DArray[x][y] = cells[x][y].value
            }
        }

        return board2DArray
    }

    fun reset() {
        for (x in 0..8) {
            for (y in 0..8) {
                cells[x][y].value = 0
                cells[x][y].isValid = true
            }
        }
    }
}