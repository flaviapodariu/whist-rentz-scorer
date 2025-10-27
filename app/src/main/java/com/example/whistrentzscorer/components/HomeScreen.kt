package com.example.whistrentzscorer.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.whistrentzscorer.R
import com.example.whistrentzscorer.viewmodels.HomeViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun HomeScreen(
    modifier: Modifier,
    onCreateGame: () -> Unit,
    onReviewHistory: () -> Unit,
    onResume: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val gameToResume = viewModel.gameToResume.collectAsState().value

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "Homescreen logo",
            Modifier.background(Color.Transparent)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onCreateGame,
                ) {
                    Text(
                        text = "New Game",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
                    )
                }

                Button(
                    onClick = onReviewHistory
                ) {
                    Text(
                        text = "Games History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
                    )
                }
            }

            Button(
                onClick = onResume,
                enabled = gameToResume != null
            ) {
                Text(
                    text = "Resume",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(3f))
    }
}