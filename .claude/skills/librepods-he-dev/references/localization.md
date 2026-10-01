# Localization

The Hebrew translation is done the standard Android way so that it follows the
phone's language (English phone -> English, Hebrew phone -> Hebrew) and could be
merged upstream as an ordinary translation.

## Where text lives

- English: `android/app/src/main/res/values/strings.xml`
- Hebrew: `android/app/src/main/res/values-iw/strings.xml`

The folder is `values-iw`, not `values-he`: Android's resource system knows
Hebrew by its legacy language code, and that is the name the working translation
uses - do not rename it or add a second folder. `generateLocaleConfig = true` in the Gradle file picks the folder
up automatically; there is no locale list to maintain by hand.

Only `app_name` and `flavor` are `translatable="false"`. The brand name
"LibrePods" and product names (AirPods, AirPods Pro) stay in Latin letters
inside Hebrew text, written `ה-AirPods`.

## The rule

Every piece of text a person can see comes from a resource, and both files get
the key in the same commit. A key that exists only in English shows English in
the middle of a Hebrew screen; a key that exists only in Hebrew has no default
and breaks the build or every other language.

Run the checker after touching strings:

```bash
python .claude/skills/librepods-he-dev/scripts/check_strings.py           # keys + placeholders
python .claude/skills/librepods-he-dev/scripts/check_strings.py --kotlin  # plus likely hardcoded UI text
```

The `--kotlin` list is a heuristic. On a clean tree it still shows a small
number of harmless hits - `@Preview` sample text ("Lorem ipsum", "Test"), brand
names ("Discord", "GitHub Issues", "Google Play") and pure templates
(`"$prefix $batteryPercentage%"`). A new hit that is a real sentence or label is
the thing to fix.

## What stays a literal

Do not move these into resources and do not translate them - they are data, not
text:

- preference keys, intent actions and extras, deep-link paths;
- enum constant names (`PLAY_PAUSE`, `LAUNCH_SHORTCUT`, ...), which are stored in
  preferences and sent in broadcasts;
- log messages and tags (`adb logcat` filters in the docs depend on them);
- navigation route names, file names, package names.

## Compose scope

`stringResource(R.string.x)` is a composable call. It works in the body of a
`@Composable` function and nowhere else - not inside `onClick = { }`, not inside
`LaunchedEffect { }`, not in a `remember { }` block's lambda, a ViewModel, the
service, or a plain helper function. These mistakes only show up as a failed CI
run ten minutes later, so check each new call site by eye.

- In a composable: `val label = stringResource(R.string.x)` near the top, then
  use `label` inside lambdas.
- Anywhere with a `Context` (service, activity, receiver, a lambda that captured
  `LocalContext.current`): `context.getString(R.string.x)`.
- With arguments: `stringResource(R.string.x, name)` /
  `getString(R.string.x, name)`, with `%1$s`, `%2$d` in the resource. Use
  numbered placeholders so the translation can reorder them, and write a literal
  percent sign as `%%`.

## XML details that break the build

- apostrophes must be escaped: `\'`
- `&` must be `&amp;`, `<` must be `&lt;`
- placeholders must match between the two files (the checker compares them)
- keep the key order of the Hebrew file close to the English one; it makes
  rebases on upstream survivable

## Hebrew style

- Address the user in the plural imperative, as the existing strings do:
  "בחרו", "לחצו", "ניתן להתאים".
- Short labels. Titles and buttons are tested on a very narrow screen; a title
  that truncates there should be shortened, not wrapped. Put the detail in the
  description line under it.
- Layout direction is handled by Android; do not add direction marks or reverse
  punctuation by hand. Check screens that mix Hebrew with Latin names or
  percentages for odd ordering.
- Keep one term per concept. The glossary below reflects what the app uses now.

| English | Hebrew |
|---|---|
| Listening mode / Noise control | מצב האזנה |
| Noise Cancellation | ביטול רעשים |
| Transparency | שקיפות |
| Adaptive (audio) | שמע אדפטיבי |
| Off | כבוי |
| Stem | ידית (older strings say גבעול; prefer ידית in anything new) |
| Stem presses | לחיצות על הידית |
| Press once / twice / three times | לחיצה אחת / לחיצה כפולה / לחיצה משולשת |
| Press and hold | לחיצה ארוכה |
| Call controls | בקרת שיחות |
| Hang up | ניתוק |
| Mute / unmute | השתקה / ביטול השתקה |
| Launch shortcut | הפעלת קיצור דרך |
| Digital assistant | עוזר דיגיטלי |
| Conversational awareness | מודעות לשיחה |
| Ear detection | זיהוי אוזן אוטומטי |
| Head tracking | מעקב ראש |
| Hearing aid | מכשיר שמיעה |
| Case | נרתיק |
| Left / Right | שמאל / ימין |

## Adding a string - worked example

A new stem action "Skip 30 seconds":

1. `values/strings.xml`: `<string name="stem_action_skip_30">Skip 30 seconds</string>`
2. `values-iw/strings.xml`: `<string name="stem_action_skip_30">דילוג של 30 שניות</string>`
3. In the screen: `stringResource(R.string.stem_action_skip_30)`.
4. The enum constant stays `SKIP_30` in code, preferences and the broadcast.
5. Run the checker; update the action table in `android/README.md`.

## Other languages in the tree

`values-de`, `-es`, `-fr`, `-pt`, `-tr`, `-uk`, `-vi`, `-zh-rCN`, `-zh-rTW` come
from upstream and lag behind the English file; keys added by this edition exist
only in English and Hebrew and fall back to English elsewhere. That is expected.
