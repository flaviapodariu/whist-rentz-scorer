package com.example.whistrentzscorer.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.whistrentzscorer.R
import com.example.whistrentzscorer.ui.theme.Teal40
import com.example.whistrentzscorer.ui.theme.Coral40
import com.example.whistrentzscorer.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier,
    onCreateGame: () -> Unit,
    onReviewHistory: () -> Unit,
    onResume: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val gameToResume = viewModel.gameToResume.collectAsState().value
    val selectedGameMode by viewModel.selectedGameMode.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        GameModeToggle(
            selectedMode = selectedGameMode,
            onModeChanged = { viewModel.setGameMode(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

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
fun GameModeToggle(
    selectedMode: String,
    onModeChanged: (String) -> Unit
) {
    val isRentz = selectedMode == "rentz"
    val sliderOffset by animateDpAsState(
        targetValue = if (isRentz) 140.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "sliderOffset"
    )
    val activeColor by animateColorAsState(
        targetValue = if (isRentz) Coral40 else Teal40,
        animationSpec = tween(durationMillis = 300),
        label = "activeColor"
    )

    Box(
        modifier = Modifier
            .width(280.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .offset(x = sliderOffset)
                .width(140.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(activeColor)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable { onModeChanged("whist") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Whist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (!isRentz) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable { onModeChanged("rentz") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Rentz",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRentz) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HomeButton(
    onClick: ()-> Unit,
    text: String,
    enabled: Boolean = true,
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