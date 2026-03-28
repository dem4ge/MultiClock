package com.multiplechessclok.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import com.multiplechessclok.app.presentation.PlayerCardUiState
import com.multiplechessclok.app.ui.components.PlayerGrid
import com.multiplechessclok.app.ui.theme.ChessClockTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerGridReorderUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun long_press_drag_invokes_reorder_callback_with_stable_ids() {
        var ids by mutableStateOf(listOf("player-0", "player-1", "player-2", "player-3"))
        val reorderCalls = mutableListOf<Pair<String, String>>()

        fun applyReorder(from: String, to: String) {
            reorderCalls.add(from to to)
            val fromIndex = ids.indexOf(from)
            val toIndex = ids.indexOf(to)
            if (fromIndex < 0 || toIndex < 0) return
            val next = ids.toMutableList()
            val moved = next.removeAt(fromIndex)
            next.add(toIndex, moved)
            ids = next
        }

        composeRule.setContent {
            ChessClockTheme(darkTheme = true) {
                val players = ids.mapIndexed { index, id ->
                    PlayerCardUiState(
                        id = id,
                        displayName = null,
                        remainingMs = 60_000L,
                        paletteIndex = index.coerceAtMost(5),
                        isActive = false
                    )
                }
                Column {
                    Text(
                        text = ids.joinToString(","),
                        modifier = Modifier.testTag("order_readout")
                    )
                    PlayerGrid(
                        players = players,
                        columns = 2,
                        onPlayerClick = { },
                        onReorderByPlayerIds = { f, t -> applyReorder(f, t) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("player_player-0", useUnmergedTree = true).performTouchInput {
            down(center)
            advanceEventTime(600L)
            moveBy(Offset(400f, 20f))
            advanceEventTime(50L)
            up()
        }

        composeRule.waitForIdle()

        assertTrue(
            "Expected reorder callback or list mutation",
            reorderCalls.isNotEmpty() || ids != listOf("player-0", "player-1", "player-2", "player-3")
        )
    }
}
