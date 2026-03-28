package com.multiplechessclok.app.data

import android.content.Context
import com.multiplechessclok.app.domain.ChessClockRules

/**
 * Память по **индексу места** (0..[ChessClockRules.MAX_PLAYER_COUNT]-1): имя и палитра.
 * Слоты выше текущего числа игроков сохраняются и подставляются при следующем увеличении числа.
 */
interface PlayerSlotMemory {
    fun readNamesAndPalettes(): Pair<List<String>, List<Int>>

    /** Сохранённое число игроков для следующего запуска. */
    fun readSavedPlayerCount(): Int

    /** Режим «реверс»: накопленное время от 00:00 вместо обратного отсчёта. */
    fun readReverseMode(): Boolean

    fun persist(names: List<String>, palettes: List<Int>, playerCount: Int, reverseMode: Boolean)
}

/** Для unit-тестов и превью: без диска. */
class InMemoryPlayerSlotMemory(
    private var names: MutableList<String> =
        MutableList(ChessClockRules.MAX_PLAYER_COUNT) { "" },
    private var palettes: MutableList<Int> =
        MutableList(ChessClockRules.MAX_PLAYER_COUNT) { it % ChessClockRules.PLAYER_PALETTE_SIZE },
    private var count: Int = GameConfigurationDefaults.initialPlayerCount,
    private var reverseMode: Boolean = false
) : PlayerSlotMemory {

    override fun readNamesAndPalettes(): Pair<List<String>, List<Int>> =
        names.toList() to palettes.toList()

    override fun readSavedPlayerCount(): Int = count

    override fun readReverseMode(): Boolean = reverseMode

    override fun persist(names: List<String>, palettes: List<Int>, playerCount: Int, reverseMode: Boolean) {
        this.names = MutableList(ChessClockRules.MAX_PLAYER_COUNT) { i ->
            names.getOrElse(i) { "" }
        }
        this.palettes = MutableList(ChessClockRules.MAX_PLAYER_COUNT) { i ->
            palettes.getOrElse(i) { i % ChessClockRules.PLAYER_PALETTE_SIZE }
        }
        this.count = playerCount
        this.reverseMode = reverseMode
    }
}

class SharedPreferencesPlayerSlotMemory(context: Context) : PlayerSlotMemory {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun readNamesAndPalettes(): Pair<List<String>, List<Int>> {
        val names = List(ChessClockRules.MAX_PLAYER_COUNT) { i ->
            prefs.getString(keyName(i), "") ?: ""
        }
        val palettes = List(ChessClockRules.MAX_PLAYER_COUNT) { i ->
            prefs.getInt(keyPalette(i), i % ChessClockRules.PLAYER_PALETTE_SIZE)
        }
        return names to palettes
    }

    override fun readSavedPlayerCount(): Int {
        val def = GameConfigurationDefaults.initialPlayerCount
        return prefs.getInt(KEY_PLAYER_COUNT, def).coerceIn(
            ChessClockRules.MIN_PLAYER_COUNT,
            ChessClockRules.MAX_PLAYER_COUNT
        )
    }

    override fun readReverseMode(): Boolean = prefs.getBoolean(KEY_REVERSE_MODE, false)

    override fun persist(names: List<String>, palettes: List<Int>, playerCount: Int, reverseMode: Boolean) {
        prefs.edit().apply {
            for (i in 0 until ChessClockRules.MAX_PLAYER_COUNT) {
                putString(keyName(i), names.getOrElse(i) { "" })
                putInt(keyPalette(i), palettes.getOrElse(i) { i % ChessClockRules.PLAYER_PALETTE_SIZE })
            }
            putInt(
                KEY_PLAYER_COUNT,
                playerCount.coerceIn(ChessClockRules.MIN_PLAYER_COUNT, ChessClockRules.MAX_PLAYER_COUNT)
            )
            putBoolean(KEY_REVERSE_MODE, reverseMode)
            apply()
        }
    }

    private companion object {
        const val PREFS_NAME = "player_slot_memory"
        const val KEY_PLAYER_COUNT = "player_count"
        const val KEY_REVERSE_MODE = "reverse_mode"

        fun keyName(i: Int) = "name_$i"
        fun keyPalette(i: Int) = "palette_$i"
    }
}
