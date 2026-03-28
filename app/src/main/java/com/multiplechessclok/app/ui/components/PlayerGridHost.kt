package com.multiplechessclok.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.multiplechessclok.app.presentation.PlayerCardUiState

private val GridHostPadding = 8.dp

/**
 * Lays out [PlayerGrid] in landscape-first coordinates: the grid’s major axis follows the
 * longer physical screen edge. In natural portrait, the grid is measured in a landscape
 * box (swapped width/height) and rotated −90° so rows/columns align with the long side.
 */
@Composable
fun LandscapeAlignedPlayerGridHost(
    players: List<PlayerCardUiState>,
    columns: Int,
    onPlayerClick: (String) -> Unit,
    onReorderByPlayerIds: (fromPlayerId: String, toPlayerId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val isNaturalPortrait = maxHeight > maxWidth
        val longSide = maxOf(maxWidth, maxHeight)
        val shortSide = minOf(maxWidth, maxHeight)

        if (!isNaturalPortrait) {
            PlayerGrid(
                players = players,
                columns = columns,
                onPlayerClick = onPlayerClick,
                onReorderByPlayerIds = onReorderByPlayerIds,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(GridHostPadding)
            )
            return@BoxWithConstraints
        }

        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .requiredSize(longSide, shortSide)
                    .graphicsLayer {
                        rotationZ = -90f
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
            ) {
                PlayerGrid(
                    players = players,
                    columns = columns,
                    onPlayerClick = onPlayerClick,
                    onReorderByPlayerIds = onReorderByPlayerIds,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(GridHostPadding)
                )
            }
        }
    }
}
