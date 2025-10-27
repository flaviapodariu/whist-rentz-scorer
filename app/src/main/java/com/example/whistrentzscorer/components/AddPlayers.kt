package com.example.whistrentzscorer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun AddPlayerComponent(
    playerName: String,
    onNameChange: (String) -> Unit,
    onAddPlayer: () -> Unit,
    players: List<String>,
) {
    val playersNames = players.map{ it }.toSet()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = playerName,
            onValueChange = onNameChange,
            label = {
                if (playerName.isNotBlank() && playersNames.contains(playerName.trim())) {
                    Text(
                        text = "Player already added",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else Text("Enter Player Name")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAddPlayer() }),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onAddPlayer,
            enabled = playerName.isNotBlank() && !playersNames.contains(playerName),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.height(56.dp)
        ) {
            Text("ADD")
        }
    }
}

@Composable
fun PlayerList(
    players: List<String>,
    onRemovePlayer: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(items = players, key = { it }) { player ->
            PlayerItem(
                player = player,
                onRemove = { onRemovePlayer(player) }
            )
        }
    }
}

@Composable
fun PlayerItem(
    player: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = player,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove $player",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}