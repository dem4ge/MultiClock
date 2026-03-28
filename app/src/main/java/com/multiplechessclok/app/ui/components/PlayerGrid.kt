package com.multiplechessclok.app.ui.components

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.multiplechessclok.app.presentation.PlayerCardUiState
import com.multiplechessclok.app.ui.TimeFormatting
import kotlin.math.ceil

private val GridGutter = 10.dp

/**
 * Five players: rows [0–1], [4–2], bottom row centers 3 with same cell width as above (half of grid) so sizes match after [LandscapeAlignedPlayerGridHost] rotation.
 */
@Composable
fun PlayerGrid(
    players: List<PlayerCardUiState>,
    columns: Int,
    onPlayerClick: (String) -> Unit,
    onReorderByPlayerIds: (fromPlayerId: String, toPlayerId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeColumns = columns.coerceAtLeast(1)
    val rowCount = ceil(players.size / safeColumns.toFloat()).toInt().coerceAtLeast(1)

    var draggingPlayerId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val layoutCoordsByPlayerId = remember { mutableMapOf<String, LayoutCoordinates>() }

    val latestClick = rememberUpdatedState(onPlayerClick)
    val latestReorder = rememberUpdatedState(onReorderByPlayerIds)
    val latestPlayers = rememberUpdatedState(players)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (players.size == 5 && safeColumns == 2) {
            val p = players
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                GridPlayerCell(
                    player = p[0],
                    draggingPlayerId = draggingPlayerId,
                    dragOffset = dragOffset,
                    layoutCoordsByPlayerId = layoutCoordsByPlayerId,
                    onDraggingChange = { draggingPlayerId = it },
                    onDragOffsetChange = { dragOffset = it },
                    latestClick = latestClick,
                    latestReorder = latestReorder,
                    latestPlayers = latestPlayers,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(GridGutter / 2)
                )
                GridPlayerCell(
                    player = p[1],
                    draggingPlayerId = draggingPlayerId,
                    dragOffset = dragOffset,
                    layoutCoordsByPlayerId = layoutCoordsByPlayerId,
                    onDraggingChange = { draggingPlayerId = it },
                    onDragOffsetChange = { dragOffset = it },
                    latestClick = latestClick,
                    latestReorder = latestReorder,
                    latestPlayers = latestPlayers,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(GridGutter / 2)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                GridPlayerCell(
                    player = p[4],
                    draggingPlayerId = draggingPlayerId,
                    dragOffset = dragOffset,
                    layoutCoordsByPlayerId = layoutCoordsByPlayerId,
                    onDraggingChange = { draggingPlayerId = it },
                    onDragOffsetChange = { dragOffset = it },
                    latestClick = latestClick,
                    latestReorder = latestReorder,
                    latestPlayers = latestPlayers,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(GridGutter / 2)
                )
                GridPlayerCell(
                    player = p[2],
                    draggingPlayerId = draggingPlayerId,
                    dragOffset = dragOffset,
                    layoutCoordsByPlayerId = layoutCoordsByPlayerId,
                    onDraggingChange = { draggingPlayerId = it },
                    onDragOffsetChange = { dragOffset = it },
                    latestClick = latestClick,
                    latestReorder = latestReorder,
                    latestPlayers = latestPlayers,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(GridGutter / 2)
                )
            }
            // Same cell width as rows above (half of row): 1+2+1 weights → middle is 50% wide; full width would become a tall stripe after portrait rotation.
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                GridPlayerCell(
                    player = p[3],
                    draggingPlayerId = draggingPlayerId,
                    dragOffset = dragOffset,
                    layoutCoordsByPlayerId = layoutCoordsByPlayerId,
                    onDraggingChange = { draggingPlayerId = it },
                    onDragOffsetChange = { dragOffset = it },
                    latestClick = latestClick,
                    latestReorder = latestReorder,
                    latestPlayers = latestPlayers,
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .padding(GridGutter / 2)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        } else {
            for (row in 0 until rowCount) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    for (col in 0 until safeColumns) {
                        val index = row * safeColumns + col
                        if (index >= players.size) {
                            Spacer(modifier = Modifier.weight(1f))
                            continue
                        }
                        val player = players[index]
                        GridPlayerCell(
                            player = player,
                            draggingPlayerId = draggingPlayerId,
                            dragOffset = dragOffset,
                            layoutCoordsByPlayerId = layoutCoordsByPlayerId,
                            onDraggingChange = { draggingPlayerId = it },
                            onDragOffsetChange = { dragOffset = it },
                            latestClick = latestClick,
                            latestReorder = latestReorder,
                            latestPlayers = latestPlayers,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(GridGutter / 2)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridPlayerCell(
    player: PlayerCardUiState,
    draggingPlayerId: String?,
    dragOffset: Offset,
    layoutCoordsByPlayerId: MutableMap<String, LayoutCoordinates>,
    onDraggingChange: (String?) -> Unit,
    onDragOffsetChange: (Offset) -> Unit,
    latestClick: State<(String) -> Unit>,
    latestReorder: State<(String, String) -> Unit>,
    latestPlayers: State<List<PlayerCardUiState>>,
    modifier: Modifier = Modifier
) {
    val isDraggingThis = draggingPlayerId == player.id

    key(player.id) {
        Box(
            modifier = modifier
                .onGloballyPositioned { coords ->
                    layoutCoordsByPlayerId[player.id] = coords
                }
                .zIndex(if (isDraggingThis) 20f else 0f)
                .semantics { contentDescription = "player_${player.id}" }
                .testTag("player_${player.id}")
                .pointerInput(player.id) {
                    while (true) {
                        awaitPointerEventScope {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val afterLongPress = awaitLongPressOrCancellation(down.id)
                            if (afterLongPress == null) {
                                latestClick.value(player.id)
                                return@awaitPointerEventScope
                            }

                            val draggedId = player.id
                            onDraggingChange(draggedId)
                            onDragOffsetChange(Offset.Zero)
                            var accumulated = Offset.Zero
                            var lastPointerWindow = Offset.Zero
                            var sawDragEvent = false

                            try {
                                val dragCompleted = drag(afterLongPress.id) { change ->
                                    val coords = layoutCoordsByPlayerId[draggedId]
                                    if (coords == null || !coords.isAttached) return@drag
                                    sawDragEvent = true
                                    accumulated += change.positionChange()
                                    onDragOffsetChange(accumulated)
                                    change.consume()
                                    lastPointerWindow = coords.localToWindow(change.position)
                                }
                                if (sawDragEvent && dragCompleted) {
                                    val orderedIds = latestPlayers.value.map { it.id }
                                    val dropTarget = orderedIds.firstOrNull { id ->
                                        if (id == draggedId) return@firstOrNull false
                                        val c = layoutCoordsByPlayerId[id]
                                        c != null && c.isAttached && c.boundsInWindow().contains(lastPointerWindow)
                                    }
                                    if (dropTarget != null) {
                                        latestReorder.value(draggedId, dropTarget)
                                    }
                                }
                            } finally {
                                onDraggingChange(null)
                                onDragOffsetChange(Offset.Zero)
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (isDraggingThis) {
                            translationX = dragOffset.x
                            translationY = dragOffset.y
                        }
                    }
            ) {
                PlayerCard(
                    player = player,
                    timeLabel = TimeFormatting.formatHhMmSs(player.remainingMs),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
