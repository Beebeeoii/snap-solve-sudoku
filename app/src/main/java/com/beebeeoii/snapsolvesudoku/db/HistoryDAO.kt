package com.beebeeoii.snapsolvesudoku.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class HistoryDAO {
    @Insert
    abstract fun insertHistoryEntry(historyEntity: HistoryEntity)

    @Delete
    abstract fun deleteHistoryEntry(historyEntity: HistoryEntity)

    @Query("SELECT * FROM HistoryEntry")
    abstract fun getAllHistoryEntry(): LiveData<List<HistoryEntity>>

    @Query("SELECT * FROM HistoryEntry WHERE uniqueId=:uniqueId")
    abstract fun getSpecificEntry(uniqueId: String): LiveData<List<HistoryEntity>>

    @Query("UPDATE HistoryEntry SET processedPicturePath=:processedPicturePath WHERE uniqueId=:uniqueId")
    abstract fun updateProcessedPicturePath(uniqueId: String, processedPicturePath: String?)

    @Query("UPDATE HistoryEntry SET solutionsPath=:solutionsPath WHERE uniqueId=:uniqueId")
    abstract fun updateSolutions(uniqueId: String, solutionsPath: String?)

    @Query("UPDATE HistoryEntry SET recognisedDigits=:recognisedDigits WHERE uniqueId=:uniqueId")
    abstract fun updateRecognisedDigits(uniqueId: String, recognisedDigits: String?)

    @Query("UPDATE HistoryEntry SET timeTakenToSolve=:timeTakenToSolve WHERE uniqueId=:uniqueId")
    abstract fun updateTimeTakenToSolve(uniqueId: String, timeTakenToSolve: Int?)

    @Query("SELECT EXISTS(SELECT * FROM HistoryEntry WHERE uniqueId=:uniqueId)")
    abstract fun doesEntryExist(uniqueId: String): Boolean
}