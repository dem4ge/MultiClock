package com.multiplechessclok.app.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.multiplechessclok.app.data.InMemoryPlayerSlotMemory
import com.multiplechessclok.app.domain.ChessClockRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChessClockViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_allInactiveFullHour() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val state = vm.uiState.value
        assertTrue(state.players.all { !it.isActive })
        assertEquals(ChessClockRules.DEFAULT_DURATION_MS, state.players[0].remainingMs)
    }

    @Test
    fun firstTap_startsActivePlayer() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val id = vm.uiState.value.players[0].id
        vm.onTapPlayer(id)
        assertEquals(id, vm.uiState.value.players.first { it.isActive }.id)
    }

    @Test
    fun activeTimerCountsDown() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val id = vm.uiState.value.players[0].id
        vm.onTapPlayer(id)
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS)
        dispatcher.scheduler.runCurrent()
        assertEquals(
            ChessClockRules.DEFAULT_DURATION_MS - ChessClockRules.TIMER_TICK_INTERVAL_MS,
            vm.uiState.value.players.first { it.id == id }.remainingMs
        )
    }

    @Test
    fun reverseMode_activeTimerCountsUp() = runTest(dispatcher) {
        val mem = InMemoryPlayerSlotMemory(reverseMode = true)
        val vm = ChessClockViewModel(mem)
        try {
            assertEquals(true, vm.uiState.value.isReverseMode)
            val id = vm.uiState.value.players[0].id
            assertEquals(0L, vm.uiState.value.players.first { it.id == id }.remainingMs)
            vm.onTapPlayer(id)
            advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS)
            dispatcher.scheduler.runCurrent()
            assertEquals(
                ChessClockRules.TIMER_TICK_INTERVAL_MS,
                vm.uiState.value.players.first { it.id == id }.remainingMs
            )
        } finally {
            vm.cancelTickerForTests()
        }
    }

    @Test
    fun pause_freezesCountdown() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val id = vm.uiState.value.players[0].id
        vm.onTapPlayer(id)
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS)
        dispatcher.scheduler.runCurrent()
        vm.onPause()
        val frozen = vm.uiState.value.players.first { it.id == id }.remainingMs
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS * 5)
        dispatcher.scheduler.runCurrent()
        assertEquals(frozen, vm.uiState.value.players.first { it.id == id }.remainingMs)
    }

    @Test
    fun resumeContinuesAfterPause() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val id = vm.uiState.value.players[0].id
        vm.onTapPlayer(id)
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS)
        dispatcher.scheduler.runCurrent()
        vm.onPause()
        vm.onTapPlayer(id)
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS)
        dispatcher.scheduler.runCurrent()
        assertEquals(
            ChessClockRules.DEFAULT_DURATION_MS - ChessClockRules.TIMER_TICK_INTERVAL_MS * 2,
            vm.uiState.value.players.first { it.id == id }.remainingMs
        )
    }

    @Test
    fun secondPauseResumesLikeTapActive() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val id = vm.uiState.value.players[0].id
        vm.onTapPlayer(id)
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS)
        dispatcher.scheduler.runCurrent()
        vm.onPause()
        vm.onPause()
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS)
        dispatcher.scheduler.runCurrent()
        assertEquals(
            ChessClockRules.DEFAULT_DURATION_MS - ChessClockRules.TIMER_TICK_INTERVAL_MS * 2,
            vm.uiState.value.players.first { it.id == id }.remainingMs
        )
    }

    @Test
    fun reorder_updatesOrder() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val before = vm.uiState.value.players.map { it.id }
        vm.onReorder(0, 2)
        val after = vm.uiState.value.players.map { it.id }
        assertEquals(before[1], after[0])
        assertEquals(before[2], after[1])
        assertEquals(before[0], after[2])
        assertEquals(before[3], after[3])
    }

    @Test
    fun reorderByPlayerIds_updatesOrder_and_preserves_active() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val ids = vm.uiState.value.players.map { it.id }
        vm.onTapPlayer(ids[0])
        assertEquals(ids[0], vm.uiState.value.players.first { it.isActive }.id)
        vm.onReorderByPlayerIds(ids[0], ids[2])
        val after = vm.uiState.value.players.map { it.id }
        assertEquals(ids[1], after[0])
        assertEquals(ids[2], after[1])
        assertEquals(ids[0], after[2])
        assertEquals(ids[0], vm.uiState.value.players.first { it.isActive }.id)
    }

    @Test
    fun applySettings_clampsCountThroughDomain() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        vm.openSettings()
        vm.updateSettingsPlayerCount(99)
        vm.applySettingsFromSheet()
        assertEquals(ChessClockRules.MAX_PLAYER_COUNT, vm.uiState.value.players.size)
    }

    @Test
    fun playerSlotMemory_keepsNameForPlaceWhenCountDropsThenRises() = runTest(dispatcher) {
        val mem = InMemoryPlayerSlotMemory()
        val vm = ChessClockViewModel(mem)
        vm.openSettings()
        vm.updateSettingsPlayerName(3, "Alice")
        vm.updateSettingsPlayerCount(2)
        assertEquals(2, vm.uiState.value.settings.playerCount)
        assertEquals("Alice", vm.uiState.value.settings.playerNames[3])
        vm.updateSettingsPlayerCount(4)
        assertEquals("Alice", vm.uiState.value.settings.playerNames[3])
        vm.applySettingsFromSheet()
        assertEquals("Alice", vm.uiState.value.players[3].displayName)
    }

    @Test
    fun openSettings_pausesActiveTimer() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val id = vm.uiState.value.players[0].id
        vm.onTapPlayer(id)
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS)
        dispatcher.scheduler.runCurrent()
        val afterOneTick = vm.uiState.value.players.first { it.id == id }.remainingMs
        vm.openSettings()
        assertTrue(vm.uiState.value.settings.isOpen)
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS * 5)
        dispatcher.scheduler.runCurrent()
        assertEquals(afterOneTick, vm.uiState.value.players.first { it.id == id }.remainingMs)
    }

    @Test
    fun dismissSettings_resumesAfterAutoPauseFromOpenSettings() = runTest(dispatcher) {
        val vm = ChessClockViewModel()
        val id = vm.uiState.value.players[0].id
        vm.onTapPlayer(id)
        vm.openSettings()
        assertTrue(vm.uiState.value.settings.isOpen)
        vm.dismissSettings()
        assertTrue(!vm.uiState.value.settings.isOpen)
        advanceTimeBy(ChessClockRules.TIMER_TICK_INTERVAL_MS)
        dispatcher.scheduler.runCurrent()
        assertEquals(
            ChessClockRules.DEFAULT_DURATION_MS - ChessClockRules.TIMER_TICK_INTERVAL_MS,
            vm.uiState.value.players.first { it.id == id }.remainingMs
        )
    }
}
