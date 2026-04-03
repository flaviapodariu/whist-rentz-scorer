package com.example.whistrentzscorer.components

import android.content.pm.ActivityInfo
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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

    val context = LocalContext.current
    val activity = context.findActivity()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                gameStateViewModel.autoSave()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val round = gameStateViewModel.currentRound
    val playerCount = gameStateViewModel.playerList.size

    val firstPlayer = remember { gameStateViewModel.getCurrentPlayer() }

    var currentPlayer by remember {
        mutableIntStateOf(
            gameStateViewModel.getCurrentPlayer()
        )
    }

    // Local buffer for handsTaken during results input — only committed on confirm
    val localHandsTaken = remember { mutableMapOf<Int, Int>() }

    var selectedValue by remember {
        mutableIntStateOf(
            if (action == RoundActions.RESULTS.name) {
                localHandsTaken[currentPlayer]
                    ?: gameStateViewModel.getRoundStateForPlayer(round, currentPlayer).bid ?: 0
            } else {
                gameStateViewModel.getRoundStateForPlayer(round, currentPlayer).bid ?: 0
            }
        )
    }

    var shouldAnimate by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight

            val playerNameStyle = if (availableWidth < 360.dp)
                MaterialTheme.typography.headlineSmall
            else
                MaterialTheme.typography.headlineLarge

            val playerNamePadding = min(32.dp, availableWidth * 0.06f)

            val saveAndNavigate = { direction: Int ->
                if (action == RoundActions.BID.name) {
                    gameStateViewModel.setBid(round, currentPlayer, selectedValue)
                } else {
                    localHandsTaken[currentPlayer] = selectedValue
                }
                currentPlayer = (currentPlayer + direction + playerCount) % playerCount

                val newPlayerState = gameStateViewModel.getRoundStateForPlayer(
                    round = round,
                    playerIndex = currentPlayer
                )
                selectedValue = if (action == RoundActions.BID.name) {
                    newPlayerState.bid ?: 0
                } else {
                    localHandsTaken[currentPlayer] ?: newPlayerState.bid ?: 0
                }

                if (direction == 1) {
                    val nextIsLast = isLastPlayer(currentPlayer, playerCount, firstPlayer)
                    if (nextIsLast && action == RoundActions.BID.name) {
                        val illegal = getIllegalChoice(
                            action = action,
                            cardsThisRound = cardsThisRound,
                            roundState = gameStateViewModel.game.state[round]!!,
                            lastPlayer = gameStateViewModel.playerList[currentPlayer]
                        )
                        if (illegal != null && selectedValue == illegal) {
                            selectedValue = (0..cardsThisRound).first { it != illegal }
                        }
                    }
                }

                shouldAnimate = false
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerControllerButton(
                        enabled = currentPlayer != firstPlayer,
                        onClick = { saveAndNavigate(-1) },
                        icon = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "prev player"
                    )

                    Text(
                        text = gameStateViewModel.playerList[currentPlayer],
                        style = playerNameStyle,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = playerNamePadding),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    PlayerControllerButton(
                        enabled = !isLastPlayer(currentPlayer, playerCount, firstPlayer),
                        onClick = { saveAndNavigate(1) },
                        icon = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "next player"
                    )
                }

                val isCurrentPlayerLast = isLastPlayer(currentPlayer, playerCount, firstPlayer)
                val illegalChoice: Int? = if (action == RoundActions.BID.name && isCurrentPlayerLast) {
                    getIllegalChoice(
                        action = action,
                        cardsThisRound = cardsThisRound,
                        roundState = gameStateViewModel.game.state[round]!!,
                        lastPlayer = gameStateViewModel.playerList[currentPlayer]
                    )
                } else null

                key(currentPlayer) {
                    ValueChooser(
                        action = action,
                        selectedValue = selectedValue,
                        onSelected = {
                            selectedValue = it
                            shouldAnimate = true
                        },
                        cardsThisRound = cardsThisRound,
                        shouldAnimate = shouldAnimate,
                        illegalChoice = illegalChoice,
                    )
                }

                if (isCurrentPlayerLast) {
                    Spacer(modifier = Modifier.height(availableHeight * 0.05f))
                    Button(
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            if (action == RoundActions.BID.name) {
                                gameStateViewModel.setBid(round, currentPlayer, selectedValue)
                            }

                            if (action == RoundActions.RESULTS.name) {
                                localHandsTaken[currentPlayer] = selectedValue
                                val totalHands = localHandsTaken.values.sum()
                                if (totalHands != cardsThisRound) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Total hands taken ($totalHands) must equal cards dealt ($cardsThisRound)"
                                        )
                                    }
                                    return@Button
                                }
                                // Commit all buffered results to game state
                                localHandsTaken.forEach { (playerIdx, hands) ->
                                    gameStateViewModel.setHandsTaken(round, playerIdx, hands)
                                }
                                gameStateViewModel.saveRoundScore(round)
                                gameStateViewModel.advanceRound()
                            }
                            onBack()
                        }
                    ) {
                        Text(
                            text = if (action == RoundActions.BID.name) "Play" else "Input results",
                            style = if (availableWidth < 360.dp)
                                MaterialTheme.typography.titleLarge
                            else
                                MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(
                                horizontal = min(32.dp, availableWidth * 0.06f),
                                vertical = 8.dp
                            )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
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

@Composable
fun ValueChooser(
    action: String,
    selectedValue: Int,
    onSelected: (Int) -> Unit,
    cardsThisRound: Int,
    shouldAnimate: Boolean,
    illegalChoice: Int? = null,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val totalWidth = maxWidth
        val horizontalPadding = totalWidth * 0.08f
        val gap = totalWidth * 0.016f
        val buttonContentPadding = totalWidth * 0.008f
        val verticalPadding = totalWidth * 0.04f

        val density = LocalDensity.current
        val fontSize = with(density) { (totalWidth * 0.038f).toPx().toSp() }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically
        ) {

            for (value in 0..8) {
                val isSelected = value == selectedValue && value != illegalChoice

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
                        illegalChoice = illegalChoice,
                        action = action,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(selectedColor),
                    contentPadding = PaddingValues(
                        horizontal = buttonContentPadding,
                        vertical = buttonContentPadding
                    ),
                    modifier = Modifier
                        .weight(1f)
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
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.toString(),
                            fontSize = fontSize,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            maxLines = 1
                        )

                        if (value == illegalChoice) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val crossSize = size.maxDimension * 0.9f
                                val halfCross = crossSize / 2f
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val stroke = (crossSize * 0.12f)
                                    .coerceAtLeast(2.dp.toPx())
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(centerX - halfCross, centerY - halfCross),
                                    end = Offset(centerX + halfCross, centerY + halfCross),
                                    strokeWidth = stroke
                                )
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(centerX + halfCross, centerY - halfCross),
                                    end = Offset(centerX - halfCross, centerY + halfCross),
                                    strokeWidth = stroke
                                )
                            }
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
    lastPlayer: String? = null
): Int? {
    if (action == RoundActions.RESULTS.name || lastPlayer == null) {
        return null
    }

    val bidsSoFar = roundState.entries
        .filter { it.key != lastPlayer }
        .sumOf { it.value.bid ?: 0 }
    val difference = cardsThisRound - bidsSoFar
    return if (difference < 0) null else difference
}

fun enabledCondition(
    value: Int,
    cardsThisRound: Int,
    illegalChoice: Int?,
    action: String,
): Boolean {
    if (action == RoundActions.RESULTS.name) {
        return value <= cardsThisRound
    }

    return value <= cardsThisRound && value != illegalChoice
}