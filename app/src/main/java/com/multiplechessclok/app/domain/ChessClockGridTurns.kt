package com.multiplechessclok.app.domain

/**
 * Turn order matches [gridColumnsFor] + layout in [com.multiplechessclok.app.ui.components.PlayerGrid]:
 * indices advance along the outer perimeter clockwise (not along plain row-major when that would break the ring).
 * E.g. 2×2: 0→1→3→2; five players use a 2+2+1 layout so the perimeter is 0→1→2→3→4.
 */
object ChessClockGridTurns {

    fun gridColumnsFor(playerCount: Int): Int =
        minOf(2, playerCount).coerceAtLeast(ChessClockRules.MIN_PLAYER_COUNT)

    /**
     * Next index in clockwise turn order for [playerCount] players laid out in a grid with [gridColumnsFor] columns.
     */
    fun nextClockwiseIndex(currentIndex: Int, playerCount: Int): Int {
        require(playerCount >= ChessClockRules.MIN_PLAYER_COUNT)
        require(currentIndex in 0 until playerCount)
        val order = clockwiseOrder(playerCount)
        val pos = order.indexOf(currentIndex)
        check(pos >= 0) { "index $currentIndex not in clockwise order for n=$playerCount" }
        return order[(pos + 1) % order.size]
    }

    private fun clockwiseOrder(n: Int): IntArray =
        when (n) {
            2 -> intArrayOf(0, 1)
            3 -> intArrayOf(0, 1, 2)
            4 -> intArrayOf(0, 1, 3, 2)
            5 -> intArrayOf(0, 1, 2, 3, 4)
            6 -> intArrayOf(0, 1, 3, 5, 4, 2)
            else -> fallbackRowMajor(n)
        }

    private fun fallbackRowMajor(n: Int): IntArray = IntArray(n) { it }
}
