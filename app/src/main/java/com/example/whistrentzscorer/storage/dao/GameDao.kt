package com.example.whistrentzscorer.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.whistrentzscorer.storage.entity.GameEntity
import com.example.whistrentzscorer.utils.GameMode
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Query("SELECT * FROM games ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE gameMode = :gameMode ORDER BY timestamp DESC")
    fun getAllGamesByMode(gameMode: GameMode): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isFinished = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastUnfinishedGame(): GameEntity?

    @Query("SELECT * FROM games WHERE isFinished = 0 AND gameMode = :gameMode ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastUnfinishedGameByType(gameMode: GameMode): GameEntity?

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: Int): GameEntity?

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Int)

    @Query("UPDATE games SET scoresJson = :score WHERE id = :gameId")
    suspend fun updateScore(gameId: Int, score: String)

    @Query("UPDATE games SET elapsedTime = :elapsedTime WHERE id = :gameId")
    suspend fun updateElapsedTime(gameId: Int, elapsedTime: Long)
}