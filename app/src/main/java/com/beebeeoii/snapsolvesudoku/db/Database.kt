package com.beebeeoii.snapsolvesudoku.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [HistoryEntity::class], version = 2)

abstract class Database : RoomDatabase() {

    abstract fun getHistoryDao(): HistoryDAO

    companion object {
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE HistoryEntry "
                            + " ADD COLUMN timeTakenToSolve INT"
                )
            }
        }

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
            ).addMigrations(MIGRATION_1_2).build()
    }
}