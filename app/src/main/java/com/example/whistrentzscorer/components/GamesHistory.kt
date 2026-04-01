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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.whistrentzscorer.storage.entity.GameEntity
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.ui.theme.SlateBlue
import com.example.whistrentzscorer.ui.theme.Teal80
import com.example.whistrentzscorer.utils.toFormattedDate
import com.example.whistrentzscorer.viewmodels.HomeViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun GamesHistory(
    onBack: () -> Unit,
    onGameClick: (GameEntity) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val games by viewModel.allGames.collectAsState()

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "Games History") },
                onBack = onBack
            )
        }
    ) { padding ->
        if (games.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No games yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(games, key = { it.id }) { game ->
                    SwipeToDeleteContainer(
                        onDelete = { viewModel.deleteGame(game.id) }
                    ) {
                        GameHistoryCard(
                            game = game,
                            onClick = { onGameClick(game) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showConfirmDialog = true
                false
            } else {
                false
            }
        }
    )

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Delete Game") },
            text = { Text("Are you sure you want to delete this game?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onDelete()
                }) {
                    Text("Delete", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF5350)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        },
        content = { content() }
    )
}

@Composable
fun GameHistoryCard(
    game: GameEntity,
    onClick: () -> Unit
) {
    val players = game.players.split(",").map { it.trim() }.filter { it.isNotBlank() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.timestamp.toFormattedDate(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (!game.isFinished) {
                    Text(
                        text = "IN PROGRESS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(SlateBlue, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Players: ${players.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (game.isFinished && game.scoresJson.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                FinalScores(game.scoresJson, players)
            }
        }
    }
}

@Composable
fun FinalScores(scoresJson: String, players: List<String>) {
    val scoreData = try {
        val gson = Gson()
        val type = object : TypeToken<Map<String, Map<String, Map<String, Any>>>>() {}.type
        val saveData: Map<String, Map<String, Map<String, Any>>> = gson.fromJson(scoresJson, type)
        val state = saveData["state"] ?: return
        
        // Find the last round with scores
        val lastRound = state.keys
            .mapNotNull { it.toIntOrNull() }
            .maxOrNull() ?: return
        
        state[lastRound.toString()]
    } catch (e: Exception) {
        null
    }
    
    scoreData?.let { lastRoundData ->
        Column {
            Text(
                text = "Final Scores:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            players.forEach { player ->
                val playerData = lastRoundData[player] as? Map<*, *>
                val score = (playerData?.get("score") as? Double)?.toInt()
                if (score != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = player,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}