# Building and signing

## Contents
- The workflow
- Running a build and getting the files
- Checking what you got
- Signing: the four secrets
- Setting up signing on your own fork
- When a run fails
- Building locally (if you really want to)
- Versions and build numbers

## The workflow

`.github/workflows/hebrew-build.yml` (its display name says "Debug" for
historical reasons; it builds **release**):

- triggers: every push to `hebrew-translation`, and manual `workflow_dispatch`;
- runs `./gradlew assembleFossRelease zipReleaseModule` in `android/`;
- uploads two artifacts:
  - `apk-foss-release` -> `app-foss-release.apk`
  - `root-module-release` -> `LibrePods-FOSS-v<version>-release.zip`, the
    flashable root module with the same APK inside
    (`system/priv-app/LibrePods/LibrePods.apk`);
- publishes nothing: no GitHub release, no Discord post.

Only the `foss` flavor is built. The `play` flavor has `minSdk 36` and billing
code and is not part of this edition.

A run takes roughly 10-15 minutes; most of it is the NDK download and the native
build.

## Running a build and getting the files

Pushing the branch is enough. To build again without a new commit:

```bash
gh workflow run hebrew-build.yml --ref hebrew-translation
```

Follow it and fetch the result (pass `-R <owner>/<repo>` whenever the clone has
more than one remote, otherwise `gh` may look at the wrong repository):

```bash
gh run list --workflow hebrew-build.yml --limit 3
gh run watch <run-id> --exit-status
gh run download <run-id> -D out/
```

## Checking what you got

Before the files go anywhere, confirm the signer. A build that silently fell
back to another key would install for nobody who has the app already.

```bash
apksigner verify --print-certs out/apk-foss-release/app-foss-release.apk
```

Compare the `certificate SHA-256 digest` with the previous build's. When a phone
is connected you can compare against what is actually installed - pull the
installed APK and run the same command on it:

```bash
adb shell pm path me.kavishdevar.librepods      # prints package:<path>/base.apk
adb pull <path>/base.apk installed.apk
apksigner verify --print-certs installed.apk
```

Publish SHA-256 sums of whatever you hand to people (`sha256sum file`), and
regenerate them for every build - file names stay the same between builds, so
the hash is the only thing that tells two builds apart.

## Signing: the four secrets

`android/app/build.gradle.kts` reads signing data from `android/local.properties`
and signs release **and** debug with it when all four values are present:

| Secret | Meaning |
|---|---|
| `RELEASE_KEYSTORE_FILE` | the keystore file, base64 on one line |
| `RELEASE_STORE_PASSWORD` | keystore password |
| `RELEASE_KEY_ALIAS` | key alias |
| `RELEASE_KEY_PASSWORD` | key password (same as the store password for PKCS12) |

The workflow decodes the keystore to `android/release.keystore` and writes
`local.properties` at build time. Nothing about the key is in the repository,
and it must stay that way.

If the secrets are missing or empty, Gradle treats signing as unavailable and
the release APK comes out unsigned under a different file name, so expect the
run to fail late - at packaging or at the upload step, which finds no
`app-foss-release.apk`. On a fresh fork that is a setup problem, not a code
problem.

The upstream project's own release key is private. Builds from this repository
are therefore *not* updates of the upstream APK: moving between the two means
uninstalling first.

## Setting up signing on your own fork

Create one key and keep it safe; losing it means your users reinstall.

```bash
keytool -genkeypair -v -storetype PKCS12 -keystore release.keystore \
  -alias librepods -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 release.keystore | gh secret set RELEASE_KEYSTORE_FILE
gh secret set RELEASE_STORE_PASSWORD     # paste when prompted
gh secret set RELEASE_KEY_PASSWORD
gh secret set RELEASE_KEY_ALIAS --body librepods
```

Secrets cannot be read back from GitHub, so copying a repository's signing setup
to another repository always starts from the original keystore file and
passwords, never from the first repository. After setting them, run a build and
compare the certificate digest with a known-good APK.

Let the person who owns the key enter or approve the secret values; do not paste
passwords into chat, commit messages or logs.

## When a run fails

```bash
gh run view <run-id> --log-failed
```

Read which **step** failed first; it usually tells you whether the cause is the
code, the environment or the secrets.

| Failing step | Usual cause |
|---|---|
| Setup Android SDK: `Failed to find package 'tools'` | `setup-android@v3` installs `tools platform-tools` by default and the obsolete `tools` package is gone. The workflow sets `packages: platform-tools`; keep that line. |
| Install NDK | the pinned NDK version changed in `build.gradle.kts` (`ndkVersion`) but not in the workflow, or the download timed out - re-run once. |
| Decode keystore, a signing error, or "no files found" at upload | secrets missing or wrong on this repository. |
| `compile...Kotlin` | a real code error. The usual ones here: `stringResource()` called outside a composable (inside `onClick`, `LaunchedEffect`, a plain function) - resolve the string in the composable above or use `context.getString()`; a `when` over `StemAction` / `CallStemAction` that is no longer exhaustive after adding or removing a constant; a missing import after moving a screen; a string key used in code but missing from `values/strings.xml`. |
| `merge...Resources` / AAPT | malformed XML in a strings file - an unescaped apostrophe (`\'`), a bare `&`, or `%` that should be `%%`. |

An environment failure that upstream does not have is worth comparing with
upstream's latest green run of `ci-android.yml`: the same runner image and
actions are used, so a difference in the workflow file is usually the answer.

Re-running without a change is only useful for network flakiness. If the same
step fails twice, it is not flaky.

## Building locally (if you really want to)

Needs JDK 21, Android platform 37, NDK `30.0.14904198` and CMake 3.22.1. Without
the four `RELEASE_*` lines in `android/local.properties` the release APK comes
out unsigned and debug uses the machine's debug key - neither updates an
installed CI build. For a quick compile check, `./gradlew :app:compileFossDebugKotlin`
is much faster than a full assemble.

## Versions and build numbers

`appVersionName` and `versionCode` in `android/app/build.gradle.kts` follow
upstream; this edition does not bump them, so the version shown in the app does
not identify a build. That is why builds are numbered separately and tied to a
commit and run id, and why hashes are published. If upstream's version changes
during a sync, artifact file names change with it - update anything that refers
to them.
