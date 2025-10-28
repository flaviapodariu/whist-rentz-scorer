package com.example.whistrentzscorer.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.ui.theme.Purple80
import com.example.whistrentzscorer.viewmodels.GameConfigViewModel
import com.example.whistrentzscorer.viewmodels.GameStateViewModel

@Composable
fun ScoreSheet(
    // if round number = 0 allow going back to config setup page, else clear nav stack
    onBack: () -> Unit,
    gameConfigViewModel: GameConfigViewModel = hiltViewModel(),
    gameStateViewModel: GameStateViewModel = hiltViewModel(),
    onBid: () -> Unit,
    onInputResults: () -> Unit
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

    val context = LocalContext.current
    val activity = context.findActivity()
    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val horizontalScrollState = rememberScrollState()

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "") },
                onBack = onBack,
                isInGame = true,
                onBid = onBid,
                onInputResults = onInputResults
            )
        }
    ) { padding ->
        if (playerList.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {

                        PlayersHeader(playerList, gameStateViewModel.currentRound, horizontalScrollState)

                        Box(
                            modifier = Modifier
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            ScoringCells(
                                gameStateViewModel.totalRounds,
                                playerList,
                                gameType,
                                gameStateViewModel.bets,
                                gameStateViewModel.handsTaken,
                                gameStateViewModel.scorePerRound
                            )
                        }

                    }
                }
            }
        }
    }


}

@Composable
fun ScoringCells(
    totalRounds: Int,
    playerList: List<String>,
    gameType: String,
    bets: Map<Int, Map<String, Int>>,
    handsTaken: Map<Int, Map<String, Int>>,
    scorePerRound: Map<Int, Map<String, Int>>
) {
    LazyColumn(
        modifier = Modifier
    ) {
        items(totalRounds) { round ->

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val roundHandSize = handsThisRound(
                    round = round + 1,
                    playerCount = playerList.size,
                    gameType = gameType,
                    totalRounds = totalRounds
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .border(1.dp, Color.Gray)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = roundHandSize.toString())
                }
                playerList.forEach { player ->
                    HandInputCell(
                        scoreMap = bets[round],
                        player = player
                    )
                    HandInputCell(
                        scoreMap = handsTaken[round],
                        player = player
                    )
                    ScoreCell(
                        scoreMap = scorePerRound[round],
                        player = player
                    )
                }
            }
        }
    }
}

@Composable
fun PlayersHeader(playerList: List<String>, round: Int, scroll: ScrollState) {
    Row(
        modifier = Modifier
            .horizontalScroll(scroll)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .border(1.dp, Color.Gray)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // placeholder
            Text(text = "", fontWeight = FontWeight.Bold)
        }

        playerList.forEachIndexed { i, player ->

            var playerNameColor = Color(0XFF2A0134)
            var playerFont = FontWeight.SemiBold

            if (i == (round-1) % 4) {
                playerNameColor = Color(0xFF9D00FF)
                playerFont = FontWeight.ExtraBold
            }
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .border(1.dp, Color.Gray)
                    .background(Purple80)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // todo highlight player whose turn it is
                Text(
                    text = player,
                    fontWeight = playerFont,
                    color = playerNameColor
                )
            }
        }
    }
}

@Composable
fun HandInputCell(scoreMap: Map<String, Int>?, player: String) {
    Box(
        modifier = Modifier
            .width(40.dp)
            .border(1.dp, Color.Gray)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val displayScore = if (scoreMap?.get(player) == null)
            "" else scoreMap[player].toString()
        Text(text = displayScore)
    }
}

@Composable
fun ScoreCell(scoreMap: Map<String, Int>?, player: String) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .border(1.dp, Color.Gray)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val displayScore = if (scoreMap?.get(player) == null || scoreMap[player] == 0)
            "" else scoreMap[player].toString()
        Text(text = displayScore)
    }
}


// 1 1 1 1 2 3 4 5 6 7 8 8 8 8 7 6 5 4 3 2 1
//  1 1 1 1 2 3 4 5 6 7 8 8
private fun handsThisRound(
    round: Int,
    playerCount: Int,
    gameType: String,
    totalRounds: Int
): Int {
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


fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}