package com.multiplechessclok.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.multiplechessclok.app.data.GameConfigurationDefaults
import com.multiplechessclok.app.data.InMemoryPlayerSlotMemory
import com.multiplechessclok.app.data.PlayerSlotMemory
import com.multiplechessclok.app.domain.ChessClockReducer
import com.multiplechessclok.app.domain.ChessClockTimerEngine
import com.multiplechessclok.app.domain.ChessClockRules
import com.multiplechessclok.app.domain.model.ChessClockDomainState
import com.multiplechessclok.app.domain.model.ChessClockEvent
import com.multiplechessclok.app.domain.model.PlayerId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChessClockViewModel(
    private val slotMemory: PlayerSlotMemory = InMemoryPlayerSlotMemory()
) : ViewModel() {

    override fun onCleared() {
        cancelTickerForTests()
        super.onCleared()
    }

    /** Останавливает корутину тика; для unit-тестов, где реверс-режим не даёт «обнулить» таймер. */
    internal fun cancelTickerForTests() {
        tickerJob?.cancel()
        tickerJob = null
    }

    /** True if we applied [ChessClockEvent.Pause] when opening settings; cleared when the sheet closes. */
    private var pausedForSettingsSheet: Boolean = false

    private var tickerJob: Job? = null

    private var domainState: ChessClockDomainState

    private val _uiState: MutableStateFlow<ChessClockUiState>

    init {
        val (storedNames, storedPalettes) = slotMemory.readNamesAndPalettes()
        val names = storedNames.toMutableList()
        val palettes = storedPalettes.toMutableList()
        ensureSlotLists(names, palettes)

        val count = slotMemory.readSavedPlayerCount()
        val savedReverse = slotMemory.readReverseMode()
        domainState = ChessClockReducer.initialState(
            playerCount = count,
            defaultDurationMs = GameConfigurationDefaults.initialDurationMs,
            isReverseMode = savedReverse
        ).let { state ->
            state.copy(
                players = state.players.mapIndexed { i, p ->
                    p.copy(
                        displayName = names[i].ifBlank { null },
                        paletteIndex = palettes[i]
                    )
                }
            )
        }

        val initialSettings = settingsSnapshot(domainState, isOpen = false, names, palettes)
        _uiState = MutableStateFlow(domainToUi(domainState, initialSettings))
        restartTickerIfNeeded()
    }

    val uiState: StateFlow<ChessClockUiState> = _uiState.asStateFlow()

    fun onTapPlayer(playerId: String) {
        applyDomain(ChessClockEvent.TapPlayer(PlayerId(playerId)))
    }

    fun onPause() {
        applyDomain(ChessClockEvent.Pause)
    }

    /** Останавливает тик активного игрока (если часы шли), без смены состояния иначе. */
    fun pauseActiveTimerIfRunning() {
        if (domainState.activePlayerId != null && !domainState.isPaused) {
            applyDomain(ChessClockEvent.Pause)
        }
    }

    fun onReorder(fromIndex: Int, toIndex: Int) {
        applyDomain(ChessClockEvent.Reorder(fromIndex = fromIndex, toIndex = toIndex))
    }

    fun onReorderByPlayerIds(fromPlayerId: String, toPlayerId: String) {
        if (fromPlayerId == toPlayerId) return
        applyDomain(
            ChessClockEvent.ReorderByPlayerIds(
                fromPlayerId = PlayerId(fromPlayerId),
                toPlayerId = PlayerId(toPlayerId)
            )
        )
    }

    fun openSettings() {
        pausedForSettingsSheet = false
        if (domainState.activePlayerId != null && !domainState.isPaused) {
            applyDomain(ChessClockEvent.Pause)
            pausedForSettingsSheet = true
        }
        _uiState.update { domainToUi(domainState, settingsFromDomain(domainState, isOpen = true)) }
    }

    fun dismissSettings() {
        resumeAfterSettingsSheetIfNeeded()
        _uiState.update { prev ->
            prev.copy(settings = prev.settings.copy(isOpen = false))
        }
    }

    fun updateSettingsDurationMinutes(raw: String) {
        _uiState.update { prev ->
            val digits = raw.filter { it.isDigit() }.take(MAX_INPUT_DIGITS)
            prev.copy(settings = prev.settings.copy(durationMinutesInput = digits))
        }
    }

    fun updateSettingsPlayerCount(count: Int) {
        val c = count.coerceIn(ChessClockRules.MIN_PLAYER_COUNT, ChessClockRules.MAX_PLAYER_COUNT)
        _uiState.update { prev ->
            prev.copy(settings = prev.settings.copy(playerCount = c))
        }
    }

    fun updateSettingsPlayerName(index: Int, value: String) {
        _uiState.update { prev ->
            if (index !in 0 until ChessClockRules.MAX_PLAYER_COUNT) return@update prev
            val names = prev.settings.playerNames.toMutableList()
            names[index] = value.take(MAX_NAME_LENGTH)
            prev.copy(settings = prev.settings.copy(playerNames = names))
        }
        persistSettingsSlots(_uiState.value.settings)
    }

    fun updateSettingsPlayerPalette(index: Int, paletteIndex: Int) {
        val idx = paletteIndex.coerceIn(0, ChessClockRules.PLAYER_PALETTE_LAST_INDEX)
        _uiState.update { prev ->
            if (index !in 0 until ChessClockRules.MAX_PLAYER_COUNT) return@update prev
            val palettes = prev.settings.playerPaletteIndices.toMutableList()
            palettes[index] = idx
            prev.copy(settings = prev.settings.copy(playerPaletteIndices = palettes))
        }
        persistSettingsSlots(_uiState.value.settings)
    }

    fun updateSettingsReverseMode(enabled: Boolean) {
        _uiState.update { prev ->
            prev.copy(settings = prev.settings.copy(reverseMode = enabled))
        }
    }

    fun applySettingsFromSheet() {
        val sheet = _uiState.value.settings
        val minutes = sheet.durationMinutesInput.toLongOrNull()
            ?.coerceAtLeast(MIN_SETTINGS_MINUTES)
            ?: (domainState.defaultDurationMs / MS_PER_MINUTE)
        val durationMs = (minutes * MS_PER_MINUTE).coerceAtLeast(ChessClockRules.MIN_DURATION_MS)
        val n = sheet.playerCount
        val namesMap = (0 until n).associateWith { i ->
            sheet.playerNames[i].ifBlank { null }
        }
        val palettes = sheet.playerPaletteIndices.take(n)
        applyDomain(
            ChessClockEvent.ApplySettings(
                newPlayerCount = n,
                newDefaultDurationMs = durationMs,
                namesByPaletteIndex = namesMap,
                paletteIndicesByPlayerIndex = palettes,
                reverseMode = sheet.reverseMode
            )
        )
        dismissSettings()
    }

    fun resetFromSettings() {
        applyDomain(ChessClockEvent.Reset)
    }

    private fun resumeAfterSettingsSheetIfNeeded() {
        if (!pausedForSettingsSheet) return
        pausedForSettingsSheet = false
        val activeId = domainState.activePlayerId ?: return
        if (!domainState.isPaused) return
        applyDomain(ChessClockEvent.TapPlayer(activeId))
    }

    private fun applyDomain(event: ChessClockEvent) {
        domainState = ChessClockReducer.reduce(domainState, event)
        val preservedSettings = _uiState.value.settings
        _uiState.value = domainToUi(
            domainState,
            preservedSettings.copy(
                isOpen = preservedSettings.isOpen,
                durationMinutesInput = (domainState.defaultDurationMs / MS_PER_MINUTE).toString(),
                playerCount = domainState.players.size,
                playerNames = mergeSlotNamesFromDomain(preservedSettings.playerNames, domainState),
                playerPaletteIndices = mergeSlotPalettesFromDomain(
                    preservedSettings.playerPaletteIndices,
                    domainState
                ),
                reverseMode = domainState.isReverseMode
            )
        )
        when (event) {
            is ChessClockEvent.ApplySettings,
            is ChessClockEvent.Reorder,
            is ChessClockEvent.ReorderByPlayerIds -> persistSettingsSlots(_uiState.value.settings)
            else -> Unit
        }
        restartTickerIfNeeded()
    }

    private fun mergeSlotNamesFromDomain(
        previous: List<String>,
        domain: ChessClockDomainState
    ): List<String> {
        val out = previous.toMutableList()
        while (out.size < ChessClockRules.MAX_PLAYER_COUNT) out.add("")
        while (out.size > ChessClockRules.MAX_PLAYER_COUNT) out.removeLast()
        for (i in domain.players.indices) {
            out[i] = domain.players[i].displayName.orEmpty()
        }
        return out
    }

    private fun mergeSlotPalettesFromDomain(
        previous: List<Int>,
        domain: ChessClockDomainState
    ): List<Int> {
        val out = previous.toMutableList()
        while (out.size < ChessClockRules.MAX_PLAYER_COUNT) {
            val i = out.size
            out.add(i % ChessClockRules.PLAYER_PALETTE_SIZE)
        }
        while (out.size > ChessClockRules.MAX_PLAYER_COUNT) out.removeLast()
        for (i in domain.players.indices) {
            out[i] = domain.players[i].paletteIndex
        }
        return out
    }

    private fun restartTickerIfNeeded() {
        tickerJob?.cancel()
        val active = domainState.activePlayerId
        val shouldRun = active != null && !domainState.isPaused
        if (!shouldRun) {
            tickerJob = null
            return
        }
        tickerJob = viewModelScope.launch {
            while (isActive && domainState.activePlayerId != null && !domainState.isPaused) {
                delay(ChessClockRules.TIMER_TICK_INTERVAL_MS)
                if (domainState.activePlayerId == null || domainState.isPaused) break
                val activePlayer = domainState.players.firstOrNull { it.id == domainState.activePlayerId }
                    ?: break
                if (!domainState.isReverseMode && activePlayer.remainingMs <= ChessClockRules.MIN_REMAINING_MS) break
                domainState = ChessClockTimerEngine.applyTick(domainState)
                _uiState.update { prev ->
                    domainToUi(domainState, prev.settings)
                }
            }
        }
    }

    private fun domainToUi(
        domain: ChessClockDomainState,
        settings: SettingsUiState
    ): ChessClockUiState =
        ChessClockUiState(
            players = domain.players.map { p ->
                PlayerCardUiState(
                    id = p.id.value,
                    displayName = p.displayName,
                    remainingMs = p.remainingMs,
                    paletteIndex = p.paletteIndex,
                    isActive = domain.activePlayerId == p.id
                )
            },
            defaultDurationMs = domain.defaultDurationMs,
            playerCount = domain.players.size,
            settings = settings,
            gridColumns = ChessClockUiState.gridColumnsFor(domain.players.size),
            isReverseMode = domain.isReverseMode
        )

    private fun settingsFromDomain(domain: ChessClockDomainState, isOpen: Boolean): SettingsUiState {
        val prev = _uiState.value.settings
        return settingsSnapshot(domain, isOpen, prev.playerNames.toMutableList(), prev.playerPaletteIndices.toMutableList())
    }

    private fun settingsSnapshot(
        domain: ChessClockDomainState,
        isOpen: Boolean,
        names: MutableList<String>,
        palettes: MutableList<Int>
    ): SettingsUiState {
        ensureSlotLists(names, palettes)
        for (i in domain.players.indices) {
            names[i] = domain.players[i].displayName.orEmpty()
            palettes[i] = domain.players[i].paletteIndex
        }
        return SettingsUiState(
            isOpen = isOpen,
            durationMinutesInput = (domain.defaultDurationMs / MS_PER_MINUTE).toString(),
            playerCount = domain.players.size,
            playerNames = names,
            playerPaletteIndices = palettes,
            reverseMode = domain.isReverseMode
        )
    }

    private fun persistSettingsSlots(settings: SettingsUiState) {
        slotMemory.persist(
            names = settings.playerNames,
            palettes = settings.playerPaletteIndices,
            playerCount = domainState.players.size,
            reverseMode = domainState.isReverseMode
        )
    }

    private fun ensureSlotLists(names: MutableList<String>, palettes: MutableList<Int>) {
        while (names.size < ChessClockRules.MAX_PLAYER_COUNT) {
            names.add("")
        }
        while (palettes.size < ChessClockRules.MAX_PLAYER_COUNT) {
            val i = palettes.size
            palettes.add(i % ChessClockRules.PLAYER_PALETTE_SIZE)
        }
        while (names.size > ChessClockRules.MAX_PLAYER_COUNT) {
            names.removeLast()
        }
        while (palettes.size > ChessClockRules.MAX_PLAYER_COUNT) {
            palettes.removeLast()
        }
    }

    companion object {
        private const val MS_PER_MINUTE = 60_000L
        private const val MIN_SETTINGS_MINUTES = 1L
        private const val MAX_INPUT_DIGITS = 5
        private const val MAX_NAME_LENGTH = 24
    }
}
