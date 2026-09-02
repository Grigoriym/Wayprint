# Frictions

Tooling friction hit during work, newest last. One line each. Promoted or fixed
entries get deleted — see /finalize.

- M5.2 verify: `adb shell content query --uri content://media/external/file --where "_data='...'"` threw `IllegalArgumentException: Invalid token storage` regardless of quoting — worked around with `--projection _id:_data` (no `--where`) piped to `grep`.
- M5.2 verify: `am start -a android.intent.action.SEND --grant-read-uri-permission --eu ... content://media/external/file/<id>` failed with "has no access to" — `adb shell am`'s calling identity (shell) can't forward a grant on a MediaStore row it doesn't itself hold under API 36's scoped media permissions; worked around by driving the real Files-app Share/Open-with UI instead of synthesizing the intent.
- M5.4 verify: `adb shell content query --uri content://media/external/images/media --projection col1,col2,col3` (comma-separated) threw `IllegalArgumentException: Invalid column col1,col2,col3` — worked around by omitting `--projection` entirely and grepping the full row output instead.
- M6.3: no local tool could validate `.github/workflows/ci.yml`'s YAML (`actionlint`/`yamllint` absent, `pip install pyyaml` refused under PEP 668 without `--break-system-packages`) — settled for hand-inspection instead of a real syntax check.
- Top-bar UI polish verify: `uiautomator dump` of DocumentsUI's file-picker list omitted the last (partially below-fold) row's text node (`fixture.gpx`) twice in a row — a screenshot taken at the same moment showed it fine. A third dump captured it. Retried the dump rather than assuming the row wasn't there/was misnamed.
