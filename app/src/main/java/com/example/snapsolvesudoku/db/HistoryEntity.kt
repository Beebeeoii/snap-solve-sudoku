package com.example.snapsolvesudoku.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HistoryEntry")
data class HistoryEntity (
    @PrimaryKey
    val uniqueId: String,
    val dateTime: String, //ddMMyyHHmmSS
    val folderPath: String,
    val originalPicturePath: String ?= null,
    val processedPicturePath: String ?= null,
    val recognisedDigits: String ?= null, //"002001000200010006008020...." etc
    val solutionsPath: String ?= null
)
