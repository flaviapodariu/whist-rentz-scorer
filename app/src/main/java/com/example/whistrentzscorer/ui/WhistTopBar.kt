package com.example.whistrentzscorer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.example.whistrentzscorer.ui.theme.SoftIndigo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhistTopBar(
    title: @Composable () -> Unit,
    onBack: () -> Unit,
    isInGame: Boolean = false,
    isRentz: Boolean = false,
    onBid: () -> Unit = {},
    onInputResults: () -> Unit = {},
    onSelectMiniGame: () -> Unit = {},
    undoLastTurn: () -> Unit = {},
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.padding(8.dp),
                    contentDescription = "Back",
                )
            }
        },
        colors = TopAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = SoftIndigo,
            scrolledContainerColor = SoftIndigo,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            if (isInGame) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRentz) {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier.padding(horizontal = 16.dp)
                                .weight(2f),
                            onClick = { onSelectMiniGame() },
                        ) {
                            Text(
                                text = "Select Mini Game",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(0.85f))
                        Button(
                            modifier = Modifier.padding(horizontal = 16.dp)
                                .weight(1f),
                            onClick = { onBid() },
                        ) {
                            Text(
                                text = "Bid",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                        Spacer(modifier = Modifier.weight(0.5f))

                        Button(
                            modifier = Modifier.padding(horizontal = 6.dp)
                                .weight(1f),
                            onClick = { onInputResults() },
                        ) {
                            Text(
                                text = "Input Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                        Spacer(modifier = Modifier.weight(0.85f))
                    }
                }
            } else title
        },
        actions = {
            if (isInGame) {
                IconButton(
                    onClick = { undoLastTurn() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "undo last turn's scoring"
                    )
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onSearch: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {}  // TODO
            ) {
                Icon(
                    Icons.Filled.Menu,
                    modifier = Modifier.padding(8.dp),
                    contentDescription = "App Menu"
                )
            }
        },
        colors = TopAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            scrolledContainerColor = Color.Magenta,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = {
            IconButton(
                onClick = onSearch
            ) {
                Icon(
                    Icons.Filled.Search,
                    modifier = Modifier.padding(8.dp),
                    contentDescription = "Search available games"
                )
            }
        },
        title = { Text("Game History") },

        )
}