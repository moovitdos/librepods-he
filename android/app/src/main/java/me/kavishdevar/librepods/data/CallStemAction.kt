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

/**
 * What press once / press twice do during an ACTIVE call
 * (Call Controls -> Customize, per bud). Stored under [StemPressPrefs.callActionKey].
 *
 * While a call is still ringing the AirPods always keep their built-in behaviour
 * (press once answers, press twice declines); these actions only apply once the
 * call is connected.
 */
enum class CallStemAction {
    /**
     * The AirPods' own in-call handling: mute or hang up as configured in Call Controls
     * (control command 0x24). When the other bud customizes the same press, the phone
     * re-creates this behaviour.
     */
    BUILT_IN,

    /** The phone ends the call. */
    END_CALL,

    /** The phone toggles the microphone mute. */
    MUTE,

    /** The phone starts the shortcut stored under [StemPressPrefs.callShortcutKey]. */
    LAUNCH_SHORTCUT;

    companion object {
        fun fromString(action: String?): CallStemAction? = entries.find { it.name == action }
    }
}
