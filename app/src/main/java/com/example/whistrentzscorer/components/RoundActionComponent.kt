package com.example.whistrentzscorer.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.ui.theme.Orange40
import com.example.whistrentzscorer.viewmodels.GameStateViewModel
import com.example.whistrentzscorer.viewmodels.RoundActions
import com.example.whistrentzscorer.viewmodels.RoundState

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

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "") },
                onBack = onBack
            )
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
                        selectedValue =
                            gameStateViewModel.getRoundStateForPlayer(
                                round = round,
                                playerIndex = currentPlayer
                            ).bid
                                ?: 0
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
                        selectedValue =
                            gameStateViewModel.getRoundStateForPlayer(
                                round = round,
                                playerIndex = currentPlayer
                            ).bid
                                ?: 0
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
            if (action == RoundActions.RESULTS.name) {
                handsTakenSoFar = handsTakenSoFar(
                    cardsThisRound = cardsThisRound,
                    roundState = gameStateViewModel.game.state[round]!!,
                )
                if (isLastPlayer) {
                    selectedValue = handsTakenSoFar
                }
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
                            gameStateViewModel.saveRoundScore(round)
                            gameStateViewModel.currentRound += 1
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
    isLastPlayer: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        for (value in 0..8) {
            val targetColor =
                when {
                    value == illegalChoice -> Color.LightGray.copy(alpha = 0.5f)
                    value == selectedValue -> Orange40
                    else -> ButtonDefaults.buttonColors().containerColor
                }

            val animatedColor by animateColorAsState(
                targetValue = targetColor,
                animationSpec = tween(durationMillis = 400),
                label = "ButtonColorAnim"
            )

            val selectedColor = if (shouldAnimate) animatedColor else targetColor

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
                    autoSelected = selectedValue,
                    isLastPlayer = isLastPlayer
                ),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(selectedColor),
            ) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
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
    cardsThisRound: Int,
    roundState: MutableMap<String, RoundState>
): Int {
    val handsTaken = roundState.values.sumOf { state -> state.handsTaken ?: 0 }
    return cardsThisRound - handsTaken
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
    val remaining = cardsThisRound - handsTakenSoFar
    if (action == RoundActions.RESULTS.name) {
        if (isLastPlayer)
            return autoSelected == value
        return value <= remaining
    }

    return value <= cardsThisRound && value != illegalChoice
}