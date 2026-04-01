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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.whistrentzscorer.ui.theme.LightLavender
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
                val verticalScrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    // Fixed left column: header + scrollable card counts + total label
                    Column {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .background(LightLavender)
                                .border(1.dp, Color.Gray)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "", fontWeight = FontWeight.Bold)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(LightLavender)
                                .verticalScroll(verticalScrollState)
                        ) {
                            for (round in 1..stateVM.totalRounds) {
                                val isCurrentRound = round == stateVM.currentRound
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .background(if (isCurrentRound) LightLavender else Color.Transparent)
                                        .border(1.dp, Color.Gray)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stateVM.cardsThisRound(
                                            round = round,
                                            stateVM.gameType,
                                            playerCount = stateVM.playerList.size
                                        ).toString()
                                    )
                                }
                            }
                        }
                        // Total label
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .border(1.dp, Color.Gray)
                                .background(LightLavender)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "\u03A3", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Right section: header + scrollable scores + total row
                    Column(
                        modifier = Modifier.horizontalScroll(horizontalScrollState)
                    ) {
                        PlayersHeader(stateVM.playerList, stateVM.currentRound)

                        ScoringCells(
                            totalRounds = stateVM.totalRounds,
                            playerList = stateVM.playerList,
                            gameState = stateVM.game.state,
                            verticalScrollState = verticalScrollState,
                            currentRound = stateVM.currentRound,
                            modifier = Modifier.weight(1f)
                        )

                        // Total score row
                        TotalScoreRow(
                            playerList = stateVM.playerList,
                            gameState = stateVM.game.state,
                            currentRound = stateVM.currentRound
                        )
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
    verticalScrollState: ScrollState,
    currentRound: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(verticalScrollState)
    ) {
        for (round in 1..totalRounds) {
            val isCurrentRound = round == currentRound
            Row(
                modifier = Modifier.background(if (isCurrentRound) LightLavender else Color.Transparent)
            ) {
                playerList.forEach { player ->
                    val playerState = gameState[round]?.get(player)
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
fun PlayersHeader(playerList: List<String>, round: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
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
                    .background(LightLavender)
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
fun TotalScoreRow(
    playerList: List<String>,
    gameState: MutableMap<Int, MutableMap<String, RoundState>>,
    currentRound: Int
) {
    Row {
        playerList.forEach { player ->
            val lastCompletedRound = (currentRound - 1).coerceAtLeast(0)
            val totalScore = gameState[lastCompletedRound]?.get(player)?.score

            Box(
                modifier = Modifier
                    .width(160.dp)
                    .border(1.dp, Color.Gray)
                    .background(LightLavender)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = totalScore?.toString() ?: "0",
                    fontWeight = FontWeight.Bold
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