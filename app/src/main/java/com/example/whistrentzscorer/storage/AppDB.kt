package com.example.whistrentzscorer.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.whistrentzscorer.storage.dao.GameDao
import com.example.whistrentzscorer.storage.entity.GameEntity

@Database(
    entities = [
        GameEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDB : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null


        fun getDatabase(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "flyscore_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}