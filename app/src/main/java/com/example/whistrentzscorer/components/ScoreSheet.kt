package com.example.whistrentzscorer.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.viewmodels.GameConfigViewModel
import com.example.whistrentzscorer.viewmodels.GameStateViewModel

@Composable
fun ScoreSheet(
    onBack: () -> Unit,
    gameConfigViewModel: GameConfigViewModel = hiltViewModel(),
    gameStateViewModel: GameStateViewModel = hiltViewModel()
) {

    var playerList by remember { mutableStateOf(emptyList<String>()) }
    val gameType = gameConfigViewModel.gameType

    LaunchedEffect(Unit) {
        val initialPlayers = gameConfigViewModel.players.toList()
        if (initialPlayers.isNotEmpty()) {
            playerList = initialPlayers
            gameStateViewModel.init(playerList)
        }
    }

    // each player -> bet | actual hands taken | total score until now + number of round
    val rows = gameConfigViewModel.getPlayerList().size * 3 + 1

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "Add Players") },
                onBack = onBack
            )
        }
    ) { padding ->
        if (playerList.isNotEmpty()) {
            LazyHorizontalGrid(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                rows = GridCells.Fixed(rows),
                contentPadding = PaddingValues(2.dp)
            ) {
                val totalRounds = gameStateViewModel.totalRounds
                for (round in 1 until totalRounds + 1) {
                    val handsThisRound =
                        handsThisRound(round, playerList.size, gameType, totalRounds)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, Color.Black)
                                .padding(1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = handsThisRound.toString())
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun TableHeaderCell(
    text: String,
    modifier: Modifier,
    isPrimary: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = if (isPrimary) Color.Red else Color.DarkGray
        )
    }
}

//15 - 8

// 1 1 1 1 2 3 4 5 6 7 8 8 8 8 7 6 5 4 3 2 1
//  1 1 1 1 2 3 4 5 6 7 8 8
private fun handsThisRound(round: Int, playerCount: Int, gameType: String, totalRounds: Int): Int {
    val roundTypes = gameType.split("..")
    // first character in 11 / 88 represents hand card number
    val startingRound = Integer.parseInt(roundTypes[0][0].toString())
    val midRound = Integer.parseInt(roundTypes[1][0].toString())

    val middleRoundsEnd = 2 * playerCount + 7
    if (round in 1..playerCount) return startingRound
    if (round in playerCount + 7..middleRoundsEnd) return midRound

    if (round in playerCount..playerCount + 6 && startingRound == 1) {
        return round - playerCount + startingRound
    } else if (startingRound == 8) {
        return startingRound - (round - playerCount)
    }

    if (round in middleRoundsEnd + 1..totalRounds && startingRound == 1) {
        return round - midRound - 1
    } else {
        return round - playerCount - startingRound - 1
    }

}
