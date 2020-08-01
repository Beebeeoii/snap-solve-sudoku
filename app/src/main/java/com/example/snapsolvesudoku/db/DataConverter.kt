package com.example.snapsolvesudoku.db

import androidx.room.TypeConverter

class DataConverter {
    @TypeConverter
    fun fromSolutionListToString(solutionList: List<String>) : String {
        return solutionList.joinToString(separator = ",")
    }

    @TypeConverter
    fun fromSolutionStringToList(solutionString: String) : List<String> {
        return solutionString.split(",")
    }
}