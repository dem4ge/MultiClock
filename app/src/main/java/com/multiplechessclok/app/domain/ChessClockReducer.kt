package com.multiplechessclok.app.domain

import com.multiplechessclok.app.domain.model.ChessClockDomainState
import com.multiplechessclok.app.domain.model.ChessClockEvent
import com.multiplechessclok.app.domain.model.PlayerId
import com.multiplechessclok.app.domain.model.PlayerSlot

object ChessClockReducer {

    fun reduce(state: ChessClockDomainState, event: ChessClockEvent): ChessClockDomainState = when (event) {
        is ChessClockEvent.TapPlayer -> reduceTap(state, event.playerId)
        ChessClockEvent.Pause -> reducePause(state)
        is ChessClockEvent.Reorder -> reduceReorder(state, event.fromIndex, event.toIndex)
        is ChessClockEvent.ReorderByPlayerIds -> reduceReorderByPlayerIds(state, event.fromPlayerId, event.toPlayerId)
        is ChessClockEvent.ApplySettings -> reduceApplySettings(state, event)
        ChessClockEvent.Reset -> reduceReset(state)
        ChessClockEvent.Tick -> reduceTick(state)
    }

    fun initialState(
        playerCount: Int = ChessClockRules.DEFAULT_PLAYER_COUNT,
        defaultDurationMs: Long = ChessClockRules.DEFAULT_DURATION_MS,
        isReverseMode: Boolean = false
    ): ChessClockDomainState {
        val n = playerCount.coerceIn(ChessClockRules.MIN_PLAYER_COUNT, ChessClockRules.MAX_PLAYER_COUNT)
        return ChessClockDomainState(
            players = buildPlayers(n, defaultDurationMs, isReverseMode),
            activePlayerId = null,
            isPaused = false,
            defaultDurationMs = defaultDurationMs,
            isReverseMode = isReverseMode
        )
    }

    private fun reduceTap(state: ChessClockDomainState, playerId: PlayerId): ChessClockDomainState {
        val idx = state.players.indexOfFirst { it.id == playerId }
        if (idx == ChessClockRules.LIST_NOT_FOUND_INDEX) return state

        val active = state.activePlayerId
        when {
            active == null -> {
                return state.copy(
                    activePlayerId = playerId,
                    isPaused = false
                )
            }
            active != playerId -> return state
            state.isPaused -> {
                return state.copy(isPaused = false)
            }
            else -> {
                val nextIdx = ChessClockGridTurns.nextClockwiseIndex(idx, state.players.size)
                val nextId = state.players[nextIdx].id
                return state.copy(
                    activePlayerId = nextId,
                    isPaused = false
                )
            }
        }
    }

    private fun reducePause(state: ChessClockDomainState): ChessClockDomainState {
        if (state.activePlayerId == null) return state
        return state.copy(isPaused = !state.isPaused)
    }

    private fun reduceReorder(state: ChessClockDomainState, fromIndex: Int, toIndex: Int): ChessClockDomainState {
        if (fromIndex == toIndex) return state
        if (fromIndex !in state.players.indices || toIndex !in state.players.indices) return state
        val list = state.players.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        return state.copy(players = list)
    }

    private fun reduceReorderByPlayerIds(
        state: ChessClockDomainState,
        fromPlayerId: PlayerId,
        toPlayerId: PlayerId
    ): ChessClockDomainState {
        if (fromPlayerId == toPlayerId) return state
        val fromIndex = state.players.indexOfFirst { it.id == fromPlayerId }
        val toIndex = state.players.indexOfFirst { it.id == toPlayerId }
        if (fromIndex == ChessClockRules.LIST_NOT_FOUND_INDEX || toIndex == ChessClockRules.LIST_NOT_FOUND_INDEX) {
            return state
        }
        return reduceReorder(state, fromIndex, toIndex)
    }

    private fun reduceApplySettings(
        state: ChessClockDomainState,
        event: ChessClockEvent.ApplySettings
    ): ChessClockDomainState {
        val n = event.newPlayerCount.coerceIn(ChessClockRules.MIN_PLAYER_COUNT, ChessClockRules.MAX_PLAYER_COUNT)
        val duration = event.newDefaultDurationMs.coerceAtLeast(ChessClockRules.MIN_DURATION_MS)
        val old = state.players
        val modeChanged = event.reverseMode != state.isReverseMode
        val durationChanged = duration != state.defaultDurationMs
        val baselineMs = if (event.reverseMode) ChessClockRules.MIN_REMAINING_MS else duration
        val players = (0 until n).map { i ->
            val name = event.namesByPaletteIndex[i]?.trim()?.takeIf { it.isNotEmpty() }
            val paletteIndex = resolvePaletteIndex(event, old, i)
            val remainingMs = when {
                modeChanged -> if (event.reverseMode) ChessClockRules.MIN_REMAINING_MS else duration
                durationChanged -> baselineMs
                i < old.size -> old[i].remainingMs
                else -> if (event.reverseMode) ChessClockRules.MIN_REMAINING_MS else duration
            }
            if (i < old.size) {
                val prev = old[i]
                prev.copy(
                    displayName = name,
                    remainingMs = remainingMs,
                    paletteIndex = paletteIndex
                )
            } else {
                PlayerSlot(
                    id = PlayerId("player-$i"),
                    displayName = name,
                    remainingMs = remainingMs,
                    paletteIndex = paletteIndex
                )
            }
        }
        val activeId = state.activePlayerId
        val activeStillPresent = activeId != null && players.any { it.id == activeId }
        val clearedForNewDuration = durationChanged
        return ChessClockDomainState(
            players = players,
            activePlayerId = when {
                !activeStillPresent -> null
                clearedForNewDuration -> null
                else -> activeId
            },
            isPaused = when {
                !activeStillPresent || clearedForNewDuration -> false
                else -> state.isPaused
            },
            defaultDurationMs = duration,
            isReverseMode = event.reverseMode
        )
    }

    private fun reduceReset(state: ChessClockDomainState): ChessClockDomainState {
        val duration = state.defaultDurationMs
        val baseline = if (state.isReverseMode) ChessClockRules.MIN_REMAINING_MS else duration
        return state.copy(
            players = state.players.map {
                it.copy(remainingMs = baseline)
            },
            activePlayerId = null,
            isPaused = false
        )
    }

    private fun reduceTick(state: ChessClockDomainState): ChessClockDomainState {
        if (state.isPaused || state.activePlayerId == null) return state
        val activeId = state.activePlayerId
        val players = state.players.map { p ->
            if (p.id != activeId) p
            else {
                if (state.isReverseMode) {
                    val next = p.remainingMs + ChessClockRules.TIMER_TICK_INTERVAL_MS
                    p.copy(remainingMs = next)
                } else {
                    val next = (p.remainingMs - ChessClockRules.TIMER_TICK_INTERVAL_MS)
                        .coerceAtLeast(ChessClockRules.MIN_REMAINING_MS)
                    p.copy(remainingMs = next)
                }
            }
        }
        return state.copy(players = players)
    }

    private fun buildPlayers(count: Int, durationMs: Long, reverse: Boolean): List<PlayerSlot> =
        (0 until count).map { i ->
            PlayerSlot(
                id = PlayerId("player-$i"),
                displayName = null,
                remainingMs = if (reverse) ChessClockRules.MIN_REMAINING_MS else durationMs,
                paletteIndex = i % ChessClockRules.PLAYER_PALETTE_SIZE
            )
        }

    private fun resolvePaletteIndex(
        event: ChessClockEvent.ApplySettings,
        old: List<PlayerSlot>,
        index: Int
    ): Int {
        val fromSheet = event.paletteIndicesByPlayerIndex
        if (index < fromSheet.size) {
            return fromSheet[index].coerceIn(0, ChessClockRules.PLAYER_PALETTE_LAST_INDEX)
        }
        if (index < old.size) return old[index].paletteIndex
        return index % ChessClockRules.PLAYER_PALETTE_SIZE
    }
}
