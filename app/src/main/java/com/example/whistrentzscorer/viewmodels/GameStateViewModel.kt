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

        for (round in 1 until totalRounds+1) {
            players.forEach { p ->
                scorePerRound[round] = mapOf(p to 0)
                bets[round] = mapOf(p to 0)
                handsTaken[round] = mapOf(p to 0)
            }
        }
    }

    // round to map<player, score>
    var scorePerRound = mutableStateMapOf(
        1 to mapOf(
            "" to 0
        )
    )

    //    round to map<player, bet>
    var bets = mutableStateMapOf(
        1 to mapOf(
            "" to 0
        )
    )

    //    round to map<player, hands>

    var handsTaken = mutableStateMapOf(
        1 to mapOf(
            "" to 0
        )
    )

    var totalRounds: Int = 0

}
