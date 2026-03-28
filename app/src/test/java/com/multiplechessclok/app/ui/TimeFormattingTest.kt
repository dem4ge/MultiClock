package com.multiplechessclok.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormattingTest {

    @Test
    fun underOneHour_usesMinutesAndSecondsOnly() {
        assertEquals("01:05", TimeFormatting.formatHhMmSs(65_000L))
        assertEquals("59:59", TimeFormatting.formatHhMmSs(3_599_000L))
        assertEquals("00:00", TimeFormatting.formatHhMmSs(0L))
    }

    @Test
    fun oneHourOrMore_usesHoursMinutesAndSeconds() {
        assertEquals("01:00:00", TimeFormatting.formatHhMmSs(3_600_000L))
        assertEquals("02:30:45", TimeFormatting.formatHhMmSs((2 * 3_600 + 30 * 60 + 45) * 1_000L))
    }

    @Test
    fun clampsNegativeToZeroDisplay() {
        assertEquals("00:00", TimeFormatting.formatHhMmSs(-5_000L))
    }
}
