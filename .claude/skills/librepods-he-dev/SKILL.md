---
name: librepods-he-dev
description: Working methods for this repository, the Hebrew edition of LibrePods (AirPods on Android, branch hebrew-translation). Use it for any work here - adding or changing a feature in the Android app, adding or translating strings, stem-press / call-control / listening-mode shortcut behaviour, building the APK and the root-module ZIP through GitHub Actions, release signing, reading a failed CI run, installing and verifying a build on a rooted phone over adb, or syncing with upstream librepods-org/librepods. Use it even when the request is casual or in Hebrew ("תוסיף אפשרות", "תבנה", "למה הבנייה נכשלה", "תתרגם", "תבדוק בטלפון"), because several mistakes that look harmless here (turning minification back on, a new signing key, a string in only one file) break installs for existing users.
---

# LibrePods Hebrew edition - how we work

This repository is LibrePods (`librepods-org/librepods`, GPL-3.0) plus a set of
changes kept as small, separate commits so that each one could be offered
upstream:

- a complete Hebrew translation, done through string resources;
- two bug fixes (release crash on Android 13, system battery indicator);
- listening-mode shortcuts and intents for automation apps;
- per-stem, per-press actions, including "Launch shortcut" and call-time actions.

## Branches

| Branch | What it is | Rule |
|---|---|---|
| `main` | upstream `main`, untouched | never commit here; it exists so the diff against upstream stays readable |
| `hebrew-translation` (default) | all of our work, linear history on top of `main` | every change is one focused commit |

`git diff main..hebrew-translation --stat` is the quickest way to see everything
this edition changes. Upstream keeps moving (check for long-running branches
such as a rewrite before planning a sync); bring upstream in by updating `main`
and rebasing `hebrew-translation`, and expect conflicts in `strings.xml` and
`AirPodsService.kt`.

## The loop for any change

There is no practical local build for most machines (the app needs JDK 21,
`compileSdk 37` and NDK `30.0.14904198`), so **GitHub Actions is the compiler**.
That shapes the whole loop: a mistake costs a 10-15 minute round trip, so read
your change carefully before pushing instead of relying on the build to find it.

1. **Locate** the code with the map below; read the surrounding file first and
   match its style (Compose, Kotlin, the project's own components).
2. **Change code and strings together.** Any text a person can see goes into
   `values/strings.xml` (English) *and* `values-iw/strings.xml` (Hebrew) in the
   same commit. See `references/localization.md` for the rules and glossary.
3. **Check** with `python .claude/skills/librepods-he-dev/scripts/check_strings.py`
   (add `--kotlin` to list literals that may have slipped into the UI).
4. **Update `android/README.md`** when anything another app can observe changes:
   intent actions, extras, deep links, broadcast contents, the list of stem
   actions. Automation users copy from that file.
5. **Commit** - English, imperative, with the area as prefix, one logical change:
   `android: ...`, `ci: ...`, `android/README: ...`. Mention the upstream issue
   number when fixing a known upstream bug.
6. **Push `hebrew-translation`** - this starts the build. Then follow
   `references/build-and-signing.md`: wait, download the two artifacts, check the
   signing certificate.
7. **Install and verify on a device** before calling it a build - see
   `references/device-testing.md`. A green CI run only proves it compiles; the
   Android 13 release crash below passed CI.
8. **Number the build** (build 1, 2, ... one per successful CI run that was
   handed to a person) and note commit, run id and what changed, so a bug report
   can be traced to a commit.

## Things that must not regress

Each of these was learned the hard way; the reason is given so you can judge
edge cases yourself.

- **R8 minification stays off for release** (`isMinifyEnabled = false`,
  `isShrinkResources = false` in `android/app/build.gradle.kts`). A minified
  release crashes at launch on Android 13: the obfuscated `@Parcelize Battery`
  class makes `Intent.getParcelableArrayListExtra("data", Battery::class.java)`
  throw an NPE inside `Parcel.readParcelableCreatorInternal` (upstream issue
  #592). Debug builds never show it. `proguard-rules.pro` carries keep rules for
  `data.**`, Parcelable `CREATOR`s and the `@Serializable` navigation routes in
  case minification is ever wanted again - do not enable it without them and
  without testing a release build on Android 13. Cost of the fix: the APK is
  about 43 MB instead of about 13 MB.
- **One signing key forever.** Builds signed with the same key update in place
  (`adb install -r`, settings and Xposed scope survive). A different key forces
  every user to uninstall first and lose their settings. The key lives only in
  the repository's Actions secrets and with its owner; never commit a keystore,
  never print a password in a log, and never "fix" a signing failure by
  generating a new key.
- **`setBatteryMetadata()` must check `== PERMISSION_GRANTED`** for
  `BLUETOOTH_PRIVILEGED` (in `AirPodsService.kt`). Upstream had the comparison
  inverted, which skipped the left/right/case battery metadata exactly on
  privileged installs, so Android's own battery indicator stayed empty.
- **`ci-android.yml` is limited to `main`** on this branch so that upstream's
  release/Discord path never runs for our pushes. Our workflow is
  `hebrew-build.yml`; leave the upstream one alone otherwise.
- **Persisted enum names are data.** `StemAction` / `CallStemAction` constants are
  stored by name in preferences and sent in the `STEM_PRESS` broadcast. Renaming
  one breaks stored settings and other people's macros; removing one is fine as
  long as unknown stored values fall back to the default (they do - `fromString`
  returns null).
- **Do not bring back what was deliberately removed**: the volume up/down stem
  actions and the "automation only" action were tried and dropped. Ask before
  re-adding them.

## Behaviour decisions already made

Keep these unless asked to change them; they came out of testing with real calls.

- While a call is **ringing**, press once / press twice always keep the AirPods'
  built-in answer / decline. Call-time actions apply only to an **active** call.
- *Call Controls -> Customize* gives each stem its own press-once and press-twice
  action: Built-in, Hang up, Mute, Listening Mode (cycle), Launch shortcut.
- Normal stem actions: Play/Pause, Next, Previous, Listening Mode, Digital
  Assistant, Launch shortcut.
- The AirPods only forward a press type that is customized, and customizing a
  press type applies to both stems at the AirPods level; the phone then performs
  the other stem's action itself.
- Every customized press is broadcast as `me.kavishdevar.librepods.STEM_PRESS`,
  whatever its action.
- Labels are short on purpose - the reference phone has a very narrow screen and
  long titles were truncated.

## Code map

Paths are under `android/app/src/main/`.

| Area | Where |
|---|---|
| Strings | `res/values/strings.xml`, `res/values-iw/strings.xml` (`generateLocaleConfig = true`) |
| The service (connection, packets, stem handling, broadcasts, metadata) | `java/.../services/AirPodsService.kt` |
| Quick Settings tile (shares `cycleNoiseControlMode()` with the shortcuts) | `java/.../services/AirPodsQSService.kt` |
| Listening-mode shortcut trampoline (`SET_ANC_MODE`, `librepods://noise-control/...`) | `java/.../NoiseControlShortcutActivity.kt` |
| Legacy shortcut picker (`CREATE_SHORTCUT`, for MacroDroid / Tasker / Nova) | `java/.../NoiseControlShortcutPickerActivity.kt` |
| Launcher shortcuts and their icons | `res/xml/shortcuts.xml`, `res/drawable/ic_shortcut_*` |
| Stem actions and their stored preferences | `java/.../data/StemAction.kt`, `StemPressPrefs.kt`, `CallStemAction.kt` |
| Stem-press and call-control screens | `java/.../presentation/screens/StemPressScreens.kt`, `CallStemScreens.kt`, `components/StemPressSettings.kt`, `components/CallControlSettings.kt` |
| Navigation routes (`@Serializable`, kept from R8) | `java/.../presentation/navigation/Screen.kt`, `AppNavGraph.kt` |
| Native Bluetooth hook (L2CAP channel-mode check) | `cpp/l2c_fcr_hook.cpp`, loaded via `resources/META-INF/xposed/` |
| Root module template (installs the APK as a privileged app) | `/root-module-manual/` |
| Build, flavors (`foss` / `play`), signing, module ZIP task | `android/app/build.gradle.kts` |
| Our CI | `/.github/workflows/hebrew-build.yml` |

Two activities are exported on purpose (`NoiseControlShortcutActivity`,
`NoiseControlShortcutPickerActivity`) because launchers, automation apps, NFC
tags and `adb` have to start them. They do one harmless thing - switch the
listening mode. Do not add exported entry points that do more than that without
thinking about who can call them.

## Reference files

- `references/build-and-signing.md` - the workflow, secrets, creating a key for
  your own fork, downloading and verifying artifacts, reading failures, known CI
  breakages, local-build requirements.
- `references/localization.md` - where strings go, Compose scope rules, what must
  stay untranslated, Hebrew style and the term glossary.
- `references/device-testing.md` - what a device needs (root, Xposed framework,
  module scope), the two install paths, the verification checklist, signals that
  look like bugs but are not, settings that cause a disconnect loop, and `adb`
  commands for testing the automation features without AirPods in hand.

## Reporting

Say what was verified and how (CI run, certificate, on-device checks) separately
from what was only changed. If a step was skipped - most often the on-device
check because no phone was connected - say so; a build that has not run on a
phone is a candidate, not a build.
