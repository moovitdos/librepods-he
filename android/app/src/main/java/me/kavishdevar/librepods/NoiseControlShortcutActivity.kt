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

package me.kavishdevar.librepods

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import me.kavishdevar.librepods.data.NoiseControlMode
import me.kavishdevar.librepods.services.AirPodsService
import me.kavishdevar.librepods.services.ServiceManager
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Invisible trampoline activity that switches the listening (noise control) mode
 * and immediately finishes. It exists so that the mode can be changed from
 * *outside* the app: launcher shortcuts (see res/xml/shortcuts.xml), automation
 * apps (Tasker, MacroDroid, Automate, Key Mapper, ...), NFC tags, `adb`, etc.
 *
 * Accepted intents:
 *  - action [ACTION_SET_ANC_MODE] (or any action, e.g. MAIN) with optional extra
 *    [EXTRA_MODE]: `off` / `anc` / `transparency` / `adaptive`, or the raw AACP
 *    value 1–4. Without the extra the mode is cycled exactly like a tap on the
 *    Quick Settings tile.
 *  - action VIEW with a `librepods://noise-control/<mode>` or
 *    `librepods://noise-control?mode=<mode>` URI. `librepods://noise-control`
 *    on its own cycles.
 */
class NoiseControlShortcutActivity : Activity() {

    companion object {
        const val ACTION_SET_ANC_MODE = "me.kavishdevar.librepods.SET_ANC_MODE"
        const val EXTRA_MODE = "mode"
        private const val TAG = "NoiseControlShortcut"

        /**
         * Maps a user supplied mode (name or number) to the AACP listening-mode
         * value. Returns null for "cycle" / empty input and -1 for garbage.
         */
        fun parseMode(raw: Any?): Int? {
            val text = when (raw) {
                null -> return null
                is Number -> return raw.toInt().takeIf { it in 1..4 } ?: -1
                is CharSequence -> raw.toString().trim().lowercase()
                else -> return -1
            }
            if (text.isEmpty()) return null
            text.toIntOrNull()?.let { return if (it in 1..4) it else -1 }
            return when (text.replace("-", "_").replace(" ", "_")) {
                "cycle", "next", "toggle", "switch" -> null
                "off", "none" -> NoiseControlMode.OFF.ordinal + 1
                "anc", "nc", "noise_cancellation", "noisecancellation", "noise_cancelling" ->
                    NoiseControlMode.NOISE_CANCELLATION.ordinal + 1
                "transparency", "transparent", "passthrough" -> NoiseControlMode.TRANSPARENCY.ordinal + 1
                "adaptive" -> NoiseControlMode.ADAPTIVE.ordinal + 1
                else -> -1
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handle(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
        finish()
    }

    private fun handle(intent: Intent?) {
        try {
            val rawMode = extractRawMode(intent)
            val requestedMode = parseMode(rawMode)
            Log.d(TAG, "Received ${intent?.action} data=${intent?.data} mode=$rawMode -> $requestedMode")

            if (requestedMode == -1) {
                toast(getString(R.string.shortcut_invalid_mode, rawMode.toString()))
                return
            }

            val service = ServiceManager.getService()
            if (service == null) {
                // The background service is not running, so nothing can talk to the
                // AirPods. Kick it off so the next tap works.
                Log.w(TAG, "AirPodsService not running; starting it")
                try {
                    startForegroundService(Intent(this, AirPodsService::class.java))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start AirPodsService", e)
                }
                toast(getString(R.string.airpods_not_connected))
                return
            }

            val newMode = if (requestedMode == null) {
                service.cycleNoiseControlMode()
            } else {
                if (service.setNoiseControlMode(requestedMode)) requestedMode else null
            }

            if (newMode == null) {
                toast(getString(R.string.airpods_not_connected))
            } else {
                toast(getString(R.string.shortcut_mode_set, modeLabel(newMode)))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling shortcut intent", e)
        }
    }

    /** Looks for the mode in the extras first, then in a `librepods://` URI. */
    @Suppress("DEPRECATION")
    private fun extractRawMode(intent: Intent?): Any? {
        val extras = intent?.extras
        if (extras != null && extras.containsKey(EXTRA_MODE)) {
            return extras.get(EXTRA_MODE)
        }
        val uri = intent?.data ?: return null
        uri.getQueryParameter(EXTRA_MODE)?.let { return it }
        return uri.lastPathSegment
    }

    private fun modeLabel(mode: Int): String {
        return when (mode) {
            NoiseControlMode.OFF.ordinal + 1 -> getString(R.string.off)
            NoiseControlMode.NOISE_CANCELLATION.ordinal + 1 -> getString(R.string.noise_cancellation)
            NoiseControlMode.TRANSPARENCY.ordinal + 1 -> getString(R.string.transparency)
            NoiseControlMode.ADAPTIVE.ordinal + 1 -> getString(R.string.adaptive)
            else -> getString(R.string.qs_mode_unknown)
        }
    }

    private fun toast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
