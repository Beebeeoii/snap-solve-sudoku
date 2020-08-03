package com.beebeeoii.snapsolvesudoku.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntity::class], version = 1)

abstract class Database : RoomDatabase() {

    abstract fun getHistoryDao(): HistoryDAO

    companion object {
        @Volatile
        private var instance: com.beebeeoii.snapsolvesudoku.db.Database?= null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance
            ?: synchronized(LOCK) {
            instance
                ?: createDatabase(
                    context
                ).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                com.beebeeoii.snapsolvesudoku.db.Database::class.java,
                "history_db"
            ).build()
    }
}