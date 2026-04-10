package com.example.whistrentzscorer.viewmodels.whist.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whistrentzscorer.objects.RentzMiniGame
import com.example.whistrentzscorer.storage.repository.IGameRepository
import com.example.whistrentzscorer.utils.GameMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    val currentRoundBidsPlaced: Boolean
        get() = allBidsPlaced(currentRound)

    var roundOrdering: String = "11..88..11"
        private set

    var gameId: Int = 0
        private set

    var gameMode: GameMode by mutableStateOf(GameMode.WHIST)
        private set

    var bonusPoints: Int = 0
        private set

    var elapsedSeconds: Long by mutableLongStateOf(0L)

    private var timerJob: Job? = null
    private var timerRunning = false

    fun startTimer() {
        if (timerRunning || gameId == 0 || isGameFinished) return
        timerRunning = true
        timerJob = viewModelScope.launch {
            while (timerRunning && !isGameFinished) {
                delay(1000L)
                if (timerRunning) {
                    elapsedSeconds++
                }
            }
        }
    }

    fun pauseTimer() {
        timerRunning = false
        timerJob?.cancel()
        timerJob = null
        autoSave()
    }

    val isGameFinished: Boolean
        get() = totalRounds > 0 && currentRound > totalRounds

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

    fun isRentzGame(): Boolean = gameMode == GameMode.RENTZ

    private fun updateCurrentRoundCards() {
        currentRoundCards = cardsThisRound(currentRound, roundOrdering, playerList.size)
    }

    fun advanceRound() {
        currentRound += 1
        updateCurrentRoundCards()
        autoSave()
    }

    fun init(players: List<String>,
             roundOrdering: String = "11..88..11",
             gameMode: GameMode = GameMode.WHIST,
             bonus: Int = 0
    ) {
        playerList = players
        this.roundOrdering = roundOrdering
        this.gameMode = gameMode
        this.bonusPoints = bonus

        if (isRentzGame()) {
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


    fun getFinalRankings(): List<Pair<String, Int>> {
        return playerList.map { player ->
            val score = game.state[totalRounds]?.get(player)?.score ?: 0
            player to score
        }.sortedByDescending { it.second }
    }

    fun getCurrentPlayer(): Int {
       return (currentRound - 1) % playerList.size
    }

    fun getRoundStateForPlayer(round: Int, playerIndex: Int): RoundState {
        val player = playerList[playerIndex]

        return game.state[round]?.get(player)?: RoundState()
    }

    fun allBidsPlaced(round: Int): Boolean {
        val roundState = game.state[round] ?: return false
        return roundState.values.all { it.bid != null }
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
        val cardsInRound = cardsThisRound(round, roundOrdering, playerList.size)

        playerList.forEach { player ->
            val currentPlayerState = game.state[round]?.get(player)
            val previousPlayerState = game.state[round - 1]?.get(player)

            val bid = currentPlayerState?.bid ?: 0
            val handsTaken = currentPlayerState?.handsTaken ?: 0
            val scoreLastRound = previousPlayerState?.score ?: 0
            val previousConsecutiveCorrect = previousPlayerState?.consecutiveCorrectBids ?: 0
            val previousConsecutiveFailed = previousPlayerState?.consecutiveFailedBids ?: 0

            val bidCorrect = bid == handsTaken
            var bonusAdjustmentPoints = 0
            var bonusAdjustment = ScoreAdjustment.NONE


            if (bonusPoints > 0 && cardsInRound > 1) {
                var newConsecutiveCorrect: Int
                var newConsecutiveFailed: Int

                if (bidCorrect) {
                    newConsecutiveCorrect = previousConsecutiveCorrect + 1
                    newConsecutiveFailed = 0

                    if (newConsecutiveCorrect == 5) {
                        bonusAdjustmentPoints = bonusPoints
                        bonusAdjustment =
                            ScoreAdjustment.BONUS_AWARDED
                        newConsecutiveCorrect = 0
                    }
                } else {
                    newConsecutiveCorrect = 0
                    newConsecutiveFailed = previousConsecutiveFailed + 1

                    if (newConsecutiveFailed == 5) {
                        bonusAdjustmentPoints = -bonusPoints
                        bonusAdjustment = ScoreAdjustment.BONUS_DEDUCTED
                        newConsecutiveFailed = 0
                    }
                }

                currentPlayerState?.consecutiveCorrectBids = newConsecutiveCorrect
                currentPlayerState?.consecutiveFailedBids = newConsecutiveFailed
                currentPlayerState?.bonusAdjustment = bonusAdjustment
            }

            currentPlayerState?.score = scoreLastRound + whistScoring(bid, handsTaken) + bonusAdjustmentPoints
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
                gameType = roundOrdering,
                bonusPoints = bonusPoints,
                state = game.state.mapKeys { (round, _) -> round.toString() }
                    .mapValues { (_, players) ->
                        players.mapValues { (_, roundState) ->
                            roundState.toVO()
                        }
                    }
            )
            val json = gson.toJson(saveData)
            gameRepository.updateScore(gameId, json)
            gameRepository.updateElapsedTime(gameId, elapsedSeconds)
        }
    }

    fun restoreGame(
        id: Int,
        players: List<String>,
        scoresJson: String,
        gameMode: GameMode,
        elapsedTime: Long = 0L
    ) {
        gameId = id
        this.gameMode = gameMode
        
        if (scoresJson.isBlank()) {
            init(players, gameMode = gameMode)
            return
        }

        try {
            val type = object : TypeToken<SaveData>() {}.type
            val saveData: SaveData = gson.fromJson(scoresJson, type)

            playerList = players
            roundOrdering = saveData.gameType
            bonusPoints = saveData.bonusPoints
            currentRound = saveData.currentRound
            elapsedSeconds = elapsedTime
            totalRounds = players.size * 3 + 12

            game = GameState()
            val stateByInt = saveData.state.mapKeys { (k, _) -> k.toInt() }
            for (round in 1..totalRounds) {
                val savedRound = stateByInt[round]
                if (savedRound != null) {
                    game.state[round] = savedRound.mapValues { (_, srs) ->
                        RoundState(
                            bid = srs.bid,
                            handsTaken = srs.handsTaken,
                            score = srs.score,
                            consecutiveCorrectBids = srs.consecutiveCorrectBids,
                            consecutiveFailedBids = srs.consecutiveFailedBids,
                            bonusAdjustment = srs.bonusAdjustment
                        )
                    }.toMutableMap()
                } else {
                    game.state[round] = players.associateWith { RoundState() }.toMutableMap()
                }
            }
            updateCurrentRoundCards()
        } catch (e: Exception) {
            init(players, gameMode = gameMode)
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

fun whistScoring(bid: Int, handsTaken: Int): Int {
    if (bid == handsTaken) {
        return 5 + handsTaken
    }
    return -abs(bid - handsTaken)
}

data class RoundStateVO(
    val bid: Int? = null,
    val handsTaken: Int? = null,
    val score: Int? = null,
    val consecutiveCorrectBids: Int = 0,
    val consecutiveFailedBids: Int = 0,
    val bonusAdjustment: ScoreAdjustment = ScoreAdjustment.NONE
)

data class SaveData(
    val currentRound: Int,
    val gameType: String,
    val bonusPoints: Int = 0,
    val state: Map<String, Map<String, RoundStateVO>>
)