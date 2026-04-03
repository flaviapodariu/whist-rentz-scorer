package com.example.whistrentzscorer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.viewmodels.GameConfigViewModel
import kotlinx.coroutines.launch

val GameType = listOf("11..88..11", "88..11..88")
val Bonus = listOf(0, 5, 10)

@Composable
fun PlayersSetupScreen(
    onPlayersAdded: (List<String>) -> Unit,
    onBack: () -> Unit,
    viewModel: GameConfigViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val index = viewModel.currentPlayerIndex
    var currentName by remember(index) {
        mutableStateOf(viewModel.getPlayerName(index))
    }

    val canGoBack = index > 0
    val canProceed = currentName.isNotBlank()
    val playerCount = viewModel.players.size
    val isDuplicate = viewModel.getPlayerList().filterIndexed { i, _ -> i != index }.contains(currentName.trim())

    val saveCurrentAndGoNext = {
        if (currentName.isNotBlank() && !isDuplicate) {
            viewModel.setPlayerName(index, currentName.trim())
            viewModel.goToNextPlayer()
        }
    }

    val saveCurrentAndGoBack = {
        if (currentName.isNotBlank() && !isDuplicate) {
            viewModel.setPlayerName(index, currentName.trim())
        }
        viewModel.goToPrevPlayer()
    }

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "Add Players") },
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { saveCurrentAndGoBack() },
                    enabled = canGoBack,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous player",
                        modifier = Modifier.size(32.dp)
                    )
                }

                BasicTextField(
                    value = currentName,
                    onValueChange = { currentName = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { saveCurrentAndGoNext() }),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (currentName.isEmpty()) {
                                    Text(
                                        text = "Enter name",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(2.dp)
                                    .background(
                                        if (isDuplicate && currentName.isNotBlank())
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                            if (isDuplicate && currentName.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Name already taken",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )

                IconButton(
                    onClick = { saveCurrentAndGoNext() },
                    enabled = canProceed && !isDuplicate,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next player",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$playerCount player${if (playerCount != 1) "s" else ""} added",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (playerCount >= 3) {
                Button(
                    onClick = {
                        if (currentName.isNotBlank() && !isDuplicate) {
                            viewModel.setPlayerName(index, currentName.trim())
                        }
                        coroutineScope.launch {
                            onPlayersAdded(viewModel.getPlayerList())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Continue with $playerCount players",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                Text(
                    text = "Add at least 3 players to continue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.weight(3f))
        }
    }
}

@Composable
fun GameSetupScreen(
    onGameStarted: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: GameConfigViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (viewModel.gameMode == "rentz") {
            val gameId = viewModel.createNewGame()
            onGameStarted(gameId)
        }
    }

    if (viewModel.gameMode == "rentz") return

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "Game Preferences") },
                onBack = onBack
            )
        },
        floatingActionButton = {
            if (viewModel.players.size >= 3) {
                FloatingActionButton(onClick = {
                    coroutineScope.launch {
                        val gameId = viewModel.createNewGame()
                        onGameStarted(gameId)
                    }
                }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start Game")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Select play style",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
                )

                ElevatedCard(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GameType.forEach { gameType ->
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clickable { viewModel.onGameTypeSelected(gameType) }
                                    .padding(horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = gameType,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 16.dp)
                                )

                                RadioButton(
                                    selected = (gameType == viewModel.gameType),
                                    onClick = { viewModel.onGameTypeSelected(gameType) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Column(
                modifier = Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Select bonus points (5 correct bets)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
                )
                ElevatedCard(
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Bonus.forEach { bonusValue ->
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clickable { viewModel.onBonusSelected(bonusValue) }
                                    .padding(horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = bonusValue.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 16.dp)
                                )

                                RadioButton(
                                    selected = (bonusValue == viewModel.bonus),
                                    onClick = { viewModel.onBonusSelected(bonusValue) }
                                )
                            }
                        }

                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Zero points wins the game",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
                )

                Checkbox(
                    checked = viewModel.zeroPointsWins,
                    onCheckedChange = {
                        viewModel.onZeroPointsWinsSelected()
                    }
                )
            }

        }

    }


}