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
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Answers the legacy `android.intent.action.CREATE_SHORTCUT` request that
 * automation apps and launchers use to build a shortcut (MacroDroid "Launch
 * Shortcut", Tasker "Shortcut", Nova / Button Mapper shortcut pickers, ...).
 *
 * Shows a list of listening modes; the chosen one is returned as a shortcut
 * intent that opens [NoiseControlShortcutActivity]. The result carries both the
 * modern pin-request extra and the legacy EXTRA_SHORTCUT_* extras, so old and
 * new hosts understand it.
 */
class NoiseControlShortcutPickerActivity : Activity() {

    private class Option(
        val id: String,
        @StringRes val labelRes: Int,
        @DrawableRes val iconRes: Int,
        /** Value for the "mode" extra, or null to cycle. */
        val mode: String?
    )

    private val options = listOf(
        Option("noise_control_cycle", R.string.shortcut_cycle_long, R.drawable.ic_shortcut_cycle, null),
        Option("noise_control_anc", R.string.noise_cancellation, R.drawable.ic_shortcut_anc, "anc"),
        Option("noise_control_transparency", R.string.transparency, R.drawable.ic_shortcut_transparency, "transparency"),
        Option("noise_control_adaptive", R.string.adaptive, R.drawable.ic_shortcut_adaptive, "adaptive"),
        Option("noise_control_off", R.string.off, R.drawable.ic_shortcut_off, "off"),
    )

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val dialogTheme = if (isNight) {
            android.R.style.Theme_DeviceDefault_Dialog_Alert
        } else {
            android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
        }

        dialog = AlertDialog.Builder(this, dialogTheme)
            .setTitle(R.string.noise_control)
            .setItems(options.map { getString(it.labelRes) }.toTypedArray()) { _, which ->
                deliver(options[which])
            }
            .setNegativeButton(android.R.string.cancel) { d, _ -> d.cancel() }
            .setOnCancelListener { finish() }
            .show()
    }

    override fun onDestroy() {
        dialog?.setOnCancelListener(null)
        dialog?.dismiss()
        dialog = null
        super.onDestroy()
    }

    private fun deliver(option: Option) {
        try {
            val launchIntent = Intent(this, NoiseControlShortcutActivity::class.java).apply {
                action = NoiseControlShortcutActivity.ACTION_SET_ANC_MODE
                option.mode?.let { putExtra(NoiseControlShortcutActivity.EXTRA_MODE, it) }
            }
            val label = getString(option.labelRes)
            val icon = ContextCompat.getDrawable(this, option.iconRes)
                ?.toBitmap(ICON_SIZE_PX, ICON_SIZE_PX)
                ?.let { IconCompat.createWithBitmap(it) }
                ?: IconCompat.createWithResource(this, option.iconRes)

            val shortcut = ShortcutInfoCompat.Builder(this, option.id)
                .setShortLabel(label)
                .setLongLabel(label)
                .setIcon(icon)
                .setIntent(launchIntent)
                .build()

            setResult(RESULT_OK, ShortcutManagerCompat.createShortcutResultIntent(this, shortcut))
            Log.d(TAG, "Returning shortcut ${option.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build shortcut result", e)
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    companion object {
        private const val TAG = "NoiseControlShortcut"
        private const val ICON_SIZE_PX = 144
    }
}
