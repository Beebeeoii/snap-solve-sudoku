package com.beebeeoii.snapsolvesudoku.sudoku.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.beebeeoii.snapsolvesudoku.R
import com.google.android.material.button.MaterialButton

/**
 * SudokuKeyboardView is a View that contains buttons to input digits 1-9 into a SudokuBoard.
 *
 * @constructor Initialises the keyboard with 9 corresponding properly styled buttons for the
 *              inputs.
 * @property mSudokuKeyboardListener Listener for the respective input buttons in this keyboard.
 *
 * @author Jia Wei Lee
 */
class SudokuKeyboardView(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs) {
    private var mSudokuKeyboardListener: ISudokuKeyboardListener? = null

    /**
     * Functional interface for this keyboard listener.
     */
    @FunctionalInterface
    interface ISudokuKeyboardListener {
        fun onInput(input: Int)
    }

    /**
     * Sets the keyboard listener.
     *
     * @param mSudokuKeyboardListener The keyboard listener.
     */
    fun setOnSudokuKeyboardListener(mSudokuKeyboardListener: ISudokuKeyboardListener) {
        this.mSudokuKeyboardListener = mSudokuKeyboardListener
    }

    init {
        orientation = HORIZONTAL
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        gravity = Gravity.CENTER

        val buttonStyle: Int = R.style.Widget_Material3_Button_TextButton
        val buttonTextColour: Int = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        val buttonLayoutParams: ViewGroup.LayoutParams = LayoutParams(0,
            LayoutParams.WRAP_CONTENT, 1.0f)

        val buttons: Array<MaterialButton> = arrayOf(
            MaterialButton(ContextThemeWrapper(context, buttonStyle), attrs, buttonStyle),
            MaterialButton(ContextThemeWrapper(context, buttonStyle), attrs, buttonStyle),
            MaterialButton(ContextThemeWrapper(context, buttonStyle), attrs, buttonStyle),
            MaterialButton(ContextThemeWrapper(context, buttonStyle), attrs, buttonStyle),
            MaterialButton(ContextThemeWrapper(context, buttonStyle), attrs, buttonStyle),
            MaterialButton(ContextThemeWrapper(context, buttonStyle), attrs, buttonStyle),
            MaterialButton(ContextThemeWrapper(context, buttonStyle), attrs, buttonStyle),
            MaterialButton(ContextThemeWrapper(context, buttonStyle), attrs, buttonStyle),
            MaterialButton(ContextThemeWrapper(context, buttonStyle), attrs, buttonStyle)
        )

        for ((index, button) in buttons.withIndex()) {
            val inputDigit = index + 1
            val text = "$inputDigit"

            button.text = text
            button.textSize = 14f
            button.typeface = Typeface.DEFAULT_BOLD
            button.setTextColor(buttonTextColour)
            button.layoutParams = buttonLayoutParams
            button.setBackgroundColor(Color.TRANSPARENT)
            button.rippleColor = ContextCompat.getColorStateList(context, R.color.colorSecondaryLight)
            button.setOnClickListener {
                mSudokuKeyboardListener?.onInput(inputDigit)
            }

            addView(button)
        }
    }
}