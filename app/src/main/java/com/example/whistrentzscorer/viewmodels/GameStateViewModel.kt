package com.example.whistrentzscorer.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whistrentzscorer.storage.repository.IGameRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class GameStateViewModel @Inject constructor(
    private val gameRepository: IGameRepository
) : ViewModel() {

    var game by mutableStateOf(GameState())
        private set

    lateinit var playerList: List<String>
        private set
    var totalRounds: Int = 0
        private set
    var currentRound: Int by mutableIntStateOf(1)

    var currentRoundCards: Int by mutableIntStateOf(1)

    var gameType: String = "11..88..11"
        private set

    var gameId: Int = 0
        private set

    private val gson = Gson()

    private fun updateCurrentRoundCards() {
        currentRoundCards = cardsThisRound(currentRound, gameType)
    }

    fun advanceRound() {
        currentRound += 1
        updateCurrentRoundCards()
        autoSave()
    }

    fun init(players: List<String>, gameType: String = "11..88..11") {
        playerList = players
        this.gameType = gameType
        // 2 rounds with 1 card + 1 round with 8 cards per player + the rest 2-7 cards (up and down)
        totalRounds = players.size * 3 + 12

        for (round in 1 until totalRounds+1) {
            game.state[round] = players.associateWith { RoundState() }.toMutableMap()
        }
        updateCurrentRoundCards()
    }


    fun getCurrentPlayer(): Int {
       return (currentRound - 1) % playerList.size
    }

    fun getGameState(): GameState = game

    fun getRoundStateForPlayer(round: Int, playerIndex: Int): RoundState {
        val player = playerList[playerIndex]

        return game.state[round]?.get(player)?: RoundState()
    }

    fun setBid(round: Int, playerIndex: Int, bid: Int) {
        val player = playerList[playerIndex]
        game.state[round]?.get(player)?.bid = bid
    }

    fun setHandsTaken(round: Int, playerIndex: Int, hands: Int) {
        val player = playerList[playerIndex]
        game.state[round]?.get(player)?.handsTaken = hands
    }

    fun saveRoundScore(round: Int) {
        playerList.forEach { player ->
            val bid = game.state[round]?.get(player)?.bid?: 0
            val handsTaken = game.state[round]?.get(player)?.handsTaken?: 0
            val scoreLastRound = game.state[round - 1]?.get(player)?.score ?:0
            game.state[round]?.get(player)?.score = scoreLastRound + whistScoring(bid, handsTaken)

        }
    }

    fun undoLastTurn() {
        if (currentRound <= 1) return

        val currentRoundHasResults = game.state[currentRound]?.values
            ?.any { it.score != null } == true

        if (currentRoundHasResults) {
            // Current round is fully completed — clear it
            clearRound(currentRound)
        } else {
            // Current round only has bids (or nothing) — clear it and go back one round
            clearRound(currentRound)
            currentRound -= 1
            clearRound(currentRound)
        }
        updateCurrentRoundCards()
        autoSave()
    }

    private fun clearRound(round: Int) {
        val state = game.state[round] ?: return
        game.state[round] = state.keys.associateWith { RoundState() }.toMutableMap()
    }

    fun setGameId(id: Int) {
        gameId = id
    }

    fun autoSave() {
        if (gameId == 0) return
        viewModelScope.launch {
            val saveData = SaveData(
                currentRound = currentRound,
                gameType = gameType,
                state = game.state.mapKeys { (k, _) -> k.toString() }.mapValues { (_, playerMap) ->
                    playerMap.mapValues { (_, rs) ->
                        SerializableRoundState(rs.bid, rs.handsTaken, rs.score)
                    }
                }
            )
            val json = gson.toJson(saveData)
            gameRepository.updateScore(gameId, json)
        }
    }

    fun restoreGame(id: Int, players: List<String>, scoresJson: String) {
        gameId = id
        
        // If no saved state, initialize a fresh game
        if (scoresJson.isBlank()) {
            init(players)
            return
        }

        try {
            val type = object : TypeToken<SaveData>() {}.type
            val saveData: SaveData = gson.fromJson(scoresJson, type)

            playerList = players
            gameType = saveData.gameType
            currentRound = saveData.currentRound
            totalRounds = players.size * 3 + 12

            game = GameState()
            val stateByInt = saveData.state.mapKeys { (k, _) -> k.toInt() }
            for (round in 1..totalRounds) {
                val savedRound = stateByInt[round]
                if (savedRound != null) {
                    game.state[round] = savedRound.mapValues { (_, srs) ->
                        RoundState(bid = srs.bid, handsTaken = srs.handsTaken, score = srs.score)
                    }.toMutableMap()
                } else {
                    game.state[round] = players.associateWith { RoundState() }.toMutableMap()
                }
            }
            updateCurrentRoundCards()
        } catch (e: Exception) {
            // If parsing fails, initialize a fresh game
            init(players)
        }
    }

    fun cardsThisRound(
        round: Int,
        gameType: String,
    ): Int {
        val playerCount = playerList.size
        val roundTypes = gameType.split("..")
        // first character in 11 / 88 represents hand card number
        val startingRound = Integer.parseInt(roundTypes[0][0].toString())
        val midRound = Integer.parseInt(roundTypes[1][0].toString())

        // start
        if (round in 1..playerCount) return startingRound

        // up
        if (round in playerCount+1..playerCount + 6) {
            if (startingRound == 1)
                return round - playerCount + startingRound
            if (startingRound == 8)
                return startingRound - (round - playerCount)
        }

        // mid
        val middleRoundsEnd = 2 * playerCount + 6
        if (round in playerCount + 7..middleRoundsEnd) return midRound


        // 15 -> 7  -8
        // 16 -> 6  -10
        // 17 -> 5   -12

        // down
        val startEndRounds = middleRoundsEnd + 6
        if (round in middleRoundsEnd + 1..startEndRounds) {
            if (startingRound == 1)
                return middleRoundsEnd + midRound - round
            if (startingRound == 8)
                 return round - playerCount - startingRound - 1
        }

        if (round in startEndRounds + 1.. startEndRounds + playerCount) {
            return startingRound
        }
        // startingRound can only be 1 or 8
        return 0
    }
}

data class GameState(
    var state: MutableMap<Int, MutableMap<String, RoundState>> = mutableStateMapOf()
)

data class RoundState(
    var score: Int? = null,
    var bid: Int? = null,
    var handsTaken: Int? = null
)

enum class RoundActions() {
    BID,
    RESULTS
}

fun whistScoring(bid: Int, handsTaken: Int): Int {
    if (bid == handsTaken) {
        return 5 + handsTaken
    }
    return -abs(bid - handsTaken)
}

data class SerializableRoundState(
    val bid: Int? = null,
    val handsTaken: Int? = null,
    val score: Int? = null
)

data class SaveData(
    val currentRound: Int,
    val gameType: String,
    val state: Map<String, Map<String, SerializableRoundState>>
)