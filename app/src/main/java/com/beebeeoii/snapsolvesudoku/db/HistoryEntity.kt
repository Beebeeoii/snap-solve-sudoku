package com.beebeeoii.snapsolvesudoku.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HistoryEntry")
data class HistoryEntity (
    @PrimaryKey val uniqueId: String,
    @ColumnInfo(name = "dateTime") val dateTime: String, //ddMMyyHHmmSS
    @ColumnInfo(name = "folderPath") val folderPath: String,
    @ColumnInfo(name = "originalPicturePath") val originalPicturePath: String?,
    @ColumnInfo(name = "processedPicturePath") val processedPicturePath: String?,
    @ColumnInfo(name = "recognisedDigits") val recognisedDigits: String?, //"002001000200010006008020...." etc
    @ColumnInfo(name = "solutionsPath") val solutionsPath: String?,
    @ColumnInfo(name = "timeTakenToSolve") val timeTakenToSolve: Int? //millis
)
