package com.example.whistrentzscorer.viewmodels.whist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.whistrentzscorer.objects.Game
import com.example.whistrentzscorer.storage.repository.IGameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameConfigViewModel @Inject constructor(
    private val gameRepository: IGameRepository
) : ViewModel() {

    val players = mutableStateListOf<String>()

    var gameType by mutableStateOf("11..88..11")
        private set

    var bonus by mutableIntStateOf(0)
        private set

    var zeroPointsWins by mutableStateOf(false)

    var gameMode by mutableStateOf("whist")


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

    var currentPlayerIndex by mutableIntStateOf(0)
        private set

    fun getPlayerList(): List<String> = players.toList()

    fun setPlayerName(index: Int, name: String) {
        if (index < players.size) {
            players[index] = name
        } else if (index == players.size) {
            players.add(name)
        }
    }

    fun getPlayerName(index: Int): String {
        return if (index < players.size) players[index] else ""
    }

    fun goToNextPlayer() {
        currentPlayerIndex++
    }

    fun goToPrevPlayer() {
        if (currentPlayerIndex > 0) {
            currentPlayerIndex--
        }
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