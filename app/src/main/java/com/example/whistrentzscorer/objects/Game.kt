package com.example.whistrentzscorer.objects

import com.example.whistrentzscorer.storage.entity.GameEntity
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: Int = 0,
    val timestamp: Long,
    val players: List<String>,
    val isFinished: Boolean,
    val scoresJson: String,
    val gameType: String = "whist"
)

fun Game.toEntity(id: Int = 0): GameEntity {
    return GameEntity(
        id = id,
        timestamp = timestamp,
        players = players.joinToString(separator = ","),
        isFinished = isFinished,
        scoresJson = scoresJson,
        gameType = gameType
    )
}