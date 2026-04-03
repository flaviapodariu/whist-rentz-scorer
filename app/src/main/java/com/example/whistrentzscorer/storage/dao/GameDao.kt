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

    @Query("SELECT * FROM games WHERE gameType = :gameType ORDER BY timestamp DESC")
    fun getAllGamesByType(gameType: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isFinished = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastUnfinishedGame(): GameEntity?

    @Query("SELECT * FROM games WHERE isFinished = 0 AND gameType = :gameType ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastUnfinishedGameByType(gameType: String): GameEntity?

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: Int): GameEntity?

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Int)

    @Query("UPDATE games SET scoresJson = :score WHERE id = :gameId")
    suspend fun updateScore(gameId: Int, score: String)

    @Query("UPDATE games SET elapsedTime = :elapsedTime WHERE id = :gameId")
    suspend fun updateElapsedTime(gameId: Int, elapsedTime: Long)
}