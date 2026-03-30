package com.example.whistrentzscorer.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.ui.theme.Coral40
import com.example.whistrentzscorer.ui.theme.Orange40
import com.example.whistrentzscorer.viewmodels.GameStateViewModel
import com.example.whistrentzscorer.viewmodels.RoundActions
import com.example.whistrentzscorer.viewmodels.RoundState
import kotlinx.coroutines.launch

@Composable
fun RoundActionScreen(
    cardsThisRound: Int,
    action: String,
    onBack: () -> Unit,
    gameStateViewModel: GameStateViewModel
) {

    val round = gameStateViewModel.currentRound
    val playerCount = gameStateViewModel.playerList.size

    var firstPlayer by remember {
        mutableIntStateOf(
            gameStateViewModel.getCurrentPlayer()
        )
    }

    var currentPlayer by remember {
        mutableIntStateOf(
            gameStateViewModel.getCurrentPlayer()
        )
    }

    var selectedValue by remember {
        mutableIntStateOf(
            gameStateViewModel
                .getRoundStateForPlayer(round, currentPlayer).bid ?: 0
        )
    }

    var shouldAnimate by remember { mutableStateOf(false) }

    val isLastPlayer = isLastPlayer(currentPlayer, playerCount, firstPlayer)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                gameStateViewModel.autoSave()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "") },
                onBack = onBack
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Coral40,
                    contentColor = Color.White,
                    actionColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                PlayerControllerButton(
                    enabled = currentPlayer != firstPlayer,
                    onClick = {
                        if (action == RoundActions.BID.name) {
                            gameStateViewModel.setBid(round, currentPlayer, selectedValue)
                        } else {
                            gameStateViewModel.setHandsTaken(round, currentPlayer, selectedValue)
                        }
                        currentPlayer = (currentPlayer - 1 + playerCount) % playerCount

                        val newPlayerState = gameStateViewModel.getRoundStateForPlayer(
                            round = round,
                            playerIndex = currentPlayer
                        )
                        selectedValue = if (action == RoundActions.BID.name) {
                            newPlayerState.bid ?: 0
                        } else {
                            newPlayerState.handsTaken ?: newPlayerState.bid ?: 0
                        }
                        shouldAnimate = false
                    },
                    icon = Icons.Filled.ArrowBackIosNew,
                    contentDescription = "prev player"
                )

                Text(
                    text = gameStateViewModel.playerList[currentPlayer],
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                // 0 1 2 3
                PlayerControllerButton(
                    enabled = !isLastPlayer(currentPlayer, playerCount, firstPlayer),
                    onClick = {
                        if (action == RoundActions.BID.name) {
                            gameStateViewModel.setBid(round, currentPlayer, selectedValue)
                        } else {
                            gameStateViewModel.setHandsTaken(round, currentPlayer, selectedValue)
                        }
                        currentPlayer = (currentPlayer + 1) % playerCount

                        val newPlayerState = gameStateViewModel.getRoundStateForPlayer(
                            round = round,
                            playerIndex = currentPlayer
                        )
                        selectedValue = if (action == RoundActions.BID.name) {
                            newPlayerState.bid ?: 0
                        } else {
                            newPlayerState.handsTaken ?: newPlayerState.bid ?: 0
                        }
                        shouldAnimate = false
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "next player"
                )

            }

            var illegalChoice: Int? = null

            if (action == RoundActions.BID.name) {
                illegalChoice = getIllegalChoice(
                    action = action,
                    cardsThisRound = cardsThisRound,
                    roundState = gameStateViewModel.game.state[round]!!,
                    isLastPlayer = isLastPlayer
                )
                val nextLegalChoice = if (illegalChoice == 0) 1 else 0
                selectedValue = if (!isLastPlayer || illegalChoice == null)
                    selectedValue else nextLegalChoice
            }

            var handsTakenSoFar = 0
            var autoSelectedValue = 0
            if (action == RoundActions.RESULTS.name) {
                handsTakenSoFar = handsTakenSoFar(
                    roundState = gameStateViewModel.game.state[round]!!,
                    excludePlayer = gameStateViewModel.playerList[currentPlayer]
                )
                autoSelectedValue = cardsThisRound - handsTakenSoFar
            }




            ValueChooser(
                action = action,
                selectedValue = selectedValue,
                onSelected = {
                    selectedValue = it
                    shouldAnimate = true
                },
                cardsThisRound = cardsThisRound,
                handsTakenSoFar = handsTakenSoFar,
                shouldAnimate = shouldAnimate,
                illegalChoice = illegalChoice,
                isLastPlayer = isLastPlayer,
                autoSelectedValue = autoSelectedValue
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isLastPlayer) {
                Button(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(3f)
                        .padding(bottom = 32.dp),
                    onClick = {
                        // save for last player
                        if (action == RoundActions.BID.name) {
                            gameStateViewModel.setBid(round, currentPlayer, selectedValue)
                        } else {
                            gameStateViewModel.setHandsTaken(round, currentPlayer, selectedValue)
                        }

                        if (action == RoundActions.RESULTS.name) {
                            val totalHands = gameStateViewModel.game.state[round]!!
                                .values.sumOf { it.handsTaken ?: 0 }
                            if (totalHands != cardsThisRound) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Total hands taken ($totalHands) must equal cards dealt ($cardsThisRound)"
                                    )
                                }
                                return@Button
                            }
                            gameStateViewModel.saveRoundScore(round)
                            gameStateViewModel.advanceRound()
                        }
                        onBack()
                    }
                ) {
                    Text(
                        text = if (action == RoundActions.BID.name) "Play" else "Input results",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }


    }
}

@Composable
fun PlayerControllerButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    Box(
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                icon,
                contentDescription
            )
        }
    }
}


@Composable
fun ValueChooser(
    action: String,
    selectedValue: Int,
    onSelected: (Int) -> Unit,
    cardsThisRound: Int,
    handsTakenSoFar: Int,
    shouldAnimate: Boolean,
    illegalChoice: Int? = null,
    isLastPlayer: Boolean,
    autoSelectedValue: Int = 0,
) {

    val isBid = action == RoundActions.BID.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        for (value in 0..8) {
            val isSelected = value == selectedValue

            val targetColor =
                when {
                    value == illegalChoice -> Color.LightGray.copy(alpha = 0.5f)
                    isSelected -> Orange40
                    else -> ButtonDefaults.buttonColors().containerColor
                }

            val animatedColor by animateColorAsState(
                targetValue = targetColor,
                animationSpec = tween(durationMillis = 400),
                label = "ButtonColorAnim"
            )

            val selectedColor = if (shouldAnimate) animatedColor else targetColor

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "ButtonScaleAnim"
            )

            val elevation by animateFloatAsState(
                targetValue = if (isSelected) 8f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "ButtonElevationAnim"
            )

            Button(
                onClick = {
                    onSelected(value)
                },
                enabled = enabledCondition(
                    value = value,
                    cardsThisRound = cardsThisRound,
                    handsTakenSoFar = handsTakenSoFar,
                    illegalChoice = illegalChoice,
                    action = action,
                    autoSelected = if (isBid) selectedValue else autoSelectedValue,
                    isLastPlayer = isLastPlayer
                ),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(selectedColor),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .shadow(
                        elevation = elevation.dp,
                        shape = RoundedCornerShape(8.dp),
                        ambientColor = Orange40,
                        spotColor = Orange40
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(min = 32.dp)
                        .size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        style = if (isSelected)
                            MaterialTheme.typography.headlineMedium
                        else
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
                    )

                    if (value == illegalChoice) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val stroke = 4.dp.toPx()
                            drawLine(
                                color = Color.Red,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height),
                                strokeWidth = stroke
                            )
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
        }
    }
}

fun isLastPlayer(currentPlayer: Int, playerCount: Int, firstPlayer: Int): Boolean {
    return (currentPlayer + 1) % playerCount == firstPlayer
}

fun getIllegalChoice(
    action: String,
    cardsThisRound: Int,
    roundState: MutableMap<String, RoundState>,
    isLastPlayer: Boolean
): Int? {
    if (action == RoundActions.RESULTS.name) {
        return null
    }

    val bidded = roundState.values.sumOf { state -> state.bid ?: 0 }
    val difference = cardsThisRound - bidded
    return if (difference < 0 || !isLastPlayer) null else difference
}

fun handsTakenSoFar(
    roundState: MutableMap<String, RoundState>,
    excludePlayer: String? = null
): Int {
    return roundState.entries
        .filter { it.key != excludePlayer }
        .sumOf { it.value.handsTaken ?: 0 }
}

fun enabledCondition(
    isLastPlayer: Boolean,
    value: Int,
    cardsThisRound: Int,
    handsTakenSoFar: Int,
    illegalChoice: Int?,
    action: String,
    autoSelected: Int
): Boolean {
    if (action == RoundActions.RESULTS.name) {
        return value <= cardsThisRound
    }

    return value <= cardsThisRound && value != illegalChoice
}