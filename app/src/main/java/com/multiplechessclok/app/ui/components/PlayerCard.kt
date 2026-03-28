package com.multiplechessclok.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.multiplechessclok.app.presentation.PlayerCardUiState
import com.multiplechessclok.app.ui.theme.ChessClockTheme
import com.multiplechessclok.app.ui.theme.PlayerColors
import com.multiplechessclok.app.ui.TimeFormatting

private val CardCornerRadius = 18.dp
private val InactiveDim = 0.48f
private val TimerSizeSp = 34.sp
private val NameSizeSp = 15.sp

@Composable
fun PlayerCard(
    player: PlayerCardUiState,
    timeLabel: String,
    modifier: Modifier = Modifier
) {
    val base = PlayerColors.at(player.paletteIndex)
    val fill = if (player.isActive) base else base.copy(alpha = InactiveDim)
    val textColor = Color(0xFF101010).copy(alpha = if (player.isActive) 1f else 0.88f)

    Column(
        modifier = modifier
            .background(fill, RoundedCornerShape(CardCornerRadius))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val name = player.displayName
        if (!name.isNullOrBlank()) {
            Text(
                text = name,
                color = textColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = NameSizeSp,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .alpha(if (player.isActive) 1f else 0.9f)
            )
        }
        Text(
            text = timeLabel,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = TimerSizeSp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerCardPreview() {
    ChessClockTheme(darkTheme = true) {
        Box(Modifier.padding(16.dp)) {
            PlayerCard(
                player = PlayerCardUiState(
                    id = "p0",
                    displayName = "Alex",
                    remainingMs = 3_599_000L,
                    paletteIndex = 0,
                    isActive = true
                ),
                timeLabel = TimeFormatting.formatHhMmSs(3_599_000L),
                modifier = Modifier.size(width = 160.dp, height = 120.dp)
            )
        }
    }
}
