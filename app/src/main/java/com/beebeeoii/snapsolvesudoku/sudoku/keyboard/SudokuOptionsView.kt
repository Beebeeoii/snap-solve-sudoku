package com.beebeeoii.snapsolvesudoku.sudoku.keyboard

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.beebeeoii.snapsolvesudoku.R
import com.google.android.material.button.MaterialButton

/**
 * SudokuOptionsView is a View that contains utility buttons clear a cell, solve the board and
 * clear the board.
 *
 * @constructor Initialises the keyboard with buttons to clear a cell, solve the board and clear the
 *              board.
 * @property mSudokuOptionsListener Listener for the respective options buttons.
 *
 * @author Jia Wei Lee
 */
class SudokuOptionsView(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs) {
    /**
     * Options available in this options view.
     */
    enum class Options {
        CLEAR_CELL, SOLVE, CLEAR_BOARD
    }

    private var mSudokuOptionsListener: ISudokuOptionsListener? = null

    /**
     * Functional interface for this options listener.
     */
    @FunctionalInterface
    interface ISudokuOptionsListener {
        fun onOptionClick(option: Options)
    }

    /**
     * Sets the options listener.
     *
     * @param mSudokuOptionsListener The keyboard listener.
     */
    fun setOnSudokuOptionsListener(mSudokuOptionsListener: ISudokuOptionsListener) {
        this.mSudokuOptionsListener = mSudokuOptionsListener
    }

    init {
        orientation = HORIZONTAL
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        gravity = Gravity.CENTER

        val textButtonStyle: Int = R.style.Widget_Material3_Button_IconButton
        val outlinedButtonStyle: Int = R.style.Widget_Material3_Button_OutlinedButton

        val clearCellButton = MaterialButton(ContextThemeWrapper(context, textButtonStyle),
            attrs, textButtonStyle)
        val solveBoardButton = MaterialButton(ContextThemeWrapper(context, outlinedButtonStyle),
            attrs, outlinedButtonStyle)
        val clearBoardButton = MaterialButton(ContextThemeWrapper(context, textButtonStyle),
            attrs, textButtonStyle)

        clearCellButton.setOnClickListener {
            mSudokuOptionsListener?.onOptionClick(Options.CLEAR_CELL)
        }

        solveBoardButton.setOnClickListener {
            mSudokuOptionsListener?.onOptionClick(Options.SOLVE)
        }

        clearBoardButton.setOnClickListener {
            mSudokuOptionsListener?.onOptionClick(Options.CLEAR_BOARD)
        }

        addView(styleButton(clearCellButton, R.drawable.ic_clear_cell_24px))
        addView(styleButton(solveBoardButton, -1, "Solve", 2f))
        addView(styleButton(clearBoardButton, R.drawable.ic_clear_board_24px))
    }

    /**
     * Styles a button with an icon, text, weight, colour (background, icon tint, stroke, text and
     * ripple)
     *
     * @param button Button object to be styled.
     * @param icon Drawable resource to be used as the icon.
     * @param text Button text to be shown.
     * @param weight Weight of the button in the linear layout.
     *
     * @return A fully styled Material Button.
     */
    private fun styleButton(button: MaterialButton, icon: Int = -1, text: String = "",
                            weight: Float = 1.0f): MaterialButton {
        val buttonBackground = Color.TRANSPARENT
        val buttonRippleColour = ContextCompat.getColorStateList(context,
            R.color.colorSecondaryLight)
        val buttonTextColour: Int = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        val buttonLayoutParams: ViewGroup.LayoutParams = LayoutParams(0,
            LayoutParams.WRAP_CONTENT, weight)

        if (icon != -1) {
            button.icon = AppCompatResources.getDrawable(context, icon)
            button.iconTint = ColorStateList.valueOf(buttonTextColour)
        }

        if (text != "") {
            button.text = text
            button.typeface = Typeface.DEFAULT_BOLD
            button.textSize = 16f
        }

        // This is crucial else every button will have the same id causing app to crash.
        button.id = generateViewId()
        button.setBackgroundColor(buttonBackground)
        button.layoutParams = buttonLayoutParams
        button.rippleColor = buttonRippleColour
        button.setTextColor(buttonTextColour)
        button.strokeColor = ColorStateList.valueOf(buttonTextColour)
        button.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        button.iconPadding = 0

        return button
    }
}