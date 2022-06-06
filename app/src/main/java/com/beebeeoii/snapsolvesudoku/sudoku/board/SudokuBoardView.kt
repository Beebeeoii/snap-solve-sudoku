package com.beebeeoii.snapsolvesudoku.sudoku.board

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.beebeeoii.snapsolvesudoku.R
import com.beebeeoii.snapsolvesudoku.sudoku.exceptions.CoordinateOutOfBoundsException
import com.beebeeoii.snapsolvesudoku.sudoku.exceptions.InvalidBoardException
import com.beebeeoii.snapsolvesudoku.sudoku.exceptions.UnsolvableBoardException
import com.beebeeoii.snapsolvesudoku.sudoku.solver.Solver
import kotlin.math.max

private const val TAG = "SudokuBoardView"

// TODO Documentations
class SudokuBoardView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val backgroundColour: Int
    private val backgroundPaint: Paint

    private val majorGridDividerColour: Int
    private val majorGridDividerPaint: Paint

    private val minorGridDividerColour: Int
    private val minorGridDividerPaint: Paint

    private val selectedGridColour: Int
    private val selectedGridAlpha: Int
    private val selectedGridPaint: Paint

    private val errorGridColour: Int
    private val errorGridAlpha: Int
    private val errorGridPaint: Paint

    private val sameDigitGridColour: Int
    private val sameDigitGridAlpha: Int
    private val sameDigitGridPaint: Paint

    private val hintDigitColour: Int
    private val hintDigitAlpha: Int
    private val hintDigitPaint: Paint

    private val inputDigitColour: Int
    private val inputDigitAlpha: Int
    private val inputDigitPaint: Paint

    private var cellHeight: Float = 0.0F
    private var cellWidth: Float = 0.0F

    private var sudokuBoard: SudokuBoard
    private lateinit var solutions: Array<SudokuBoard>

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SudokuBoardView, 0, 0).apply {
            try {
                backgroundColour = getColor(
                    R.styleable.SudokuBoardView_backgroundColour,
                    Color.WHITE
                )
                majorGridDividerColour = getColor(
                    R.styleable.SudokuBoardView_majorGridDividerColour,
                    Color.BLACK
                )
                minorGridDividerColour = getColor(
                    R.styleable.SudokuBoardView_minorGridDividerColour,
                    Color.BLACK
                )

                selectedGridColour = getColor(
                    R.styleable.SudokuBoardView_selectedGridColour,
                    Color.LTGRAY
                )
                selectedGridAlpha = getInteger(
                    R.styleable.SudokuBoardView_selectedGridAlpha,
                    80
                )
                errorGridColour = getColor(
                    R.styleable.SudokuBoardView_selectedGridColour,
                    Color.RED
                )
                errorGridAlpha = getInteger(
                    R.styleable.SudokuBoardView_selectedGridAlpha,
                    50
                )
                sameDigitGridColour = getColor(
                    R.styleable.SudokuBoardView_sameDigitGridColour,
                    Color.rgb(82,26,74)
                )
                sameDigitGridAlpha = getInteger(
                    R.styleable.SudokuBoardView_sameDigitGridAlpha,
                    50
                )

                hintDigitColour = getColor(R.styleable.SudokuBoardView_hintDigitColour,
                    Color.argb(100,82, 26, 74)
                )
                hintDigitAlpha = getInteger(
                    R.styleable.SudokuBoardView_hintDigitAlpha,
                    255
                )
                inputDigitColour = getColor(
                    R.styleable.SudokuBoardView_inputDigitColour,
                    Color.DKGRAY
                )
                inputDigitAlpha = getInteger(
                    R.styleable.SudokuBoardView_inputDigitAlpha,
                    100
                )
            } finally {
                recycle()
            }
        }

        backgroundPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = backgroundColour
        }

        majorGridDividerPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = majorGridDividerColour
        }

        minorGridDividerPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = minorGridDividerColour
        }

        selectedGridPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = selectedGridColour
            alpha = selectedGridAlpha
        }

        errorGridPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = errorGridColour
            alpha = errorGridAlpha
        }

        sameDigitGridPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = sameDigitGridColour
            alpha = sameDigitGridAlpha
        }

        hintDigitPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = hintDigitColour
            alpha = hintDigitAlpha
        }

        inputDigitPaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = inputDigitColour
            alpha = inputDigitAlpha
        }

        this.sudokuBoard = SudokuBoard()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawRect(0F, 0F, width.toFloat(), height.toFloat(), backgroundPaint)

        for (x in 1 until 9) {
            if (x % 3 == 0) { //major divider
                canvas?.drawRect(
                    ((x * cellWidth) - 1.5).toFloat(),
                    0F,
                    ((x * cellWidth) + 1.5).toFloat(),
                    height.toFloat(),
                    majorGridDividerPaint
                )
                canvas?.drawRect(
                    0F,
                    ((x * cellWidth) - 1.5).toFloat(),
                    width.toFloat(),
                    ((x * cellWidth) + 1.5).toFloat(),
                    majorGridDividerPaint
                )
            } else { //minor divider
                canvas?.drawRect(
                    ((x * cellWidth) - 0.5).toFloat(),
                    0F,
                    ((x * cellWidth) + 0.5).toFloat(),
                    height.toFloat(),
                    majorGridDividerPaint
                )
                canvas?.drawRect(
                    0F,
                    ((x * cellWidth) - 0.5).toFloat(),
                    width.toFloat(),
                    ((x * cellWidth) + 0.5).toFloat(),
                    majorGridDividerPaint
                )
            }
        }

        // Edges
        canvas?.drawRect(
            (width - 3).toFloat(),
            0F,
            width.toFloat(),
            height.toFloat(),
            majorGridDividerPaint
        )

        canvas?.drawRect(
            0F,
            0F,
            3F,
            height.toFloat(),
            majorGridDividerPaint
        )

        canvas?.drawRect(
            0F,
            (height - 3).toFloat(),
            width.toFloat(),
            height.toFloat(),
            majorGridDividerPaint
        )

        canvas?.drawRect(
            0F,
            0F,
            width.toFloat(),
            3F,
            majorGridDividerPaint
        )

        if (this.sudokuBoard.isEditable()) {
            try {
                canvas?.drawRect(
                    this.sudokuBoard.getSelectedCellCoordinate().calculateXLeftOffset(cellWidth),
                    0F,
                    this.sudokuBoard.getSelectedCellCoordinate().calculateXRightOffset(cellWidth),
                    height.toFloat(),
                    selectedGridPaint
                )

                canvas?.drawRect(
                    0F,
                    this.sudokuBoard.getSelectedCellCoordinate().calculateYTopOffset(cellHeight),
                    width.toFloat(),
                    this.sudokuBoard.getSelectedCellCoordinate().calculateYBottomOffset(cellHeight),
                    selectedGridPaint
                )
            } catch (err: NoSuchElementException) {
                //
            }
        }

        hintDigitPaint.textSize = cellHeight / 2
        inputDigitPaint.textSize = cellHeight / 2

        val offsetWidth = (cellWidth - hintDigitPaint.measureText("9")) / 2
        val offsetHeight = (cellHeight + hintDigitPaint.textSize) / 2

        for (x in 0..8) {
            for (y in 0..8) {
                val cell = this.sudokuBoard.getCell(x, y)

                val digitPaint = if (cell.isGiven()) {
                    hintDigitPaint
                } else {
                    inputDigitPaint
                }

                if (!cell.isEmpty()) {
                    val avgY = cell.getPosition().calculateYTopOffset(cellHeight) + offsetHeight
                    val avgX = cell.getPosition().calculateXLeftOffset(cellWidth) + offsetWidth
                    canvas?.drawText(cell.toString(), avgX, avgY, digitPaint)
                }

                if (!cell.isValid()) {
                    canvas?.drawRect(
                        cell.getPosition().calculateXLeftOffset(cellWidth),
                        cell.getPosition().calculateYTopOffset(cellHeight),
                        cell.getPosition().calculateXRightOffset(cellWidth),
                        cell.getPosition().calculateYBottomOffset(cellHeight),
                        errorGridPaint
                    )
                }

                try {
                    if (!this.sudokuBoard.getSelectedCell().isEmpty() &&
                        cell.valueEquals(this.sudokuBoard.getSelectedCell()) && cell.isValid()) {
                        canvas?.drawRect(
                            cell.getPosition().calculateXLeftOffset(cellWidth),
                            cell.getPosition().calculateYTopOffset(cellHeight),
                            cell.getPosition().calculateXRightOffset(cellWidth),
                            cell.getPosition().calculateYBottomOffset(cellHeight),
                            sameDigitGridPaint
                        )
                    }
                } catch (err: NoSuchElementException) {
                    //
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val height: Float

        val width: Float = widthSize.toFloat()

        height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize.toFloat()
        } else {
            width
        }

        cellHeight = (height / 9.0).toFloat()
        cellWidth = (width / 9.0).toFloat()

        setMeasuredDimension(width.toInt(), height.toInt())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!this.sudokuBoard.isEditable()) {
            return true
        }

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                try {
                    this.sudokuBoard.setSelectedCell(getBoardCoordFromCanvasCoord(x, y))
                } catch (e: CoordinateOutOfBoundsException) {
                    // Coordinates provided did not correspond to any cell.
                } finally {
                    this.invalidate()
                }
            }
        }

        return true
    }

    private fun getBoardCoordFromCanvasCoord(x: Float, y: Float): Coordinate {
        val row = (y / cellHeight).toInt()
        val col = (x / cellWidth).toInt()

        if (row in 0..8 && col in 0..8) {
            return Coordinate(row, col)
        }

        throw CoordinateOutOfBoundsException("No cell corresponds to the coordinates [${x}, ${y}]")
    }

    fun setCell(value: Int) {
        try {
            this.sudokuBoard.setCell(
                this.sudokuBoard.getSelectedCellCoordinate(),
                value,
                true
            )
            this.invalidate()
        } catch (err: NoSuchElementException) {
            //
        }

    }

    fun clearCell() {
        try {
            this.sudokuBoard.clearCell(this.sudokuBoard.getSelectedCellCoordinate())
            this.invalidate()
        } catch (err: NoSuchElementException) {
            //
        }
    }

    fun solve(solverType: Solver.Type, maxSolutions: Int) {
        try {
            val solutions = this.sudokuBoard.solve(solverType, maxSolutions)
            if (solutions.isNotEmpty()) {
                this.sudokuBoard = solutions[0]
            }
            this.sudokuBoard.freeze()
            this.invalidate()
            this.solutions = solutions
        } catch (err: InvalidBoardException) {
            throw err
        } catch (err: UnsolvableBoardException) {
            throw err
        }
    }

    fun getSolutions(): Array<SudokuBoard> {
        return this.solutions
    }

    fun reset() {
        this.sudokuBoard.reset()
        this.invalidate()
    }

    override fun invalidate() {
        this.sudokuBoard.validateBoard()
        super.invalidate()
    }

    fun toString(onlyGivenDigits: Boolean): String {
        return this.sudokuBoard.toString(onlyGivenDigits)
    }
}