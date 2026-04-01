package com.example.whistrentzscorer.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.ui.theme.DeepPurple
import com.example.whistrentzscorer.ui.theme.Teal80
import com.example.whistrentzscorer.viewmodels.GameStateViewModel
import com.example.whistrentzscorer.viewmodels.RoundState

@Composable
fun ScoreSheet(
    onBack: () -> Unit,
    stateVM: GameStateViewModel,
    onBid: () -> Unit,
    onInputResults: () -> Unit,
    isRentz: Boolean = false,
    onSelectMiniGame: () -> Unit = {}
) {

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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                stateVM.autoSave()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val horizontalScrollState = rememberScrollState()

    var showUndoConfirmation by remember { mutableStateOf(false) }

    if (showUndoConfirmation) {
        AlertDialog(
            onDismissRequest = { showUndoConfirmation = false },
            title = { Text("Undo last round?") },
            text = { Text("This will remove the bids and results for the last completed round.") },
            confirmButton = {
                TextButton(onClick = {
                    stateVM.undoLastTurn()
                    showUndoConfirmation = false
                }) {
                    Text("Undo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUndoConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "") },
                onBack = onBack,
                isInGame = true,
                isRentz = isRentz,
                onBid = onBid,
                onInputResults = onInputResults,
                onSelectMiniGame = onSelectMiniGame,
                undoLastTurn = {
                    showUndoConfirmation = true
                }
            )
        }
    ) { padding ->
        if (stateVM.playerList.isNotEmpty()) {
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

                        PlayersHeader(stateVM.playerList, stateVM.currentRound, horizontalScrollState)

                        Box(
                            modifier = Modifier
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            ScoringCells(
                                totalRounds = stateVM.totalRounds,
                                playerList = stateVM.playerList,
                                gameState = stateVM.game.state,
                                roundHandSize = { round ->
                                    stateVM.cardsThisRound(
                                        round = round,
                                        stateVM.gameType,
                                        playerCount = stateVM.playerList.size
                                    )
                                }
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
    gameState: MutableMap<Int, MutableMap<String, RoundState>>,
    roundHandSize: (Int) -> Int
) {
    LazyColumn(
        modifier = Modifier
    ) {
        // 0 based indexing
        items(totalRounds, key = { round ->
            val roundData = gameState[round + 1]
            "${round}_${System.identityHashCode(roundData)}"
        }) { round ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .border(1.dp, Color.Gray)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = roundHandSize(round+1).toString() )
                }
                playerList.forEach { player ->
                    val playerState = gameState[round+1]?.get(player)
                    val bid = playerState?.bid
                    val handsTaken = playerState?.handsTaken
                    val bidFailed = bid != null && handsTaken != null && bid != handsTaken

                    ScoreCell(
                        score = bid,
                        width = 40.dp,
                        showCross = bidFailed
                    )
                    ScoreCell(
                        score = handsTaken,
                        width = 40.dp
                    )
                    ScoreCell(
                        score = playerState?.score,
                        width = 80.dp
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

            if (i == (round-1) % playerList.size) {
                playerNameColor = DeepPurple
                playerFont = FontWeight.W900
            }
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .border(1.dp, Color.Gray)
                    .background(Teal80)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player,
                    fontWeight = playerFont,
                    color = playerNameColor,
                    // todo find good size
//                    fontSize = 22.sp
                )
            }
        }
    }
}

@Composable
fun ScoreCell(score: Int?, width: Dp, showCross: Boolean = false) {
    Box(
        modifier = Modifier
            .width(width)
            .border(1.dp, Color.Gray)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val displayScore = score?.toString() ?: ""
        Text(text = displayScore)

        if (showCross) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val stroke = 2.dp.toPx()
                drawLine(
                    color = Color.Red,
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = stroke
                )
            }
        }
    }
}


fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}