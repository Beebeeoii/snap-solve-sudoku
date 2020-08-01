package com.example.snapsolvesudoku.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDAO {

    @Insert
    suspend fun insertHistoryEntry(historyEntity: HistoryEntity)

    @Delete
    suspend fun deleteHistoryEntry(historyEntity: HistoryEntity)

    @Query("SELECT * FROM HistoryEntry")
    fun getAllHistoryEntry(): LiveData<List<HistoryEntity>>

    @Query("SELECT * FROM HistoryEntry WHERE date=:date AND time=:time")
    fun getSpecificEntry(date: String, time: String): LiveData<HistoryEntity>
}