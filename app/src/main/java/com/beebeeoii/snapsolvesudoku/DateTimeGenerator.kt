package com.beebeeoii.snapsolvesudoku

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

object DateTimeGenerator {
    const val DATE = 1
    const val TIME = 2
    const val DATE_AND_TIME = 3

    fun generateDateTime(type: Int) : String {
        val dateTime = LocalDateTime.now()

        return when (type) {
            DATE -> dateTime.format(DateTimeFormatter.ofPattern("ddMMyy"))
            TIME -> dateTime.format(DateTimeFormatter.ofPattern("HHmmss"))
            DATE_AND_TIME -> dateTime.format(DateTimeFormatter.ofPattern("ddMMyyHHmmss"))
            else -> "000000000000"
        }
    }

    fun getDayOfWeekFromDateTime(dateTime: String) : String {
        val dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("ddMMyyHHmmss"))
        return dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    //eg 01 August 2020
    fun getFormattedDate(dateTime: String) : String {
        val dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("ddMMyyHHmmss"))
        val day = dateTime.dayOfMonth
        val month = dateTime.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val year = dateTime.year
        return "$day $month $year"
    }

}