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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.ui.theme.DarkPurple
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
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                stateVM.autoSave()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val horizontalScrollState = rememberScrollState()

    var showUndoConfirmation by remember { mutableStateOf(false) }
    var showPodium by remember { mutableStateOf(false) }

    LaunchedEffect(stateVM.currentRound) {
        if (stateVM.isGameFinished) {
            showPodium = true
        }
    }

    if (showPodium && stateVM.isGameFinished) {
        PodiumDialog(
            rankings = stateVM.getFinalRankings(),
            onDismiss = { showPodium = false }
        )
    }

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
                inputResultsEnabled = stateVM.currentRoundBidsPlaced,
                onSelectMiniGame = onSelectMiniGame,
                undoLastTurn = {
                    showUndoConfirmation = true
                }
            )
        }
    ) { padding ->
        if (stateVM.playerList.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val verticalScrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .border(0.3.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .background(LightLavender)
                                .border(0.3.dp, Color.LightGray)
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
                                        .border(0.3.dp, Color.LightGray)
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
                        // total label
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .border(0.3.dp, Color.LightGray)
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
                            currentRound = stateVM.currentRound,
                            isGameFinished = stateVM.isGameFinished
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
                modifier = Modifier.background(if (isCurrentRound) LightLavender else Color.Transparent),
            ) {
                playerList.forEach { player ->
                    val playerState = gameState[round]?.get(player)
                    val bid = playerState?.bid
                    val handsTaken = playerState?.handsTaken
                    val bidFailed = bid != null && handsTaken != null && bid != handsTaken
                    val bonusAdjustment = playerState?.bonusAdjustment ?: 0

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
                        width = 80.dp,
                        bonusAdjustment = bonusAdjustment
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

            val isCurrentPlayer = i == (round-1) % playerList.size
            if (isCurrentPlayer) {
                playerNameColor = DarkPurple
                playerFont = FontWeight.W900
            }
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .border(0.3.dp, Color.LightGray)
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
                
                if (isCurrentPlayer) {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Current turn",
                            tint = DarkPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TotalScoreRow(
    playerList: List<String>,
    gameState: MutableMap<Int, MutableMap<String, RoundState>>,
    currentRound: Int,
    isGameFinished: Boolean = false
) {
    val lastCompletedRound = (currentRound - 1).coerceAtLeast(0)

    val rankMap = if (isGameFinished) {
        val scores = playerList.map { player ->
            player to (gameState[lastCompletedRound]?.get(player)?.score ?: 0)
        }.sortedByDescending { it.second }

        val medals = listOf("\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49")
        val result = mutableMapOf<String, String>()
        scores.forEachIndexed { index, (player, _) ->
            result[player] = if (index < 3) medals[index] else "#${index + 1}"
        }
        result
    } else emptyMap()

    Row {
        playerList.forEach { player ->
            val totalScore = gameState[lastCompletedRound]?.get(player)?.score
            val rank = rankMap[player]

            Box(
                modifier = Modifier
                    .width(160.dp)
                    .border(0.3.dp, Color.LightGray)
                    .background(LightLavender)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rank != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = rank,
                            fontSize = 14.sp
                        )
                        Text(
                            text = " ${totalScore?.toString() ?: "0"}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = totalScore?.toString() ?: "0",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreCell(score: Int?, width: Dp, showCross: Boolean = false, bonusAdjustment: Int = 0) {
    Box(
        modifier = Modifier
            .width(width)
            .border(0.3.dp, Color.LightGray)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        val displayScore = score?.toString() ?: ""
        Text(text = displayScore)
        
        // Show bonus indicator positioned to the right
        if (bonusAdjustment != 0) {
            Box(
                modifier = Modifier.matchParentSize()
                    .padding(end = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (bonusAdjustment > 0) "✓" else "✗",
                    color = if (bonusAdjustment > 0) Color(0xFF4CAF50) else Color(0xFFE53935),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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

@Composable
fun PodiumDialog(
    rankings: List<Pair<String, Int>>,
    onDismiss: () -> Unit
) {
    val medals = listOf("\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49")
    val podiumColors = listOf(
        Color(0xFFFFD700),
        Color(0xFFC0C0C0),
        Color(0xFFCD7F32)
    )
    val podiumHeights = listOf(120.dp, 90.dp, 70.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = "\uD83C\uDFC6 Final Results",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val podiumCount = minOf(3, rankings.size)
                val podiumOrder = when (podiumCount) {
                    3 -> listOf(1, 0, 2)
                    2 -> listOf(1, 0)
                    else -> listOf(0)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    podiumOrder.forEach { idx ->
                        val (player, score) = rankings[idx]
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .widthIn(min = 70.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = medals[idx],
                                fontSize = if (idx == 0) 32.sp else 24.sp
                            )
                            Text(
                                text = player,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Text(
                                text = score.toString(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = DarkPurple
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Spacer(modifier = Modifier.height(podiumHeights[0] - podiumHeights[idx]))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(podiumHeights[idx])
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(podiumColors[idx])
                            )
                        }
                    }
                }

                if (rankings.size > 3) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Runners-up",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    rankings.drop(3).forEachIndexed { index, (player, score) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 4}. $player",
                                fontSize = 14.sp
                            )
                            Text(
                                text = score.toString(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = DarkPurple
                            )
                        }
                    }
                }
            }
        }
    )
}