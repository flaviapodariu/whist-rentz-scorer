package com.example.whistrentzscorer.components.rentz

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.whistrentzscorer.objects.RentzMiniGame
import com.example.whistrentzscorer.components.whist.AppTopBar

/**
 * Single player checkbox input — for King of Hearts and 10 of Clubs.
 * Only one player can be selected.
 */
@Composable
fun SinglePlayerCheckboxScreen(
    miniGame: RentzMiniGame,
    players: List<String>,
    onSubmit: (Map<String, Int>) -> Unit,
    onBack: () -> Unit
) {
    var selectedPlayer by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(miniGame.displayName) },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = miniGame.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Who took it?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            players.forEach { player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedPlayer == player,
                        onCheckedChange = { checked ->
                            selectedPlayer = if (checked) player else null
                        }
                    )
                    Text(
                        text = player,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val scores = players.associateWith { player ->
                        if (player == selectedPlayer) miniGame.pointsPerUnit else 0
                    }
                    onSubmit(scores)
                },
                enabled = selectedPlayer != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Submit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Count-based input — for Queens, Diamonds, Tricks.
 * Each player has +/- buttons to set how many items they took.
 */
@Composable
fun CountPerPlayerScreen(
    miniGame: RentzMiniGame,
    players: List<String>,
    onSubmit: (Map<String, Int>) -> Unit,
    onBack: () -> Unit
) {
    val counts = remember { mutableStateMapOf<String, Int>().apply { players.forEach { put(it, 0) } } }
    val totalItems = when (miniGame) {
        RentzMiniGame.QUEENS -> 4
        RentzMiniGame.DIAMONDS -> 13
        RentzMiniGame.TRICKS -> 13
        else -> 13
    }
    val currentTotal = counts.values.sum()

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text(miniGame.displayName) },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = miniGame.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total: $currentTotal / $totalItems",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (currentTotal == totalItems) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(players) { _, player ->
                    val count = counts[player] ?: 0
                    CountInputRow(
                        playerName = player,
                        count = count,
                        maxCount = minOf(miniGame.maxCountPerPlayer, totalItems - currentTotal + count),
                        onCountChange = { newCount -> counts[player] = newCount }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val scores = players.associateWith { player ->
                        (counts[player] ?: 0) * miniGame.pointsPerUnit
                    }
                    onSubmit(scores)
                },
                enabled = currentTotal == totalItems,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Submit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CountInputRow(
    playerName: String,
    count: Int,
    maxCount: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = playerName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = { if (count > 0) onCountChange(count - 1) },
            enabled = count > 0
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
        }

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp)
        )

        IconButton(
            onClick = { if (count < maxCount) onCountChange(count + 1) },
            enabled = count < maxCount
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Increase")
        }
    }
}

/**
 * Rank input — for Rentz (The Race).
 * Each player gets assigned a rank (1st, 2nd, 3rd, 4th...).
 */
@Composable
fun RentzRankScreen(
    players: List<String>,
    onSubmit: (Map<String, Int>) -> Unit,
    onBack: () -> Unit
) {
    val ranks = remember { mutableStateMapOf<String, Int?>().apply { players.forEach { put(it, null) } } }
    val availableRanks = (1..players.size).toList()
    val allAssigned = ranks.values.all { it != null }
    var showDuplicateError by remember { mutableStateOf(false) }

    if (showDuplicateError) {
        AlertDialog(
            onDismissRequest = { showDuplicateError = false },
            title = { Text("Duplicate Ranks") },
            text = { Text("Two or more players have the same rank. Each player must have a unique finishing position.") },
            confirmButton = {
                TextButton(onClick = { showDuplicateError = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text("Rentz") },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Assign finishing positions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "1st: +400, 2nd: +200, 3rd: +100, 4th+: 0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(players) { _, player ->
                    RankRow(
                        playerName = player,
                        currentRank = ranks[player],
                        availableRanks = availableRanks,
                        onRankSelected = { rank -> ranks[player] = rank }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val assignedRanks = ranks.values.filterNotNull()
                    if (assignedRanks.size != assignedRanks.toSet().size) {
                        showDuplicateError = true
                    } else {
                        val scores = players.associateWith { player ->
                            RentzMiniGame.rankPoints(ranks[player] ?: players.size)
                        }
                        onSubmit(scores)
                    }
                },
                enabled = allAssigned,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Submit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun RankRow(
    playerName: String,
    currentRank: Int?,
    availableRanks: List<Int>,
    onRankSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = playerName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (currentRank != null) "${currentRank}${ordinalSuffix(currentRank)}" else "Select",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableRanks.forEach { rank ->
                    DropdownMenuItem(
                        text = {
                            Text(text = "${rank}${ordinalSuffix(rank)} — ${RentzMiniGame.rankPoints(rank)} pts")
                        },
                        onClick = {
                            onRankSelected(rank)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Totale input — combined: King of Hearts + Queens + Diamonds + Tricks.
 * Collects all four sub-results on a single screen.
 */
@Composable
fun TotaleScreen(
    players: List<String>,
    onSubmit: (Map<String, Int>) -> Unit,
    onBack: () -> Unit
) {
    // King of Hearts
    var kingPlayer by remember { mutableStateOf<String?>(null) }
    // Queens count per player
    val queenCounts = remember { mutableStateMapOf<String, Int>().apply { players.forEach { put(it, 0) } } }
    // Diamonds count per player
    val diamondCounts = remember { mutableStateMapOf<String, Int>().apply { players.forEach { put(it, 0) } } }
    // Tricks count per player
    val trickCounts = remember { mutableStateMapOf<String, Int>().apply { players.forEach { put(it, 0) } } }

    val queensTotal = queenCounts.values.sum()
    val diamondsTotal = diamondCounts.values.sum()
    val tricksTotal = trickCounts.values.sum()

    val isValid = kingPlayer != null && queensTotal == 4 && diamondsTotal == 13 && tricksTotal == 13

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text("Totale") },
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // King of Hearts section
            item {
                Text(
                    text = "King of Hearts (K♥ = -200)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            item {
                players.forEach { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = kingPlayer == player,
                            onCheckedChange = { checked ->
                                kingPlayer = if (checked) player else null
                            }
                        )
                        Text(
                            text = player,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Queens section
            item {
                Text(
                    text = "Queens (-40 each) — $queensTotal/4",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            itemsIndexed(players) { _, player ->
                CountInputRow(
                    playerName = player,
                    count = queenCounts[player] ?: 0,
                    maxCount = minOf(4, 4 - queensTotal + (queenCounts[player] ?: 0)),
                    onCountChange = { queenCounts[player] = it }
                )
            }

            // Diamonds section
            item {
                Text(
                    text = "Diamonds (-30 each) — $diamondsTotal/13",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            itemsIndexed(players) { _, player ->
                CountInputRow(
                    playerName = player,
                    count = diamondCounts[player] ?: 0,
                    maxCount = minOf(13, 13 - diamondsTotal + (diamondCounts[player] ?: 0)),
                    onCountChange = { diamondCounts[player] = it }
                )
            }

            // Tricks section
            item {
                Text(
                    text = "Tricks (-50 each) — $tricksTotal/13",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            itemsIndexed(players) { _, player ->
                CountInputRow(
                    playerName = player,
                    count = trickCounts[player] ?: 0,
                    maxCount = minOf(13, 13 - tricksTotal + (trickCounts[player] ?: 0)),
                    onCountChange = { trickCounts[player] = it }
                )
            }

            // Submit button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val scores = players.associateWith { player ->
                            val kingScore = if (player == kingPlayer) -200 else 0
                            val queenScore = (queenCounts[player] ?: 0) * -40
                            val diamondScore = (diamondCounts[player] ?: 0) * -30
                            val trickScore = (trickCounts[player] ?: 0) * -50
                            kingScore + queenScore + diamondScore + trickScore
                        }
                        onSubmit(scores)
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Submit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Whist mini-game result input — for the Whist sub-game in Rentz.
 * Each player's tricks taken are recorded, scoring +50 per trick.
 */
@Composable
fun RentzWhistScreen(
    players: List<String>,
    onSubmit: (Map<String, Int>) -> Unit,
    onBack: () -> Unit
) {
    val counts = remember { mutableStateMapOf<String, Int>().apply { players.forEach { put(it, 0) } } }
    val currentTotal = counts.values.sum()
    val totalTricks = 13

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text("Whist") },
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "+50 points per trick taken",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Total tricks: $currentTotal / $totalTricks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (currentTotal == totalTricks) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(players) { _, player ->
                    val count = counts[player] ?: 0
                    CountInputRow(
                        playerName = player,
                        count = count,
                        maxCount = minOf(13, totalTricks - currentTotal + count),
                        onCountChange = { newCount -> counts[player] = newCount }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val scores = players.associateWith { player ->
                        (counts[player] ?: 0) * 50
                    }
                    onSubmit(scores)
                },
                enabled = currentTotal == totalTricks,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Submit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

private fun ordinalSuffix(n: Int): String = when {
    n % 100 in 11..13 -> "th"
    n % 10 == 1 -> "st"
    n % 10 == 2 -> "nd"
    n % 10 == 3 -> "rd"
    else -> "th"
}
