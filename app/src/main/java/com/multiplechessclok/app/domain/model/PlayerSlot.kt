package com.multiplechessclok.app.domain.model

data class PlayerSlot(
    val id: PlayerId,
    val displayName: String?,
    val remainingMs: Long,
    val paletteIndex: Int
)
