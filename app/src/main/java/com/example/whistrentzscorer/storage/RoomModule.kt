package com.example.whistrentzscorer.storage

import android.content.Context
import com.example.whistrentzscorer.storage.dao.GameDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDB {
        return AppDB.getDatabase(context)
    }

    @Provides
    fun provideGameDao(database: AppDB): GameDao {
        return database.gameDao()
    }
}