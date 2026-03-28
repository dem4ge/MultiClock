package com.multiplechessclok.app.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.multiplechessclok.app.domain.ChessClockRules

object PlayerColors {
    /** Базовые приглушённые оттенки; перед использованием слегка осветляются ([brightenValue]). */
    private val basePalette: List<Color> = listOf(
        Color(0xFFB5736A),
        Color(0xFF6B8CAA),
        Color(0xFF7A9B7A),
        Color(0xFFC4B56A),
        Color(0xFF9B7F63),
        Color(0xFFC4946A),
        Color(0xFF8A7A9A),
        Color(0xFF5A8A8A),
        Color(0xFF8B8A82)
    )

    /** Доля прибавки к компоненте V в HSV (≈ +15% к «яркости» на шкале 0…1). */
    private const val BRIGHTEN_VALUE_DELTA = 0.15f

    /** Цвета карточек; порядок совпадает с индексами в домене ([ChessClockRules.PLAYER_PALETTE_SIZE]). */
    val palette: List<Color> = basePalette.map { brightenValue(it, BRIGHTEN_VALUE_DELTA) }

    init {
        require(palette.size == ChessClockRules.PLAYER_PALETTE_SIZE) {
            "PlayerColors.palette must match PLAYER_PALETTE_SIZE"
        }
    }

    fun at(index: Int): Color = palette[index.coerceIn(0, palette.lastIndex)]
}

private fun brightenValue(color: Color, deltaV: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    hsv[2] = (hsv[2] + deltaV).coerceIn(0f, 1f)
    return Color(AndroidColor.HSVToColor(hsv))
}
