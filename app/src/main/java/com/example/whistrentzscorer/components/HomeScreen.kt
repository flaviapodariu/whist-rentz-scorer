package com.example.whistrentzscorer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.whistrentzscorer.R
import com.example.whistrentzscorer.ui.theme.Coral40
import com.example.whistrentzscorer.ui.theme.DeepPurpleButton
import com.example.whistrentzscorer.ui.theme.LightLavender
import com.example.whistrentzscorer.ui.theme.PaleBlue
import com.example.whistrentzscorer.ui.theme.SlateBlue
import com.example.whistrentzscorer.ui.theme.SoftIndigo
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
        targetValue = if (isRentz) Coral40 else SoftIndigo,
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
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onModeChanged("whist") },
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
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onModeChanged("rentz") },
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
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// V2 Home Screen — drop-in replacement with a more polished look.
// To switch, change the NavHost call from HomeScreen(...) to HomeScreenV2(...).
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreenV2(
    modifier: Modifier,
    onCreateGame: () -> Unit,
    onReviewHistory: () -> Unit,
    onResume: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val gameToResume = viewModel.gameToResume.collectAsState().value
    val selectedGameMode by viewModel.selectedGameMode.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PaleBlue,
                        LightLavender,
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(600)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        modifier = Modifier.size(160.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img),
                                contentDescription = "Homescreen logo",
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (selectedGameMode == "whist") "Whist" else "Rentz",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SoftIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Game mode toggle ────────────────────────────────────────
            GameModeToggle(
                selectedMode = selectedGameMode,
                onModeChanged = { viewModel.setGameMode(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Action cards ────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 200)) + slideInVertically(
                    initialOffsetY = { 60 },
                    animationSpec = tween(800, delayMillis = 200)
                )
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Add,
                            title = "New Game",
                            subtitle = "Start a fresh match",
                            containerColor = SoftIndigo,
                            onClick = onCreateGame
                        )
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.List,
                            title = "History",
                            subtitle = "Review past games",
                            containerColor = DeepPurpleButton,
                            onClick = onReviewHistory
                        )
                    }

                    // Resume button — full width, accent color when available
                    ActionCard(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.PlayArrow,
                        title = "Resume Game",
                        subtitle = if (gameToResume != null)
                            "Continue where you left off"
                        else
                            "No unfinished game",
                        containerColor = if (gameToResume != null) Coral40 else Color.Gray,
                        enabled = gameToResume != null,
                        onClick = onResume
                    )
                }
            }

            Spacer(modifier = Modifier.weight(2f))
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .shadow(
                elevation = if (enabled) 6.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = containerColor.copy(alpha = 0.3f),
                spotColor = containerColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}