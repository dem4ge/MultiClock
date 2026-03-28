package com.multiplechessclok.app.data

import com.multiplechessclok.app.domain.ChessClockRules

object GameConfigurationDefaults {
    val initialPlayerCount: Int = ChessClockRules.DEFAULT_PLAYER_COUNT
    val initialDurationMs: Long = ChessClockRules.DEFAULT_DURATION_MS
}
