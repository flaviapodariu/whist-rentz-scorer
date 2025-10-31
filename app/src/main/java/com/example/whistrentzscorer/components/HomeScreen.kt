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
                HomeButton(
                    onClick = onCreateGame,
                    text = "New Game"
                )

                HomeButton(
                    onClick = onReviewHistory,
                    text = "Games History"
                )
            }

            HomeButton(
                onClick = onResume,
                text = "Resume",
                enabled = gameToResume != null
            )
        }

        Spacer(modifier = Modifier.weight(3f))
    }
}

@Composable
fun HomeButton(
    onClick: ()-> Unit,
    text: String,
    enabled: Boolean = true,
    weight: Float? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
        )
    }
}