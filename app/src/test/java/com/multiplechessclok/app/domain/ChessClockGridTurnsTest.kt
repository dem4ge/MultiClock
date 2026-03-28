package com.multiplechessclok.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ChessClockGridTurnsTest {

    @Test
    fun fourPlayers_clockwiseOrder_isPerimeterNotRows() {
        assertEquals(1, ChessClockGridTurns.nextClockwiseIndex(0, 4))
        assertEquals(3, ChessClockGridTurns.nextClockwiseIndex(1, 4))
        assertEquals(2, ChessClockGridTurns.nextClockwiseIndex(3, 4))
        assertEquals(0, ChessClockGridTurns.nextClockwiseIndex(2, 4))
    }

    @Test
    fun fivePlayers_clockwiseOrder_followsPerimeter() {
        assertEquals(1, ChessClockGridTurns.nextClockwiseIndex(0, 5))
        assertEquals(2, ChessClockGridTurns.nextClockwiseIndex(1, 5))
        assertEquals(3, ChessClockGridTurns.nextClockwiseIndex(2, 5))
        assertEquals(4, ChessClockGridTurns.nextClockwiseIndex(3, 5))
        assertEquals(0, ChessClockGridTurns.nextClockwiseIndex(4, 5))
    }

    @Test
    fun sixPlayers_clockwiseOrder_matchesSpiralPerimeter() {
        assertEquals(1, ChessClockGridTurns.nextClockwiseIndex(0, 6))
        assertEquals(3, ChessClockGridTurns.nextClockwiseIndex(1, 6))
        assertEquals(5, ChessClockGridTurns.nextClockwiseIndex(3, 6))
        assertEquals(4, ChessClockGridTurns.nextClockwiseIndex(5, 6))
        assertEquals(2, ChessClockGridTurns.nextClockwiseIndex(4, 6))
        assertEquals(0, ChessClockGridTurns.nextClockwiseIndex(2, 6))
    }
}
