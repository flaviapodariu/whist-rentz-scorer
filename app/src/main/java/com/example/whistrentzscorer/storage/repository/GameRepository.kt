package com.example.whistrentzscorer.storage.repository

import com.example.whistrentzscorer.objects.Game
import com.example.whistrentzscorer.objects.toEntity
import com.example.whistrentzscorer.storage.dao.GameDao
import com.example.whistrentzscorer.storage.entity.GameEntity
import com.example.whistrentzscorer.storage.entity.toDomain
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface IGameRepository {
    val allGames: Flow<List<GameEntity>>
    fun allGamesByType(gameType: String): Flow<List<GameEntity>>
    suspend fun addGame(game: Game): Long
    suspend fun updateScore(gameId: Int, score: String)
    suspend fun updateElapsedTime(gameId: Int, elapsedTime: Long)
    suspend fun getGameById(gameId: Int): Game?
    suspend fun getLastUnfinishedGame(): Game?
    suspend fun getLastUnfinishedGameByType(gameType: String): Game?
    suspend fun deleteGame(gameId: Int)
}

class GameRepository @Inject constructor(
    private val gameDao: GameDao
) : IGameRepository {
    override val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()

    override fun allGamesByType(gameType: String): Flow<List<GameEntity>> {
        return gameDao.getAllGamesByType(gameType)
    }

    override suspend fun addGame(game: Game): Long {
        return gameDao.insertGame(game.toEntity())
    }

    override suspend fun updateScore(gameId: Int, score: String) {
        return gameDao.updateScore(gameId, score)
    }

    override suspend fun updateElapsedTime(gameId: Int, elapsedTime: Long) {
        return gameDao.updateElapsedTime(gameId, elapsedTime)
    }

    override suspend fun getGameById(gameId: Int): Game? {
        return gameDao.getGameById(gameId)?.toDomain()
    }

    override suspend fun getLastUnfinishedGame(): Game? {
        return gameDao.getLastUnfinishedGame()?.toDomain()
    }

    override suspend fun getLastUnfinishedGameByType(gameType: String): Game? {
        return gameDao.getLastUnfinishedGameByType(gameType)?.toDomain()
    }

    override suspend fun deleteGame(gameId: Int) {
        gameDao.deleteGame(gameId)
    }
}