package com.beebeeoii.snapsolvesudoku.sudokuboard

class Cell(value: Int = 0, isGiven: Boolean = false, isValid: Boolean = true, row: Int = 0, col: Int = 0) {

    var row = 0
    var column = 0

    var value = 0
    var isGiven = false
    var isValid = true
    var confidence = 0F


    init {
        this.value = value
        this.isGiven = isGiven
        this.isValid = isValid
        this.row = row
        this.column = col
    }
}