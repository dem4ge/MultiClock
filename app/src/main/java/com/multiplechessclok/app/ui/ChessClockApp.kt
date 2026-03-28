package com.multiplechessclok.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.multiplechessclok.app.data.SharedPreferencesPlayerSlotMemory
import com.multiplechessclok.app.presentation.ChessClockViewModel
import com.multiplechessclok.app.ui.components.ChessClockBottomBar
import com.multiplechessclok.app.ui.components.LandscapeAlignedPlayerGridHost
import com.multiplechessclok.app.ui.theme.ChessClockTheme

/** Как у [com.multiplechessclok.app.ui.components.PlayerCard]. */
private val ResetDialogActionCornerRadius = 18.dp
private val ResetDialogActionMinHeight = 54.dp
private val ResetDialogActionPaddingHorizontal = 12.dp
private val ResetDialogActionPaddingVertical = 14.dp
private val ResetDialogActionsSpacing = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessClockApp() {
    val context = LocalContext.current.applicationContext
    val viewModel: ChessClockViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChessClockViewModel(SharedPreferencesPlayerSlotMemory(context)) as T
            }
        }
    )
    ChessClockTheme {
        val ui by viewModel.uiState.collectAsState()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var showResetConfirm by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101418))
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            LandscapeAlignedPlayerGridHost(
                players = ui.players,
                columns = ui.gridColumns,
                onPlayerClick = viewModel::onTapPlayer,
                onReorderByPlayerIds = viewModel::onReorderByPlayerIds,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            ChessClockBottomBar(
                onPause = viewModel::onPause,
                onOpenSettings = viewModel::openSettings,
                onResetTimer = {
                    viewModel.pauseActiveTimerIfRunning()
                    showResetConfirm = true
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                title = {
                    Text(
                        text = "Reset timer?",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ResetDialogActionsSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ResetDialogActionButton(
                            text = "Cancel",
                            onClick = { showResetConfirm = false },
                            modifier = Modifier.weight(1f)
                        )
                        ResetDialogActionButton(
                            text = "Reset",
                            onClick = {
                                viewModel.resetFromSettings()
                                showResetConfirm = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            )
        }

        if (ui.settings.isOpen) {
            ModalBottomSheet(
                onDismissRequest = viewModel::dismissSettings,
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                SettingsContent(
                    settings = ui.settings,
                    onDurationChange = viewModel::updateSettingsDurationMinutes,
                    onPlayerCountChange = viewModel::updateSettingsPlayerCount,
                    onPlayerNameChange = viewModel::updateSettingsPlayerName,
                    onPlayerPaletteChange = viewModel::updateSettingsPlayerPalette,
                    onReverseModeChange = viewModel::updateSettingsReverseMode,
                    onApply = viewModel::applySettingsFromSheet,
                    onReset = viewModel::resetFromSettings,
                    onDismiss = viewModel::dismissSettings
                )
            }
        }
    }
}

@Composable
private fun ResetDialogActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val fill = lerp(scheme.surface, scheme.background, 0.38f)
    val stroke = scheme.outline.copy(alpha = 0.45f)
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ResetDialogActionMinHeight),
        shape = RoundedCornerShape(ResetDialogActionCornerRadius),
        color = fill,
        border = BorderStroke(1.dp, stroke)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ResetDialogActionPaddingHorizontal,
                    vertical = ResetDialogActionPaddingVertical
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp
                ),
                color = scheme.onSurface
            )
        }
    }
}
