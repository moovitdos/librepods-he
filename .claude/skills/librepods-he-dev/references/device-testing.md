# Installing and verifying on a device

## Contents
- What the device needs
- Two ways to install
- Updating an installed build
- Verification checklist
- Things that look like bugs and are not
- Settings that cause a disconnect loop
- Testing the automation features with adb
- Working on someone's phone

## What the device needs

- Android 13 or newer (`minSdk 33`), arm64.
- Root (Magisk, KernelSU or APatch).
- An Xposed framework that implements **libxposed API 101 or newer**. The module
  declares `minApiVersion=101` in `resources/META-INF/xposed/module.prop` and has
  a no-argument module constructor. Frameworks that only speak the older API
  cannot instantiate it (a `NoSuchMethodException` in the framework log) and the
  hook is never loaded - this, not the chipset, was what first looked like "does
  not work on MediaTek". JingMatrix **Vector 2.2** is the framework this edition
  is tested with.
- The module enabled with scope `com.android.bluetooth` (and
  `com.android.settings`; the Google-named variants on Pixel-style ROMs), then a
  reboot so the Bluetooth process starts with the hook.

With Vector the scope can also be set from a root shell:

```bash
adb shell su -c '/data/adb/modules/zygisk_vector/cli modules enable me.kavishdevar.librepods'
adb shell su -c '/data/adb/modules/zygisk_vector/cli scope set me.kavishdevar.librepods com.android.bluetooth/0 com.android.settings/0'
```

Why a hook at all: the phone's Bluetooth stack rejects the L2CAP channel mode
the AirPods ask for. `cpp/l2c_fcr_hook.cpp` replaces `l2c_fcr_chk_chan_modes` in
the Bluetooth library so that it answers "acceptable". Everything else (battery,
listening mode, ear detection, head tracking, stem presses) rides on that
channel.

## Two ways to install

| | Plain APK | Root module ZIP |
|---|---|---|
| File | `app-foss-release.apk` | `LibrePods-FOSS-v<version>-release.zip` |
| Installed as | ordinary app | privileged app (`system/priv-app`, systemless) |
| Gets | everything inside the app | plus `BLUETOOTH_PRIVILEGED` and `MODIFY_PHONE_STATE` |
| Needed for | basic use | Android's own battery indicator for the buds, reconnecting audio on real events, call handling |

Flash the ZIP from the root manager's Modules screen, reboot, then enable the
Xposed module and its scope. Changes to the system side go **through the module
only** - never by writing to the system partition by hand; a module can be
switched off or removed, an edited partition cannot.

## Updating an installed build

Same signing key -> update in place, no uninstall, no reboot:

```bash
adb install -r app-foss-release.apk
```

Over a module install the app becomes an "updated system app" and keeps its
privileged permissions, its settings and its Xposed scope. Reflash the module
ZIP only when the module itself (not just the APK) changed.

A signature mismatch (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`) means the APK was
signed with another key. Stop and find out why; do not uninstall to get around
it.

## Verification checklist

Run after every install, with the AirPods connected for the middle part.

```bash
# 1. the app process is alive and did not crash on start
adb shell pidof me.kavishdevar.librepods
adb logcat -d -b crash | grep -i librepods

# 2. the right build is installed
adb shell dumpsys package me.kavishdevar.librepods | grep -E "versionName|lastUpdateTime"

# 3. the native hook is loaded in the Bluetooth process and overrides the check
adb logcat -d -s LibrePodsHook
#    expect: fake_l2c_fcr_chk_chan_modes: orig = 0, returning 1

# 4. privileged permissions (module installs only)
adb shell dumpsys package me.kavishdevar.librepods | grep -E "BLUETOOTH_PRIVILEGED|MODIFY_PHONE_STATE"
#    expect: granted=true on both

# 5. battery metadata reaches Android (module installs only)
adb shell dumpsys bluetooth_manager | grep -i untethered
#    expect numbers, not null, for untethered_left_battery / right / case
```

Then look at the thing you changed: open the screen, press the stem, start the
shortcut. A launch check is not a feature check.

If step 3 prints nothing: reconnect the AirPods (the line is written when the
channel is negotiated), then check that the module is enabled, the scope
includes the Bluetooth app, and the phone was rebooted afterwards.

The AirPods settings screens (stem presses, call controls, ...) are only
reachable in the app while the AirPods are connected. If the phone is locked,
toasts and dialogs are not visible in screenshots - verify through `logcat` and
`dumpsys` instead.

## Things that look like bugs and are not

- **A "connected" pop-up right after `adb install -r`.** Reinstalling restarts the
  app's service, which reopens its channel to the AirPods and shows its
  connection UI again. Audio did not drop. Opening Android's Bluetooth settings
  does the same.
- **`Permission Denial ... BATTERY_LEVEL_CHANGED` in the log.** That is a
  protected broadcast no app may send; it is noise and not the path the battery
  indicator uses.
- **The battery indicator only on the device-details page.** Some ROMs show
  "Active" in the Bluetooth list and the left/right/case levels only behind the
  gear icon.

## Settings that cause a disconnect loop

In the app's settings there is a group of "take over the AirPods when..."
switches (stored as `takeover_when_*`). Leave **"when idle"** and **"when
disconnected"** off. With nothing playing, the AirPods themselves drop the link
after about half a minute; with those two switches on, the app takes the AirPods
back about ten seconds later, and the result is a real audio drop every ~40
seconds, forever. It only became visible on module installs, because the
privileged permission is what makes the take-over actually succeed. Media start,
ringing call and call are fine to keep on.

"Act as Apple device" enables the take-over path on every connection and
upstream has open reports of instability with it. If drops appear with the
switches above already off, turning it off is the next thing to try.

Exempting the app from battery optimization is harmless and worth doing; some
vendors add their own background killer on top, which needs the same exemption.

When diagnosing drops, read the log before forming a theory. A second Bluetooth
address in the AirPods' "connected devices" report is very often the phone's own
address (`adb shell settings get secure bluetooth_address`), not another device
competing for them.

## Testing the automation features with adb

The full description is in `android/README.md`. The short version, useful
without touching the AirPods:

```bash
# switch / cycle the listening mode through the exported activity
adb shell am start -a me.kavishdevar.librepods.SET_ANC_MODE --es mode anc
adb shell am start -a me.kavishdevar.librepods.SET_ANC_MODE
adb shell am start -a android.intent.action.VIEW -d librepods://noise-control/transparency

# watch real stem presses arrive
adb logcat | grep "Broadcast stem press"

# fake a stem press for a macro under test
adb shell am broadcast -a me.kavishdevar.librepods.STEM_PRESS --es type long --es bud left --es action LAUNCH_SHORTCUT
```

With the AirPods disconnected, `SET_ANC_MODE` should produce the "not
connected" toast and nothing else - that is the correct result for that state,
and a cheap check that the activity is wired up.

Automation apps list LibrePods in two different ways: modern launchers read
`res/xml/shortcuts.xml`, while MacroDroid's "Launch Shortcut" and Tasker's
"Shortcut" only see apps that answer `android.intent.action.CREATE_SHORTCUT`,
which is what `NoiseControlShortcutPickerActivity` is for. Test both when
touching shortcuts.

Starting a shortcut from the background relies on the "Display over other
apps" permission the app already requests.

## Working on someone's phone

The test device is usually a person's daily phone. Reading (logcat, dumpsys,
screenshots, pulling a copy) is fine. Anything that changes it - installing,
rebooting, clearing data, editing preferences, changing module scope - is their
decision unless they asked for exactly that. Say what you changed, restore any
setting you flipped for a test, and report on-device results separately from
what you only inferred.
