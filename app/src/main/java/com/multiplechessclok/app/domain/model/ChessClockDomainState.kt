package com.multiplechessclok.app.domain.model

data class ChessClockDomainState(
    val players: List<PlayerSlot>,
    val activePlayerId: PlayerId?,
    val isPaused: Boolean,
    val defaultDurationMs: Long,
    /** Если true, [PlayerSlot.remainingMs] — накопленное время игрока (от 00:00); иначе — остаток до нуля. */
    val isReverseMode: Boolean
)
