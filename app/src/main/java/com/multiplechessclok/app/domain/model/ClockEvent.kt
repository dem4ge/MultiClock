package com.multiplechessclok.app.domain.model

sealed interface ChessClockEvent {
    data class TapPlayer(val playerId: PlayerId) : ChessClockEvent
    data object Pause : ChessClockEvent
    data class Reorder(val fromIndex: Int, val toIndex: Int) : ChessClockEvent
    data class ReorderByPlayerIds(val fromPlayerId: PlayerId, val toPlayerId: PlayerId) : ChessClockEvent
    data class ApplySettings(
        val newPlayerCount: Int,
        val newDefaultDurationMs: Long,
        val namesByPaletteIndex: Map<Int, String?>,
        /** Индекс цвета для каждого игрока по порядку; пустой список — не менять у существующих, для новых — по умолчанию. */
        val paletteIndicesByPlayerIndex: List<Int> = emptyList(),
        val reverseMode: Boolean = false
    ) : ChessClockEvent
    data object Reset : ChessClockEvent
    data object Tick : ChessClockEvent
}
