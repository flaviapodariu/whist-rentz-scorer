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
    val scoresJson: String
)

fun GameEntity.toDomain(): Game {
    return Game(
        id = id,
        timestamp = timestamp,
        scoresJson = scoresJson,
        isFinished = isFinished,
        players = players.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    )
}