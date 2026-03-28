package com.multiplechessclok.app.domain

import com.multiplechessclok.app.domain.model.ChessClockDomainState
import com.multiplechessclok.app.domain.model.ChessClockEvent

object ChessClockTimerEngine {
    fun applyTick(state: ChessClockDomainState): ChessClockDomainState =
        ChessClockReducer.reduce(state, ChessClockEvent.Tick)
}
