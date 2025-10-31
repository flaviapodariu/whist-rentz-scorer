package com.example.whistrentzscorer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.viewmodels.GameConfigViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.Checkbox

val GameType = listOf("11..88..11", "88..11..88")
val Bonus = listOf(0, 5, 10)

@Composable
fun PlayersSetupScreen(
    onPlayersAdded: (List<String>) -> Unit,
    onBack: () -> Unit,
    viewModel: GameConfigViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "Add Players") },
                onBack = onBack
            )
        },
        floatingActionButton = {
            if (viewModel.players.size >= 3) {
                FloatingActionButton(onClick = {
                    val players = viewModel.players
                    coroutineScope.launch {
                        onPlayersAdded(players)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AddPlayerComponent(
                playerName = viewModel.newPlayerName,
                onNameChange = viewModel::onNewPlayerNameSelected,
                onAddPlayer = viewModel::addPlayer,
                players = viewModel.getPlayerList()
            )

            Spacer(Modifier.height(24.dp))

            PlayerList(
                players = viewModel.getPlayerList(),
                onRemovePlayer = viewModel::removePlayer
            )

            if (viewModel.players.size < 3) {
                Text(
                    text = "Add at least 3 players to start scoring.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun GameSetupScreen(
    onGameStarted: () -> Unit,
    onBack: () -> Unit,
    viewModel: GameConfigViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

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
                        viewModel.createNewGame()
                        onGameStarted()
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