package com.example.whistrentzscorer.components.rentz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whistrentzscorer.objects.RentzMiniGame
import com.example.whistrentzscorer.ui.WhistTopBar

@Composable
fun MiniGameSelectionScreen(
    onMiniGameSelected: (RentzMiniGame) -> Unit,
    onBack: () -> Unit,
    playedGames: Set<RentzMiniGame> = emptySet()
) {
    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "Select Mini Game") },
                onBack = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(RentzMiniGame.entries.toList()) { miniGame ->
                val isPlayed = playedGames.contains(miniGame)
                MiniGameCard(
                    miniGame = miniGame,
                    isPlayed = isPlayed,
                    onClick = {
                        if (!isPlayed) {
                            onMiniGameSelected(miniGame)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MiniGameCard(
    miniGame: RentzMiniGame,
    isPlayed: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (isPlayed) 0.4f else 1f

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isPlayed) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isPlayed)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isPlayed) 0.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = miniGame.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = miniGame.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${if (miniGame.totalPoints > 0) "+" else ""}${miniGame.totalPoints}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (miniGame.totalPoints > 0)
                    MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = alpha)
            )

            if (isPlayed) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
    }
}
