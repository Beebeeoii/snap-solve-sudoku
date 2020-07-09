package com.example.snapsolvesudoku

class Cell(value: Int = 0, editable: Boolean = true, isValid: Boolean = true, row: Int = 0, col: Int = 0) {

    var row: Int = 0
    var column: Int = 0

    var value: Int = 0
    var editable: Boolean = true
    var isValid: Boolean = true


    init {
        this.value = value
        this.editable = editable
        this.isValid = isValid
        this.row = row
        this.column = col
    }

}