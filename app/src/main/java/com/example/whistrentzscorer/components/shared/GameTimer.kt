package com.example.whistrentzscorer.components.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.whistrentzscorer.viewmodels.whist.state.GameStateViewModel
import java.util.Locale

@Composable
fun GameTimer(
    stateVM: GameStateViewModel,
    modifier: Modifier = Modifier
) {
    val elapsedSeconds = stateVM.elapsedSeconds
    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    Box(modifier = modifier.padding(8.dp)) {
        Text(
            text = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
    }
}
