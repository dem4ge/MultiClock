package com.multiplechessclok.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.multiplechessclok.app.ui.theme.ChessClockTheme

private val ButtonSize = 50.dp
private val BarVerticalPadding = 10.dp
private val BarHorizontalPadding = 16.dp

/** Slightly above [BottomBarBackground] so controls stay visible but close to the main chrome. */
private val BottomBarBackground = Color(0xFF16191D)
private val BottomBarIconButtonContainer = Color(0xFF1F232B)

@Composable
fun ChessClockBottomBar(
    onPause: () -> Unit,
    onOpenSettings: () -> Unit,
    onResetTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BottomBarBackground)
            .padding(
                horizontal = BarHorizontalPadding,
                vertical = BarVerticalPadding
            ),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(ButtonSize),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = BottomBarIconButtonContainer,
                contentColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Icon(Icons.Filled.Settings, contentDescription = null)
        }
        FilledIconButton(
            onClick = onPause,
            modifier = Modifier.size(ButtonSize),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = BottomBarIconButtonContainer,
                contentColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Icon(Icons.Filled.Pause, contentDescription = null)
        }
        FilledIconButton(
            onClick = onResetTimer,
            modifier = Modifier.size(ButtonSize),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = BottomBarIconButtonContainer,
                contentColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Icon(Icons.Filled.RestartAlt, contentDescription = null)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomBarPreview() {
    ChessClockTheme(darkTheme = true) {
        ChessClockBottomBar(onPause = {}, onOpenSettings = {}, onResetTimer = {})
    }
}
