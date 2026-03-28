package com.multiplechessclok.app.domain

object ChessClockRules {
    /** Количество предустановленных цветов карточек игроков (индексы 0..LAST). */
    const val PLAYER_PALETTE_SIZE = 9
    const val PLAYER_PALETTE_LAST_INDEX = PLAYER_PALETTE_SIZE - 1

    const val MIN_PLAYER_COUNT = 2
    const val MAX_PLAYER_COUNT = 6
    const val DEFAULT_PLAYER_COUNT = 4
    const val DEFAULT_DURATION_MS = 3_600_000L
    const val TIMER_TICK_INTERVAL_MS = 1_000L
    const val MIN_DURATION_MS = 1_000L
    const val MIN_REMAINING_MS = 0L
    const val LIST_NOT_FOUND_INDEX = -1
}
