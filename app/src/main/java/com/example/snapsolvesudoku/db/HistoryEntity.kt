package com.example.snapsolvesudoku.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HistoryEntry")
data class HistoryEntity (
    @PrimaryKey
    val picturePath: String,
    val day: String,
    val date: String,
    val time: String,
    val solutions: List<String>
)
