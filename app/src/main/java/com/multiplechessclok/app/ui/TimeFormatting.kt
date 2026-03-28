package com.multiplechessclok.app.ui

import kotlin.math.max

object TimeFormatting {
    private const val MS_PER_SECOND = 1_000L
    private const val MS_PER_HOUR = 3_600_000L
    private const val SECONDS_PER_MINUTE = 60L
    private const val SECONDS_PER_HOUR = 3_600L

    /**
     * [mm:ss] if under one hour, otherwise [hh:mm:ss].
     */
    fun formatHhMmSs(remainingMs: Long): String {
        val clamped = max(remainingMs, 0L)
        val totalSeconds = clamped / MS_PER_SECOND
        if (clamped < MS_PER_HOUR) {
            val minutes = totalSeconds / SECONDS_PER_MINUTE
            val seconds = totalSeconds % SECONDS_PER_MINUTE
            return "%02d:%02d".format(minutes, seconds)
        }
        val hours = totalSeconds / SECONDS_PER_HOUR
        val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}
