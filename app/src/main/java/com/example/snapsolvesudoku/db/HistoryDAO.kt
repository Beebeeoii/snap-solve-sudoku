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

    @Query("SELECT * FROM HistoryEntry WHERE uniqueId=:uniqueId")
    fun getSpecificEntry(uniqueId: String): LiveData<List<HistoryEntity>>

    @Query("UPDATE HistoryEntry SET processedPicturePath=:processedPicturePath WHERE uniqueId=:uniqueId")
    fun updateProcessedPicturePath(uniqueId: String, processedPicturePath: String?)

    @Query("UPDATE HistoryEntry SET solutionsPath=:solutionsPath WHERE uniqueId=:uniqueId")
    fun updateSolutions(uniqueId: String, solutionsPath: String?)

    @Query("UPDATE HistoryEntry SET recognisedDigits=:recognisedDigits WHERE uniqueId=:uniqueId")
    fun updateRecognisedDigits(uniqueId: String, recognisedDigits: String?)

    @Query("SELECT EXISTS(SELECT * FROM HistoryEntry WHERE uniqueId=:uniqueId)")
    fun doesEntryExist(uniqueId: String): Boolean
}