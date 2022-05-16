package com.beebeeoii.snapsolvesudoku.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

private const val DATE_PATTERN = "ddMMyy"
private const val TIME_PATTERN = "HHmmss"
private const val DATE_AND_TIME_PATTERN = "ddMMyyHHmmss"

// TODO Documentations
object DateTimeGenerator {
    enum class Mode {
        DATE,
        TIME,
        DATE_AND_TIME;
    }

    fun generateDateTimeString(dateTimeObject: LocalDateTime, mode: Mode) : String {
        return when (mode) {
            Mode.DATE -> dateTimeObject.format(DateTimeFormatter.ofPattern(DATE_PATTERN))
            Mode.TIME -> dateTimeObject.format(DateTimeFormatter.ofPattern(TIME_PATTERN))
            Mode.DATE_AND_TIME -> dateTimeObject.format(DateTimeFormatter.ofPattern(DATE_AND_TIME_PATTERN))
        }
    }

    fun generateDayOfWeek(dateTimeObject: LocalDateTime) : String {
        return dateTimeObject.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }

    //eg 01 August 2020
    fun generateFormattedDateTimeString(dateTimeObject: LocalDateTime) : String {
        val day = dateTimeObject.dayOfMonth
        val month = dateTimeObject.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val year = dateTimeObject.year
        return "$day $month $year"
    }

    fun getDateTimeObjectFromDateTimeString(dateTimeString: String) : LocalDateTime {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(DATE_AND_TIME_PATTERN))
    }
}