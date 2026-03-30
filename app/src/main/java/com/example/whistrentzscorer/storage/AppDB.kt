package com.example.whistrentzscorer.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.whistrentzscorer.storage.dao.GameDao
import com.example.whistrentzscorer.storage.entity.GameEntity

@Database(
    entities = [
        GameEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDB : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN gameType TEXT NOT NULL DEFAULT 'whist'")
            }
        }

        fun getDatabase(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "flyscore_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}