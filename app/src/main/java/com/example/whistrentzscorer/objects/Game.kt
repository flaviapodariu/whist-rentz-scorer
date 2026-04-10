package com.example.whistrentzscorer.objects

import com.example.whistrentzscorer.storage.entity.GameEntity
import com.example.whistrentzscorer.utils.GameMode
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: Int = 0,
    val timestamp: Long,
    val players: List<String>,
    val isFinished: Boolean,
    val scoresJson: String,
    val gameMode: GameMode = GameMode.WHIST,
    val elapsedTime: Long = 0L
)

fun Game.toEntity(id: Int = 0): GameEntity {
    return GameEntity(
        id = id,
        timestamp = timestamp,
        players = players.joinToString(separator = ","),
        isFinished = isFinished,
        scoresJson = scoresJson,
        gameMode = gameMode,
        elapsedTime = elapsedTime
    )
}