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

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import me.kavishdevar.librepods.R
import me.kavishdevar.librepods.bluetooth.AACPManager
import me.kavishdevar.librepods.bluetooth.AACPManager.Companion.StemPressType
import me.kavishdevar.librepods.data.StemAction
import me.kavishdevar.librepods.data.StemPressPrefs
import me.kavishdevar.librepods.presentation.components.ListItemOrientation
import me.kavishdevar.librepods.presentation.components.StyledButton
import me.kavishdevar.librepods.presentation.components.StyledList
import me.kavishdevar.librepods.presentation.components.StyledListItem
import me.kavishdevar.librepods.presentation.components.stemActionLabel
import me.kavishdevar.librepods.presentation.components.stemPressTypeTitle
import me.kavishdevar.librepods.presentation.theme.DesignSystem
import me.kavishdevar.librepods.presentation.theme.LocalDesignSystem
import me.kavishdevar.librepods.presentation.viewmodel.AirPodsViewModel
import kotlin.io.encoding.ExperimentalEncodingApi

/** Order of the options in the action picker (the built-in default is moved to the top). */
private val ACTION_ORDER = listOf(
    StemAction.PLAY_PAUSE,
    StemAction.NEXT_TRACK,
    StemAction.PREVIOUS_TRACK,
    StemAction.CYCLE_NOISE_CONTROL_MODES,
    StemAction.DIGITAL_ASSISTANT,
    StemAction.LAUNCH_SHORTCUT,
)

/**
 * Opens the system shortcut chooser (ACTION_CREATE_SHORTCUT, the same list MacroDroid,
 * Tasker and launchers use) and hands the picked shortcut to [onPicked] as an intent URI
 * (`Intent.toUri(URI_INTENT_SCHEME)`) plus a label. Returns the function that opens it.
 */
@Composable
internal fun rememberShortcutPicker(onPicked: (intentUri: String, name: String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val unsupportedText = stringResource(R.string.stem_shortcut_unsupported)
    val chooserTitle = stringResource(R.string.stem_shortcut_chooser_title)

    @Suppress("DEPRECATION")
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode != Activity.RESULT_OK || data == null) return@rememberLauncherForActivityResult
        // Legacy shortcut result: what MacroDroid, Tasker, Button Mapper etc. consume too.
        val shortcutIntent = IntentCompat.getParcelableExtra(
            data, Intent.EXTRA_SHORTCUT_INTENT, Intent::class.java
        )
        val name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
        if (shortcutIntent == null) {
            Toast.makeText(context, unsupportedText, Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }
        onPicked(
            shortcutIntent.toUri(Intent.URI_INTENT_SCHEME),
            name?.takeIf { it.isNotBlank() }
                ?: shortcutIntent.component?.packageName
                ?: shortcutIntent.action
                ?: ""
        )
    }
    return {
        launcher.launch(
            Intent.createChooser(Intent(Intent.ACTION_CREATE_SHORTCUT), chooserTitle)
        )
    }
}

/** Card showing the chosen shortcut (or that none is chosen yet); tapping opens the chooser. */
@Composable
internal fun ShortcutCard(shortcutName: String?, pickShortcut: () -> Unit) {
    StyledList(
        title = stringResource(R.string.stem_shortcut_title),
        description = stringResource(R.string.stem_shortcut_description)
    ) {
        StyledListItem(
            name = shortcutName?.takeIf { it.isNotEmpty() }
                ?: stringResource(R.string.stem_shortcut_none),
            description = stringResource(R.string.stem_shortcut_change),
            onClick = pickShortcut
        )
    }
}

/**
 * The four press types of one stem (press once / twice / three times / hold),
 * each showing its current action and leading to [StemPressActionScreen].
 * bud = "left" | "right".
 */
@ExperimentalHazeMaterialsApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StemBudScreen(
    viewModel: AirPodsViewModel,
    bud: String,
    navigateToStemPress: (String) -> Unit
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

        StyledList(
            description = stringResource(R.string.stem_bud_description)
        ) {
            for (type in StemPressType.entries) {
                val typeKey = StemPressPrefs.typeKey(type)
                val stateKey = StemPressPrefs.stateKey(bud, typeKey)
                val action = state.stemActions[stateKey] ?: StemAction.defaultActions[type]!!
                StyledListItem(
                    name = stringResource(stemPressTypeTitle(typeKey)),
                    description = stemActionLabel(action, state.stemShortcutNames[stateKey]),
                    onClick = { navigateToStemPress(typeKey) }
                )
            }
        }

        Spacer(modifier = Modifier.height(bottomPadding))
    }
}

/**
 * Picks the action of one bud + press. typeKey = "single" | "double" | "triple" | "long".
 * For LAUNCH_SHORTCUT the system shortcut chooser is opened and the returned shortcut
 * intent is stored; press-and-hold + listening mode also shows the list of modes to
 * cycle through, like the original press-and-hold screen.
 */
@ExperimentalHazeMaterialsApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StemPressActionScreen(
    viewModel: AirPodsViewModel,
    bud: String,
    typeKey: String,
    navigateToPurchase: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val type = StemPressPrefs.typeFromKey(typeKey) ?: StemPressType.LONG_PRESS
    val stateKey = StemPressPrefs.stateKey(bud, typeKey)
    val defaultAction = StemAction.defaultActions[type]!!
    val currentAction = state.stemActions[stateKey] ?: defaultAction
    val shortcutName = state.stemShortcutNames[stateKey]

    val pickShortcut = rememberShortcutPicker { intentUri, name ->
        viewModel.setStemShortcut(bud, type, intentUri, name)
        viewModel.setStemAction(bud, type, StemAction.LAUNCH_SHORTCUT)
    }

    val m3eEnabled = LocalDesignSystem.current == DesignSystem.Material
    val topPadding = if (m3eEnabled) 0.dp else WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 84.dp
    val bottomPadding = if (m3eEnabled) 0.dp else WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp

    val scrollState = rememberScrollState()

    val actions = listOf(defaultAction) + ACTION_ORDER.filter { it != defaultAction }

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
            for (action in actions) {
                val description = when (action) {
                    defaultAction -> stringResource(R.string.stem_action_built_in)
                    StemAction.LAUNCH_SHORTCUT ->
                        shortcutName ?: stringResource(R.string.stem_action_launch_shortcut_description)
                    else -> null
                }
                StyledListItem(
                    name = stemActionLabel(action, null),
                    description = description,
                    selected = currentAction == action,
                    enabled = action != StemAction.DIGITAL_ASSISTANT || state.isPremium,
                    onClick = {
                        if (action == StemAction.LAUNCH_SHORTCUT && shortcutName.isNullOrEmpty()) {
                            // Nothing picked yet: the picker stores the shortcut and selects the action.
                            pickShortcut()
                        } else {
                            viewModel.setStemAction(bud, type, action)
                        }
                    }
                )
            }
        }

        if (!state.isPremium) {
            Spacer(modifier = Modifier.height(24.dp))
            StyledButton(
                onClick = navigateToPurchase,
                backdrop = rememberLayerBackdrop(),
                modifier = Modifier.fillMaxWidth(),
                maxScale = 0.05f,
                surfaceColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    stringResource(R.string.unlock_advanced_features),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (currentAction == StemAction.LAUNCH_SHORTCUT) {
            Spacer(modifier = Modifier.height(32.dp))
            ShortcutCard(shortcutName = shortcutName, pickShortcut = pickShortcut)
        }

        if (type == StemPressType.LONG_PRESS && currentAction == StemAction.CYCLE_NOISE_CONTROL_MODES) {
            Spacer(modifier = Modifier.height(32.dp))

            val currentByte = state.controlStates[AACPManager.Companion.ControlCommandIdentifiers.LISTENING_MODE_CONFIGS]?.get(0)?.toInt() ?: 0

            StyledList(
                title = stringResource(R.string.noise_control),
                description = stringResource(R.string.press_and_hold_noise_control_description)
            ) {
                if (state.offListeningMode) {
                    StyledListItem(
                        name = stringResource(R.string.off),
                        description = stringResource(R.string.listening_mode_off_description),
                        selected = (currentByte and 0x01) != 0,
                        onClick = {
                            viewModel.toggleListeningMode(0x01)
                        },
                        orientation = ListItemOrientation.Vertical,
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.noise_cancellation),
                                contentDescription = stringResource(R.string.icon),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .height(42.dp)
                                    .wrapContentWidth()
                            )
                        }
                    )
                }

                StyledListItem(
                    name = stringResource(R.string.transparency),
                    description = stringResource(R.string.listening_mode_transparency_description),
                    selected = (currentByte and 0x04) != 0,
                    onClick = {
                        viewModel.toggleListeningMode(0x04)
                    },
                    orientation = ListItemOrientation.Vertical,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.transparency),
                            contentDescription = stringResource(R.string.icon),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .height(42.dp)
                                .wrapContentWidth()
                        )
                    }
                )

                StyledListItem(
                    name = stringResource(R.string.adaptive),
                    description = stringResource(R.string.listening_mode_adaptive_description),
                    selected = (currentByte and 0x08) != 0,
                    onClick = {
                        viewModel.toggleListeningMode(0x08)
                    },
                    orientation = ListItemOrientation.Vertical,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.adaptive),
                            contentDescription = stringResource(R.string.icon),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .height(42.dp)
                                .wrapContentWidth()
                        )
                    }
                )

                StyledListItem(
                    name = stringResource(R.string.noise_cancellation),
                    description = stringResource(R.string.listening_mode_noise_cancellation_description),
                    selected = (currentByte and 0x02) != 0,
                    onClick = {
                        viewModel.toggleListeningMode(0x02)
                    },
                    orientation = ListItemOrientation.Vertical,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.noise_cancellation),
                            contentDescription = stringResource(R.string.icon),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .height(42.dp)
                                .wrapContentWidth()
                        )
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(bottomPadding))
    }
}
