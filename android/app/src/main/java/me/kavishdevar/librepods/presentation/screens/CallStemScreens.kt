/*
    LibrePods - AirPods liberated from Apple’s ecosystem
    Copyright (C) 2025 LibrePods contributors

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

@file:OptIn(ExperimentalEncodingApi::class)

package me.kavishdevar.librepods.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import me.kavishdevar.librepods.R
import me.kavishdevar.librepods.bluetooth.AACPManager.Companion.StemPressType
import me.kavishdevar.librepods.data.CallStemAction
import me.kavishdevar.librepods.data.StemPressPrefs
import me.kavishdevar.librepods.presentation.components.StyledList
import me.kavishdevar.librepods.presentation.components.StyledListItem
import me.kavishdevar.librepods.presentation.components.callStemActionLabel
import me.kavishdevar.librepods.presentation.components.stemBudTitle
import me.kavishdevar.librepods.presentation.components.stemPressTypeTitle
import me.kavishdevar.librepods.presentation.theme.DesignSystem
import me.kavishdevar.librepods.presentation.theme.LocalDesignSystem
import me.kavishdevar.librepods.presentation.viewmodel.AirPodsViewModel
import kotlin.io.encoding.ExperimentalEncodingApi

private val CALL_ACTION_ORDER = listOf(
    CallStemAction.BUILT_IN,
    CallStemAction.END_CALL,
    CallStemAction.MUTE,
    CallStemAction.LAUNCH_SHORTCUT,
)

/**
 * Call Controls -> Customize: for each stem, what press once and press twice do during an
 * active call (while ringing the AirPods always keep their built-in behaviour).
 * Each row leads to [CallStemPressActionScreen].
 */
@ExperimentalHazeMaterialsApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallCustomScreen(
    viewModel: AirPodsViewModel,
    navigateToCallStemPress: (bud: String, type: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val m3eEnabled = LocalDesignSystem.current == DesignSystem.Material
    val topPadding = if (m3eEnabled) 0.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 84.dp
    val bottomPadding = if (m3eEnabled) 0.dp else WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .verticalScroll(scrollState)
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(topPadding))

        for (bud in StemPressPrefs.BUDS) {
            StyledList(
                title = stringResource(stemBudTitle(bud)),
                description = if (bud == StemPressPrefs.LEFT) stringResource(R.string.call_custom_screen_description) else null
            ) {
                for (type in StemPressPrefs.CALL_TYPES) {
                    val typeKey = StemPressPrefs.typeKey(type)
                    val stateKey = StemPressPrefs.callStateKey(bud, typeKey)
                    val action = state.callStemActions[stateKey] ?: CallStemAction.BUILT_IN
                    StyledListItem(
                        name = stringResource(stemPressTypeTitle(typeKey)),
                        description = callStemActionLabel(action, state.callStemShortcutNames[stateKey]),
                        onClick = { navigateToCallStemPress(bud, typeKey) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.height(bottomPadding))
    }
}

/**
 * Picks what one bud's press once / press twice does during calls.
 * typeKey = "single" | "double".
 */
@ExperimentalHazeMaterialsApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallStemPressActionScreen(
    viewModel: AirPodsViewModel,
    bud: String,
    typeKey: String
) {
    val state by viewModel.uiState.collectAsState()

    val type = StemPressPrefs.typeFromKey(typeKey) ?: StemPressType.SINGLE_PRESS
    val stateKey = StemPressPrefs.callStateKey(bud, typeKey)
    val currentAction = state.callStemActions[stateKey] ?: CallStemAction.BUILT_IN
    val shortcutName = state.callStemShortcutNames[stateKey]

    val pickShortcut = rememberShortcutPicker { intentUri, name ->
        viewModel.setCallStemShortcut(bud, type, intentUri, name)
        viewModel.setCallStemAction(bud, type, CallStemAction.LAUNCH_SHORTCUT)
    }

    val m3eEnabled = LocalDesignSystem.current == DesignSystem.Material
    val topPadding = if (m3eEnabled) 0.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 84.dp
    val bottomPadding = if (m3eEnabled) 0.dp else WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .verticalScroll(scrollState)
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(topPadding))

        StyledList {
            for (action in CALL_ACTION_ORDER) {
                val description = when (action) {
                    CallStemAction.BUILT_IN -> stringResource(R.string.call_action_built_in_description)
                    CallStemAction.END_CALL -> stringResource(R.string.call_action_end_description)
                    CallStemAction.MUTE -> stringResource(R.string.call_action_mute_description)
                    CallStemAction.LAUNCH_SHORTCUT ->
                        shortcutName ?: stringResource(R.string.stem_action_launch_shortcut_description)
                }
                StyledListItem(
                    name = callStemActionLabel(action, null),
                    description = description,
                    selected = currentAction == action,
                    onClick = {
                        if (action == CallStemAction.LAUNCH_SHORTCUT && shortcutName.isNullOrEmpty()) {
                            pickShortcut()
                        } else {
                            viewModel.setCallStemAction(bud, type, action)
                        }
                    }
                )
            }
        }

        if (currentAction == CallStemAction.LAUNCH_SHORTCUT) {
            Spacer(modifier = Modifier.height(32.dp))
            ShortcutCard(shortcutName = shortcutName, pickShortcut = pickShortcut)
        }

        Spacer(modifier = Modifier.height(bottomPadding))
    }
}
