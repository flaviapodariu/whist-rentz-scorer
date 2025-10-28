package com.example.whistrentzscorer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.whistrentzscorer.ui.WhistTopBar
import com.example.whistrentzscorer.viewmodels.GameStateViewModel

@Composable
fun RoundActionScreen(
    action: String,
    onBack: () -> Unit,
    gameStateViewModel: GameStateViewModel = hiltViewModel()
) {

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

    Scaffold(
        topBar = {
            WhistTopBar(
                title = { Text(text = "") },
                onBack = onBack
            )
        }
    ) { padding ->
        val round = gameStateViewModel.currentRound

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { currentPlayer -= 1 },
                    enabled = {  },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBackIosNew,
                        contentDescription = "prev player"
                    )
                }
                Text(
                    text = gameStateViewModel.playerList[currentPlayer],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
                )

                IconButton(
                    onClick = { currentPlayer += 1 },
                    enabled = true,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "next player"
                    )
                }
            }


            ValueChooser()

        }


    }
}

@Composable
fun ValueChooser() {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (value in 1..8) {
            Box {
                Text(text = value.toString())
            }
        }
    }
}