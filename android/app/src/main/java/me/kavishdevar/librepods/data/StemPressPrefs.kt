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

package me.kavishdevar.librepods.data

import me.kavishdevar.librepods.bluetooth.AACPManager.Companion.StemPressBudType
import me.kavishdevar.librepods.bluetooth.AACPManager.Companion.StemPressType

/**
 * Naming shared by the SharedPreferences keys, the UI state and the STEM_PRESS
 * broadcast for the per-bud, per-press stem configuration.
 *
 * bud  = "left" | "right"
 * type = "single" | "double" | "triple" | "long"
 */
object StemPressPrefs {
    const val LEFT = "left"
    const val RIGHT = "right"
    val BUDS = listOf(LEFT, RIGHT)

    fun typeKey(type: StemPressType): String = when (type) {
        StemPressType.SINGLE_PRESS -> "single"
        StemPressType.DOUBLE_PRESS -> "double"
        StemPressType.TRIPLE_PRESS -> "triple"
        StemPressType.LONG_PRESS -> "long"
    }

    fun typeFromKey(key: String): StemPressType? =
        StemPressType.entries.find { typeKey(it) == key.lowercase() }

    fun budKey(bud: StemPressBudType): String =
        if (bud == StemPressBudType.LEFT) LEFT else RIGHT

    /** Pre-existing key, e.g. `left_single_press_action`. Value: a [StemAction] name. */
    fun actionKey(bud: String, type: String) = "${bud}_${type}_press_action"

    /** Intent URI (`Intent.URI_INTENT_SCHEME`) of the shortcut run by [StemAction.LAUNCH_SHORTCUT]. */
    fun shortcutKey(bud: String, type: String) = "${bud}_${type}_press_shortcut"

    /** Human readable label of that shortcut, shown in the settings. */
    fun shortcutNameKey(bud: String, type: String) = "${bud}_${type}_press_shortcut_name"

    /** Key of the UI state maps, e.g. `left_single`. */
    fun stateKey(bud: String, type: String) = "${bud}_${type}"

    /** Press types with a built-in call function, customizable under Call Controls -> Customize. */
    val CALL_TYPES = listOf(StemPressType.SINGLE_PRESS, StemPressType.DOUBLE_PRESS)

    /** e.g. `left_single_press_call_action`. Value: a [CallStemAction] name. */
    fun callActionKey(bud: String, type: String) = "${bud}_${type}_press_call_action"

    /** Intent URI of the shortcut run by [CallStemAction.LAUNCH_SHORTCUT] during calls. */
    fun callShortcutKey(bud: String, type: String) = "${bud}_${type}_press_call_shortcut"

    /** Human readable label of that shortcut. */
    fun callShortcutNameKey(bud: String, type: String) = "${bud}_${type}_press_call_shortcut_name"

    /** Key of the UI state maps for the call profile, e.g. `left_single_call`. */
    fun callStateKey(bud: String, type: String) = "${bud}_${type}_call"
}
