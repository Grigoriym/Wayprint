# Frictions

Tooling friction hit during work, newest last. One line each. Promoted or fixed
entries get deleted — see /finalize.

- M5.2 verify: `adb shell content query --uri content://media/external/file --where "_data='...'"` threw `IllegalArgumentException: Invalid token storage` regardless of quoting — worked around with `--projection _id:_data` (no `--where`) piped to `grep`.
- M5.2 verify: `am start -a android.intent.action.SEND --grant-read-uri-permission --eu ... content://media/external/file/<id>` failed with "has no access to" — `adb shell am`'s calling identity (shell) can't forward a grant on a MediaStore row it doesn't itself hold under API 36's scoped media permissions; worked around by driving the real Files-app Share/Open-with UI instead of synthesizing the intent.
- M5.2 verify: eyeballed action-bar icon coordinates for DocumentsUI's overflow menu and landed on "Delete" instead, popping a delete-confirmation dialog on the fixture file — caught via `uiautomator dump` before confirming and cancelled; re-derived every subsequent action-bar tap from a dump instead of a screenshot estimate.
