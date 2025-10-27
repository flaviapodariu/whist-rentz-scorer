package com.example.whistrentzscorer.storage.repository

import com.example.whistrentzscorer.objects.Game
import com.example.whistrentzscorer.storage.dao.GameDao
import com.example.whistrentzscorer.storage.entity.GameEntity
import com.example.whistrentzscorer.storage.entity.toDomain
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GameRepository @Inject constructor(
    private val gameDao: GameDao
) {
    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()

    suspend fun addGame(game: GameEntity): Long {
        return gameDao.insertGame(game)
    }

    suspend fun updateScore(gameId: Int, score: String) {
        return gameDao.updateScore(gameId, score)
    }

    suspend fun getGameById(gameId: Int): Game? {
        return gameDao.getGameById(gameId)?.toDomain()
    }

    suspend fun getLastUnfinishedGame(): Game? {
        return gameDao.getLastUnfinishedGame()?.toDomain()
    }

    suspend fun deleteGame(gameId: Int) {
        gameDao.deleteGame(gameId)
    }
}