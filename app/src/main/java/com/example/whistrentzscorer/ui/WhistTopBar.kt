package com.example.whistrentzscorer.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhistTopBar(
    title: @Composable () -> Unit,
    onBack: () -> Unit
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
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
            scrolledContainerColor = Color.Magenta,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = title,

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
            )  {
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