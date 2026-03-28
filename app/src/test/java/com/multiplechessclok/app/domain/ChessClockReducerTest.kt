package com.multiplechessclok.app.domain

import com.multiplechessclok.app.domain.model.ChessClockEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessClockReducerTest {

    private val base = ChessClockReducer.initialState(
        playerCount = 4,
        defaultDurationMs = 60_000L
    )

    @Test
    fun tick_decrementsActivePlayerOnly() {
        val p0 = base.players[0].id
        val running = ChessClockReducer.reduce(
            base.copy(activePlayerId = p0, isPaused = false),
            ChessClockEvent.Tick
        )
        assertEquals(59_000L, running.players[0].remainingMs)
        assertEquals(60_000L, running.players[1].remainingMs)
    }

    @Test
    fun tick_respectsFloorAtZero() {
        val p0 = base.players[0].id
        val almost = base.copy(
            activePlayerId = p0,
            isPaused = false,
            players = base.players.mapIndexed { i, p ->
                if (i == 0) p.copy(remainingMs = 500L) else p
            }
        )
        val after = ChessClockReducer.reduce(almost, ChessClockEvent.Tick)
        assertEquals(0L, after.players[0].remainingMs)
    }

    @Test
    fun first_tap_activatesPlayer() {
        val p0 = base.players[0].id
        val next = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        assertEquals(p0, next.activePlayerId)
        assertEquals(false, next.isPaused)
    }

    @Test
    fun tap_nonActive_isIgnoredWhenAnotherActive() {
        val p0 = base.players[0].id
        val p1 = base.players[1].id
        val active = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        val result = ChessClockReducer.reduce(active, ChessClockEvent.TapPlayer(p1))
        assertEquals(p0, result.activePlayerId)
    }

    @Test
    fun tap_activeAdvancesClockwise() {
        val p0 = base.players[0].id
        val p1 = base.players[1].id
        val step1 = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        val step2 = ChessClockReducer.reduce(step1, ChessClockEvent.TapPlayer(p0))
        assertEquals(p1, step2.activePlayerId)
    }

    @Test
    fun tap_activeFourPlayers_followsGridPerimeterNotDiagonal() {
        val p0 = base.players[0].id
        val p1 = base.players[1].id
        val p3 = base.players[3].id
        var state = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        state = ChessClockReducer.reduce(state, ChessClockEvent.TapPlayer(p0))
        assertEquals(p1, state.activePlayerId)
        state = ChessClockReducer.reduce(state, ChessClockEvent.TapPlayer(p1))
        assertEquals(p3, state.activePlayerId)
    }

    @Test
    fun tap_activeClockwiseWraps() {
        val p0 = base.players[0].id
        var state = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        repeat(base.players.size) {
            val activeId = state.activePlayerId!!
            state = ChessClockReducer.reduce(state, ChessClockEvent.TapPlayer(activeId))
        }
        assertEquals(p0, state.activePlayerId)
    }

    @Test
    fun pause_stopsHighlightRetention_keepsActive() {
        val p0 = base.players[0].id
        val running = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        val paused = ChessClockReducer.reduce(running, ChessClockEvent.Pause)
        assertEquals(true, paused.isPaused)
        assertEquals(p0, paused.activePlayerId)
    }

    @Test
    fun tick_doesNothingWhenPaused() {
        val p0 = base.players[0].id
        val running = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        val paused = ChessClockReducer.reduce(running, ChessClockEvent.Pause)
        val after = ChessClockReducer.reduce(paused, ChessClockEvent.Tick)
        assertEquals(60_000L, after.players[0].remainingMs)
    }

    @Test
    fun resumeActivePlayerViaTapWhenPaused() {
        val p0 = base.players[0].id
        val running = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        val paused = ChessClockReducer.reduce(running, ChessClockEvent.Pause)
        val resumed = ChessClockReducer.reduce(paused, ChessClockEvent.TapPlayer(p0))
        assertEquals(false, resumed.isPaused)
        assertEquals(p0, resumed.activePlayerId)
    }

    @Test
    fun pause_secondPressResumesWhenAlreadyPaused_likeTapActive() {
        val p0 = base.players[0].id
        val running = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        val paused = ChessClockReducer.reduce(running, ChessClockEvent.Pause)
        val resumed = ChessClockReducer.reduce(paused, ChessClockEvent.Pause)
        assertEquals(false, resumed.isPaused)
        assertEquals(p0, resumed.activePlayerId)
    }

    @Test
    fun reorder_movesSlot() {
        val reordered = ChessClockReducer.reduce(base, ChessClockEvent.Reorder(0, 2))
        assertEquals(base.players[1].id, reordered.players[0].id)
        assertEquals(base.players[2].id, reordered.players[1].id)
        assertEquals(base.players[0].id, reordered.players[2].id)
        assertEquals(base.players[3].id, reordered.players[3].id)
    }

    @Test
    fun reorderByPlayerIds_equivalent_to_index_reorder() {
        val p0 = base.players[0].id
        val p2 = base.players[2].id
        val byId = ChessClockReducer.reduce(
            base,
            ChessClockEvent.ReorderByPlayerIds(p0, p2)
        )
        val byIndex = ChessClockReducer.reduce(base, ChessClockEvent.Reorder(0, 2))
        assertEquals(byIndex.players.map { it.id }, byId.players.map { it.id })
    }

    @Test
    fun reorderByPlayerIds_preserves_active_player_identity() {
        val p0 = base.players[0].id
        val p2 = base.players[2].id
        val active = ChessClockReducer.reduce(base, ChessClockEvent.TapPlayer(p0))
        val after = ChessClockReducer.reduce(
            active,
            ChessClockEvent.ReorderByPlayerIds(p0, p2)
        )
        assertEquals(p0, after.activePlayerId)
        assertEquals(4, after.players.size)
    }

    @Test
    fun reset_restoresTimesAndClearsActive() {
        val p0 = base.players[0].id
        val ticked = ChessClockReducer.reduce(
            base.copy(activePlayerId = p0, isPaused = false),
            ChessClockEvent.Tick
        )
        val depleted = ticked.copy(
            players = ticked.players.mapIndexed { i, p ->
                if (i == 0) p.copy(remainingMs = 10_000L) else p
            }
        )
        val reset = ChessClockReducer.reduce(depleted, ChessClockEvent.Reset)
        assertNull(reset.activePlayerId)
        assertEquals(false, reset.isPaused)
        reset.players.forEach { assertEquals(60_000L, it.remainingMs) }
    }

    @Test
    fun applySettings_clampsPlayerCount() {
        val event = ChessClockEvent.ApplySettings(
            newPlayerCount = 99,
            newDefaultDurationMs = 120_000L,
            namesByPaletteIndex = mapOf(0 to "A")
        )
        val next = ChessClockReducer.reduce(base, event)
        assertEquals(ChessClockRules.MAX_PLAYER_COUNT, next.players.size)
        assertEquals(120_000L, next.defaultDurationMs)
    }

    @Test
    fun applySettings_clampsLowPlayerCount() {
        val event = ChessClockEvent.ApplySettings(
            newPlayerCount = 0,
            newDefaultDurationMs = 120_000L,
            namesByPaletteIndex = emptyMap()
        )
        val next = ChessClockReducer.reduce(base, event)
        assertEquals(ChessClockRules.MIN_PLAYER_COUNT, next.players.size)
    }

    @Test
    fun applySettings_preservesPaletteIndicesOrder() {
        val event = ChessClockEvent.ApplySettings(
            newPlayerCount = 3,
            newDefaultDurationMs = 90_000L,
            namesByPaletteIndex = emptyMap()
        )
        val next = ChessClockReducer.reduce(base, event)
        next.players.forEachIndexed { i, p -> assertEquals(i, p.paletteIndex) }
    }

    @Test
    fun applySettings_appliesPaletteIndicesFromSheet() {
        val event = ChessClockEvent.ApplySettings(
            newPlayerCount = 4,
            newDefaultDurationMs = 60_000L,
            namesByPaletteIndex = emptyMap(),
            paletteIndicesByPlayerIndex = listOf(8, 7, 6, 5)
        )
        val next = ChessClockReducer.reduce(base, event)
        assertEquals(8, next.players[0].paletteIndex)
        assertEquals(7, next.players[1].paletteIndex)
        assertEquals(6, next.players[2].paletteIndex)
        assertEquals(5, next.players[3].paletteIndex)
    }

    @Test
    fun applySettings_nameChange_doesNotResetRemainingTimes() {
        val p0 = base.players[0].id
        val running = ChessClockReducer.reduce(
            base.copy(activePlayerId = p0, isPaused = false),
            ChessClockEvent.Tick
        )
        val event = ChessClockEvent.ApplySettings(
            newPlayerCount = 4,
            newDefaultDurationMs = 60_000L,
            namesByPaletteIndex = mapOf(0 to "Alice")
        )
        val next = ChessClockReducer.reduce(running, event)
        assertEquals("Alice", next.players[0].displayName)
        assertEquals(59_000L, next.players[0].remainingMs)
        assertEquals(60_000L, next.players[1].remainingMs)
        assertEquals(60_000L, next.defaultDurationMs)
    }

    @Test
    fun applySettings_durationChange_resetsAllPlayersAndClearsActive() {
        val p0 = base.players[0].id
        val running = ChessClockReducer.reduce(
            base.copy(activePlayerId = p0, isPaused = false),
            ChessClockEvent.Tick
        )
        val event = ChessClockEvent.ApplySettings(
            newPlayerCount = 4,
            newDefaultDurationMs = 1_200_000L,
            namesByPaletteIndex = emptyMap()
        )
        val next = ChessClockReducer.reduce(running, event)
        assertNull(next.activePlayerId)
        assertFalse(next.isPaused)
        next.players.forEach { assertEquals(1_200_000L, it.remainingMs) }
        assertEquals(1_200_000L, next.defaultDurationMs)
    }

    @Test
    fun applySettings_nameChange_keepsActivePlayer() {
        val p1 = base.players[1].id
        val active = base.copy(activePlayerId = p1, isPaused = true)
        val event = ChessClockEvent.ApplySettings(
            newPlayerCount = 4,
            newDefaultDurationMs = 60_000L,
            namesByPaletteIndex = mapOf(1 to "Bob")
        )
        val next = ChessClockReducer.reduce(active, event)
        assertEquals(p1, next.activePlayerId)
        assertEquals("Bob", next.players[1].displayName)
        assertEquals(true, next.isPaused)
    }

    @Test
    fun applySettings_reducingPlayerCount_clearsActiveIfRemoved() {
        val p3 = base.players[3].id
        val active = base.copy(activePlayerId = p3, isPaused = false)
        val event = ChessClockEvent.ApplySettings(
            newPlayerCount = 3,
            newDefaultDurationMs = 60_000L,
            namesByPaletteIndex = emptyMap()
        )
        val next = ChessClockReducer.reduce(active, event)
        assertNull(next.activePlayerId)
        assertFalse(next.isPaused)
    }

    @Test
    fun reverseMode_tick_incrementsActivePlayerOnly() {
        val reverseBase = ChessClockReducer.initialState(
            playerCount = 4,
            defaultDurationMs = 60_000L,
            isReverseMode = true
        )
        val p0 = reverseBase.players[0].id
        val running = ChessClockReducer.reduce(
            reverseBase.copy(activePlayerId = p0, isPaused = false),
            ChessClockEvent.Tick
        )
        assertEquals(1_000L, running.players[0].remainingMs)
        assertEquals(0L, running.players[1].remainingMs)
    }

    @Test
    fun reverseMode_reset_setsAllToZero_andKeepsReverseFlag() {
        val reverseBase = ChessClockReducer.initialState(
            playerCount = 2,
            defaultDurationMs = 60_000L,
            isReverseMode = true
        )
        val p0 = reverseBase.players[0].id
        val ticked = ChessClockReducer.reduce(
            reverseBase.copy(activePlayerId = p0, isPaused = false),
            ChessClockEvent.Tick
        )
        val reset = ChessClockReducer.reduce(ticked, ChessClockEvent.Reset)
        assertTrue(reset.isReverseMode)
        reset.players.forEach { assertEquals(0L, it.remainingMs) }
    }

    @Test
    fun applySettings_togglesReverseMode_resetsTimes() {
        val p0 = base.players[0].id
        val running = ChessClockReducer.reduce(
            base.copy(activePlayerId = p0, isPaused = false),
            ChessClockEvent.Tick
        )
        val event = ChessClockEvent.ApplySettings(
            newPlayerCount = 4,
            newDefaultDurationMs = 60_000L,
            namesByPaletteIndex = emptyMap(),
            reverseMode = true
        )
        val next = ChessClockReducer.reduce(running, event)
        assertTrue(next.isReverseMode)
        next.players.forEach { assertEquals(0L, it.remainingMs) }
    }
}
