package com.example.whistrentzscorer.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whistrentzscorer.objects.RentzMiniGame
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

    var gameMode: String by mutableStateOf("whist")
        private set

    var selectedMiniGame: RentzMiniGame? by mutableStateOf(null)
        private set

    val playedMiniGames = mutableSetOf<RentzMiniGame>()

    // Rentz cumulative scores per player
    val rentzScores = mutableStateMapOf<String, Int>()

    private val gson = Gson()

    fun selectMiniGame(miniGame: RentzMiniGame) {
        selectedMiniGame = miniGame
    }

    fun submitRentzRoundScores(roundScores: Map<String, Int>) {
        val miniGame = selectedMiniGame ?: return
        roundScores.forEach { (player, score) ->
            rentzScores[player] = (rentzScores[player] ?: 0) + score
        }
        playedMiniGames.add(miniGame)
        selectedMiniGame = null
        autoSave()
    }

    fun isRentzGame(): Boolean = gameMode == "rentz"

    private fun updateCurrentRoundCards() {
        currentRoundCards = cardsThisRound(currentRound, gameType, playerList.size)
    }

    fun advanceRound() {
        currentRound += 1
        updateCurrentRoundCards()
        autoSave()
    }

    fun init(players: List<String>, gameType: String = "11..88..11", gameMode: String = "whist") {
        playerList = players
        this.gameType = gameType
        this.gameMode = gameMode

        if (gameMode == "rentz") {
            // Rentz has 8 sub-games per player who calls
            totalRounds = 0
            rentzScores.clear()
            playedMiniGames.clear()
            players.forEach { rentzScores[it] = 0 }
        } else {
            // 2 rounds with 1 card + 1 round with 8 cards per player + the rest 2-7 cards (up and down)
            totalRounds = players.size * 3 + 12

            for (round in 1 until totalRounds + 1) {
                game.state[round] = players.associateWith { RoundState() }.toMutableMap()
            }
            updateCurrentRoundCards()
        }
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
            clearRound(currentRound)
        } else {
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

    fun cardsThisRound(round: Int, gameType: String, playerCount: Int): Int {
        val parts = gameType.split("..")
        val start = parts[0][0].digitToInt() // 1 or 8
        val mid = parts[1][0].digitToInt()   // 8 or 1

        val ramp = if (start < mid) (start + 1 until mid).toList()
                   else (start - 1 downTo mid + 1).toList()

        val sequence = buildList {
            repeat(playerCount) { add(start) }
            addAll(ramp)
            repeat(playerCount) { add(mid) }
            addAll(ramp.reversed())
            repeat(playerCount) { add(start) }
        }

        return sequence.getOrElse(round - 1) { 0 }
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