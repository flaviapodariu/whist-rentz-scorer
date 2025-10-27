package com.example.whistrentzscorer.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.whistrentzscorer.storage.entity.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Query("SELECT * FROM games ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isFinished = 1 ORDER BY timestamp DESC")
    suspend fun getLastUnfinishedGame(): GameEntity?

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: Int): GameEntity?

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Int)

    @Query("UPDATE games SET scoresJson = :score WHERE id = :gameId")
    suspend fun updateScore(gameId: Int, score: String)
}