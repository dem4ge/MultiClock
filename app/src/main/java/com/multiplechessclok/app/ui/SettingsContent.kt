package com.multiplechessclok.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.multiplechessclok.app.domain.ChessClockRules
import com.multiplechessclok.app.presentation.SettingsUiState
import com.multiplechessclok.app.ui.theme.ChessClockTheme
import com.multiplechessclok.app.ui.theme.PlayerColors
import kotlin.math.roundToInt

private val SheetPadding = 20.dp
private val ColorSwatchSize = 36.dp
private val ColorSwatchGridSpacing = 8.dp
private val ColorButtonSwatchSize = 28.dp

@Composable
fun SettingsContent(
    settings: SettingsUiState,
    onDurationChange: (String) -> Unit,
    onPlayerCountChange: (Int) -> Unit,
    onPlayerNameChange: (Int, String) -> Unit,
    onPlayerPaletteChange: (Int, Int) -> Unit,
    onReverseModeChange: (Boolean) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = scheme.onSurface,
        unfocusedTextColor = scheme.onSurface,
        focusedLabelColor = scheme.primary,
        unfocusedLabelColor = scheme.onSurfaceVariant,
        cursorColor = scheme.primary,
        focusedBorderColor = scheme.primary,
        unfocusedBorderColor = scheme.outline
    )

    var expandedColorPickerIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(settings.playerCount) {
        val open = expandedColorPickerIndex
        if (open != null && open >= settings.playerCount) {
            expandedColorPickerIndex = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(SheetPadding)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = scheme.onBackground
        )
        Text(
            text = "Global time (minutes)",
            style = MaterialTheme.typography.titleSmall,
            color = scheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = settings.durationMinutesInput,
            onValueChange = onDurationChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            label = { Text("Minutes") },
            colors = textFieldColors
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = "Reverse mode",
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = "Count elapsed time from 00:00 instead of remaining time",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
            }
            Switch(
                checked = settings.reverseMode,
                onCheckedChange = onReverseModeChange
            )
        }

        Text(
            text = "Players (${settings.playerCount})",
            style = MaterialTheme.typography.titleSmall,
            color = scheme.onSurfaceVariant
        )
        val range =
            ChessClockRules.MIN_PLAYER_COUNT.toFloat()..ChessClockRules.MAX_PLAYER_COUNT.toFloat()
        Slider(
            value = settings.playerCount.toFloat(),
            onValueChange = { v -> onPlayerCountChange(v.roundToInt()) },
            valueRange = range,
            steps = (ChessClockRules.MAX_PLAYER_COUNT - ChessClockRules.MIN_PLAYER_COUNT - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = scheme.primary,
                activeTrackColor = scheme.primary,
                inactiveTrackColor = scheme.surfaceVariant,
                activeTickColor = scheme.primary,
                inactiveTickColor = scheme.onSurfaceVariant
            )
        )

        for (index in 0 until settings.playerCount) {
            val name = settings.playerNames.getOrElse(index) { "" }
            val selected =
                settings.playerPaletteIndices.getOrElse(index) { index % ChessClockRules.PLAYER_PALETTE_SIZE }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { onPlayerNameChange(index, it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Player ${index + 1}") },
                        singleLine = true,
                        colors = textFieldColors
                    )
                    OutlinedButton(
                        onClick = {
                            expandedColorPickerIndex =
                                if (expandedColorPickerIndex == index) null else index
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(ColorButtonSwatchSize)
                                    .clip(CircleShape)
                                    .background(PlayerColors.at(selected))
                                    .border(
                                        1.dp,
                                        scheme.outline.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                            )
                            Text(
                                text = "Choose color",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                if (expandedColorPickerIndex == index) {
                    PlayerColorPalettePicker(
                        selectedIndex = selected,
                        onSelect = {
                            onPlayerPaletteChange(index, it)
                            expandedColorPickerIndex = null
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onApply, modifier = Modifier.weight(1f)) {
                Text("Apply")
            }
            TextButton(
                onClick = onReset,
                colors = ButtonDefaults.textButtonColors(contentColor = scheme.onSurface)
            ) {
                Text("Reset timers")
            }
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = scheme.onSurface)
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun PlayerColorPalettePicker(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ColorSwatchGridSpacing)
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(ColorSwatchGridSpacing)) {
                    repeat(3) { col ->
                        val idx = row * 3 + col
                        val selected = idx == selectedIndex
                        Box(
                            modifier = Modifier
                                .size(ColorSwatchSize)
                                .clip(CircleShape)
                                .background(PlayerColors.at(idx))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) scheme.primary else scheme.outline.copy(alpha = 0.45f),
                                    shape = CircleShape
                                )
                                .clickable { onSelect(idx) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreviewDark() {
    ChessClockTheme(darkTheme = true) {
        SettingsContent(
            settings = SettingsUiState(
                isOpen = true,
                durationMinutesInput = "60",
                playerCount = 4,
                playerNames = listOf("", "A", "", "", "", ""),
                playerPaletteIndices = listOf(0, 1, 2, 3, 4, 5),
                reverseMode = false
            ),
            onDurationChange = {},
            onPlayerCountChange = {},
            onPlayerNameChange = { _, _ -> },
            onPlayerPaletteChange = { _, _ -> },
            onReverseModeChange = {},
            onApply = {},
            onReset = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreviewLight() {
    ChessClockTheme(darkTheme = false) {
        SettingsContent(
            settings = SettingsUiState(
                isOpen = true,
                durationMinutesInput = "60",
                playerCount = 4,
                playerNames = listOf("", "A", "", "", "", ""),
                playerPaletteIndices = listOf(0, 1, 2, 3, 4, 5),
                reverseMode = false
            ),
            onDurationChange = {},
            onPlayerCountChange = {},
            onPlayerNameChange = { _, _ -> },
            onPlayerPaletteChange = { _, _ -> },
            onReverseModeChange = {},
            onApply = {},
            onReset = {},
            onDismiss = {}
        )
    }
}
