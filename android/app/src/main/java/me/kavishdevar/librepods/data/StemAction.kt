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

import me.kavishdevar.librepods.bluetooth.AACPManager

enum class StemAction {
    PLAY_PAUSE,
    PREVIOUS_TRACK,
    NEXT_TRACK,
    DIGITAL_ASSISTANT,
    CYCLE_NOISE_CONTROL_MODES,

    /**
     * Start the shortcut the user picked with the system shortcut chooser
     * (a MacroDroid macro, a Tasker task, an app, a contact, ...). The shortcut
     * intent is stored under [StemPressPrefs.shortcutKey] for this bud + press.
     * Every press that reaches the phone is also announced to automation apps
     * through the [AirPodsNotifications.STEM_PRESS] broadcast, whatever its action.
     */
    LAUNCH_SHORTCUT;
    companion object {
        fun fromString(action: String): StemAction? {
            return entries.find { it.name == action }
        }
        val defaultActions: Map<AACPManager.Companion.StemPressType, StemAction> = mapOf(
            AACPManager.Companion.StemPressType.SINGLE_PRESS to PLAY_PAUSE,
            AACPManager.Companion.StemPressType.DOUBLE_PRESS to NEXT_TRACK,
            AACPManager.Companion.StemPressType.TRIPLE_PRESS to PREVIOUS_TRACK,
            AACPManager.Companion.StemPressType.LONG_PRESS to CYCLE_NOISE_CONTROL_MODES,
        )
    }
}
