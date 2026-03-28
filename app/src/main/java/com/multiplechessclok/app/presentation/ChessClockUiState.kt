package com.multiplechessclok.app.presentation

import com.multiplechessclok.app.domain.ChessClockGridTurns

data class PlayerCardUiState(
    val id: String,
    val displayName: String?,
    val remainingMs: Long,
    val paletteIndex: Int,
    val isActive: Boolean
)

data class ChessClockUiState(
    val players: List<PlayerCardUiState>,
    val defaultDurationMs: Long,
    val playerCount: Int,
    val settings: SettingsUiState,
    val gridColumns: Int,
    /** Совпадает с domain: в реверсе на карточках — затраченное время. */
    val isReverseMode: Boolean
) {
    companion object {
        fun gridColumnsFor(playerCount: Int): Int = ChessClockGridTurns.gridColumnsFor(playerCount)
    }
}

data class SettingsUiState(
    val isOpen: Boolean,
    val durationMinutesInput: String,
    val playerCount: Int,
    /**
     * Имена по **месту** (0..[com.multiplechessclok.app.domain.ChessClockRules.MAX_PLAYER_COUNT]-1);
     * в UI показываются только первые [playerCount] слотов.
     */
    val playerNames: List<String>,
    /** Палитра по каждому месту; размер как у [playerNames]. */
    val playerPaletteIndices: List<Int>,
    /** Режим «реверс»: считать затраченное время от 00:00. */
    val reverseMode: Boolean
)
