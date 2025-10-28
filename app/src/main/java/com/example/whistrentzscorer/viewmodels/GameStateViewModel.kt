package com.example.whistrentzscorer.viewmodels

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameStateViewModel @Inject constructor(
) : ViewModel() {

    fun init(players: List<String>) {
        // 2 rounds with 1 card + 1 round with 8 cards per player + the rest 2-7 cards (up and down)
        totalRounds = players.size * 3 + 14
        scorePerRound.clear()
        bets.clear()
        handsTaken.clear()

        for (round in 1 until totalRounds+1) {
            players.forEach { p ->
                val scoreMap = mutableMapOf<String, Int>()
                val betMap = mutableMapOf<String, Int>()
                val handsMap = mutableMapOf<String, Int>()

                players.forEach { player ->
                    scoreMap[player] = 0
                }

                scorePerRound[round] = scoreMap
                bets[round] = betMap
                handsTaken[round] = handsMap
            }
        }
    }


    var totalRounds: Int = 0
        private set

    // round -> (player -> score)
    var scorePerRound = mutableStateMapOf<Int, MutableMap<String, Int>>()
        private set

    // round -> (player -> bet)
    var bets = mutableStateMapOf<Int, MutableMap<String, Int>>()
        private set

    // round -> (player -> hands taken)
    var handsTaken = mutableStateMapOf<Int, MutableMap<String, Int>>()
        private set

}
