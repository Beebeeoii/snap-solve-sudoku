package com.example.snapsolvesudoku

class Cell(value: Int = 0, isGiven: Boolean = false, isValid: Boolean = true, row: Int = 0, col: Int = 0) {

    var row: Int = 0
    var column: Int = 0

    var value: Int = 0
    var isGiven: Boolean = false
    var isValid: Boolean = true


    init {
        this.value = value
        this.isGiven = isGiven
        this.isValid = isValid
        this.row = row
        this.column = col
    }

}