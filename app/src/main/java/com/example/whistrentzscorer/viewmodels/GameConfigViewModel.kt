package com.example.whistrentzscorer.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.whistrentzscorer.objects.Game
import com.example.whistrentzscorer.storage.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameConfigViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    // mutable state vars
    var newPlayerName by mutableStateOf("")
        private set

    val players = mutableStateListOf<String>()

    var gameType by mutableStateOf("11..88..11")
        private set

    var bonus by mutableIntStateOf(0)
        private set

    var zeroPointsWins by mutableStateOf(false)

    var gameMode by mutableStateOf("whist")

   // business logic

    suspend fun createNewGame(): Long {
        val newGame = Game(
            timestamp = System.currentTimeMillis(),
            players = getPlayerList(),
            isFinished = false,
            scoresJson = "",
            gameType = gameMode
        )
        return gameRepository.addGame(newGame)
    }

    fun getPlayerList(): List<String> = players.toList()

    fun addPlayer() {
        if (newPlayerName.isNotBlank() && players.size <= 6) {
            val existentPlayers = players.toSet()
            if (!existentPlayers.contains(newPlayerName)) {
                players.add(newPlayerName)
                newPlayerName = ""
            }
        }
    }

    fun removePlayer(player: String) {
        players.remove(player)
    }

    fun onNewPlayerNameSelected(name: String) {
        newPlayerName = name
    }

    fun onGameTypeSelected(newGameType: String) {
        gameType = newGameType
    }

    fun onBonusSelected(selectedBonus: Int) {
        bonus = selectedBonus
    }

    fun onZeroPointsWinsSelected() {
        zeroPointsWins = !zeroPointsWins
    }
}