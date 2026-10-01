#!/usr/bin/env python3
"""Check that the Hebrew strings stay in step with the English ones.

Run from anywhere inside the repository:

    python .claude/skills/librepods-he-dev/scripts/check_strings.py
    python .claude/skills/librepods-he-dev/scripts/check_strings.py --kotlin

Reports, for android/app/src/main/res:
  * keys present in values/ but missing from values-iw/ (untranslated)
  * keys present only in values-iw/ (stale or misspelled)
  * format placeholders (%1$s, %2$d, %%) that differ between the two
With --kotlin it also lists string literals in the Kotlin sources that look
like user-facing text passed straight to the UI. That part is a heuristic:
read each hit and decide; identifiers, preference keys and log messages are
supposed to stay literal.

Exit code is 1 when a key or placeholder problem is found, 0 otherwise
(heuristic Kotlin hits never fail the run).
"""
from __future__ import annotations

import argparse
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

RES = Path("android/app/src/main/res")
SRC = Path("android/app/src/main/java")
PLACEHOLDER = re.compile(r"%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]")
# Call sites where a bare literal ends up on screen.
UI_LITERAL = re.compile(
    r"""(?:\bText\(\s*|\btext\s*=\s*|\btitle\s*=\s*|\blabel\s*=\s*|\bdescription\s*=\s*|"""
    r"""\bsubtitle\s*=\s*|\bcontentDescription\s*=\s*|makeText\([^,]+,\s*|"""
    r"""setContentTitle\(\s*|setContentText\(\s*)"((?:[^"\\]|\\.)*)\""""
)


def repo_root() -> Path:
    here = Path.cwd().resolve()
    for candidate in [here, *here.parents]:
        if (candidate / RES / "values" / "strings.xml").is_file():
            return candidate
    sys.exit("Could not find android/app/src/main/res/values/strings.xml above the current directory.")


def load(path: Path) -> tuple[dict[str, str], set[str]]:
    """Return ({name: text}, {names marked translatable="false"})."""
    strings: dict[str, str] = {}
    untranslatable: set[str] = set()
    for element in ET.parse(path).getroot():
        name = element.get("name")
        if not name or element.tag not in ("string", "plurals", "string-array"):
            continue
        strings[name] = "".join(element.itertext())
        if element.get("translatable") == "false":
            untranslatable.add(name)
    return strings, untranslatable


def looks_like_ui_text(literal: str) -> bool:
    if len(literal) < 3 or not re.search(r"[A-Za-z֐-׿]", literal):
        return False
    if "$" in literal and not re.search(r"\s", literal):  # pure template such as "$name"
        return False
    # identifiers, keys, routes, actions: no spaces and snake/camel/dotted shape
    if not re.search(r"\s", literal) and re.fullmatch(r"[A-Za-z0-9_.:/\-]+", literal):
        return literal[0].isupper() and literal[1:].islower()  # a single capitalised word is still text
    return True


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--kotlin", action="store_true", help="also list likely hardcoded UI literals in Kotlin")
    parser.add_argument("--locale", default="iw", help="translation folder suffix to compare (default: iw)")
    args = parser.parse_args()

    root = repo_root()
    base, untranslatable = load(root / RES / "values" / "strings.xml")
    translated_path = root / RES / f"values-{args.locale}" / "strings.xml"
    if not translated_path.is_file():
        sys.exit(f"{translated_path} does not exist.")
    translated, _ = load(translated_path)

    missing = sorted(k for k in base if k not in translated and k not in untranslatable)
    extra = sorted(k for k in translated if k not in base)
    wrongly_translated = sorted(k for k in translated if k in untranslatable)
    placeholder_diff = sorted(
        k for k in base
        if k in translated and sorted(PLACEHOLDER.findall(base[k])) != sorted(PLACEHOLDER.findall(translated[k]))
    )

    print(f"values: {len(base)} keys ({len(untranslatable)} not translatable) | values-{args.locale}: {len(translated)} keys")
    problems = 0
    for title, keys in (
        (f"Missing from values-{args.locale} (add a translation)", missing),
        (f"Only in values-{args.locale} (stale or misspelled key)", extra),
        ("Marked translatable=\"false\" but translated anyway", wrongly_translated),
        ("Placeholders differ between the two files", placeholder_diff),
    ):
        if keys:
            problems += len(keys)
            print(f"\n{title}: {len(keys)}")
            for key in keys:
                if title.startswith("Placeholders"):
                    print(f"  {key}: {PLACEHOLDER.findall(base[key])} vs {PLACEHOLDER.findall(translated[key])}")
                else:
                    print(f"  {key}")
    if not problems:
        print("Keys and placeholders are in step.")

    if args.kotlin:
        hits = []
        for path in sorted((root / SRC).rglob("*.kt")):
            for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                stripped = line.strip()
                if stripped.startswith(("//", "*", "/*")) or "Log." in line or "@Preview" in line:
                    continue
                for match in UI_LITERAL.finditer(line):
                    if looks_like_ui_text(match.group(1)):
                        hits.append(f"  {path.relative_to(root).as_posix()}:{number}: \"{match.group(1)}\"")
        print(f"\nPossible hardcoded UI text in Kotlin (heuristic, review each): {len(hits)}")
        print("\n".join(hits))

    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
