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

package me.kavishdevar.librepods.presentation.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.kavishdevar.librepods.R
import me.kavishdevar.librepods.data.CallStemAction
import me.kavishdevar.librepods.data.StemAction
import me.kavishdevar.librepods.data.StemPressPrefs

/** Title of one stem. bud = "left" | "right" (see [StemPressPrefs]). */
@StringRes
fun stemBudTitle(bud: String): Int =
    if (bud.lowercase() == StemPressPrefs.LEFT) R.string.stem_left else R.string.stem_right

/** Title of one press type. typeKey = "single" | "double" | "triple" | "long". */
@StringRes
fun stemPressTypeTitle(typeKey: String): Int = when (typeKey.lowercase()) {
    "single" -> R.string.press_once
    "double" -> R.string.press_twice
    "triple" -> R.string.press_three_times
    else -> R.string.press_and_hold
}

/** Human readable label of a stem action; [shortcutName] is the chosen shortcut, if any. */
@Composable
fun stemActionLabel(action: StemAction, shortcutName: String?): String = when (action) {
    StemAction.PLAY_PAUSE -> stringResource(R.string.play_pause)
    StemAction.NEXT_TRACK -> stringResource(R.string.next_track)
    StemAction.PREVIOUS_TRACK -> stringResource(R.string.previous_track)
    StemAction.DIGITAL_ASSISTANT -> stringResource(R.string.digital_assistant)
    StemAction.CYCLE_NOISE_CONTROL_MODES -> stringResource(R.string.noise_control)
    StemAction.VOLUME_UP -> stringResource(R.string.stem_volume_up)
    StemAction.VOLUME_DOWN -> stringResource(R.string.stem_volume_down)
    StemAction.LAUNCH_SHORTCUT ->
        if (shortcutName.isNullOrEmpty()) stringResource(R.string.stem_action_launch_shortcut)
        else stringResource(R.string.stem_action_launch_shortcut_named, shortcutName)
}

/** Label of a press once / press twice action during calls (Call Controls -> Customize). */
@Composable
fun callStemActionLabel(action: CallStemAction, shortcutName: String?): String = when (action) {
    CallStemAction.BUILT_IN -> stringResource(R.string.call_action_built_in)
    CallStemAction.END_CALL -> stringResource(R.string.call_action_end)
    CallStemAction.MUTE -> stringResource(R.string.call_action_mute)
    CallStemAction.CYCLE_NOISE_CONTROL_MODES -> stringResource(R.string.noise_control)
    CallStemAction.VOLUME_UP -> stringResource(R.string.stem_volume_up)
    CallStemAction.VOLUME_DOWN -> stringResource(R.string.stem_volume_down)
    CallStemAction.LAUNCH_SHORTCUT ->
        if (shortcutName.isNullOrEmpty()) stringResource(R.string.stem_action_launch_shortcut)
        else stringResource(R.string.stem_action_launch_shortcut_named, shortcutName)
}

/** Settings-screen section: one entry per stem, leading to the per-bud press list. */
@Composable
fun StemPressSettings(
    navigateToStemBud: (String) -> Unit
) {
    StyledList(
        title = stringResource(R.string.stem_presses),
        description = stringResource(R.string.stem_presses_description)
    ) {
        StyledListItem(
            name = stringResource(R.string.left),
            description = stringResource(R.string.stem_presses_item_description),
            onClick = { navigateToStemBud(StemPressPrefs.LEFT) }
        )
        StyledListItem(
            name = stringResource(R.string.right),
            description = stringResource(R.string.stem_presses_item_description),
            onClick = { navigateToStemBud(StemPressPrefs.RIGHT) }
        )
    }
}
