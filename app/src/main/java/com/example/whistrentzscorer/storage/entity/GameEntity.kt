package com.example.whistrentzscorer.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.whistrentzscorer.objects.Game

@Entity(
    tableName = "games"
)
data class GameEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val players: String,
    val isFinished: Boolean,
    val scoresJson: String,
    val gameType: String = "whist",
    val elapsedTime: Long = 0L
) {
    fun parsePlayers(): List<String> {
        return players.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
}



fun GameEntity.toDomain(): Game {
    return Game(
        id = id,
        timestamp = timestamp,
        scoresJson = scoresJson,
        isFinished = isFinished,
        players = parsePlayers(),
        gameType = gameType,
        elapsedTime = elapsedTime
    )
}