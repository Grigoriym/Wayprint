# Wayprint — Emulator testing

Project-specific facts for the `emulator-testing` skill. Generic adb/uiautomator
technique lives in the skill itself, not here — this file is only what's true about
*this* app.

## Device facts

- AVD: `Medium_Phone_API_36.1` (also available: `Medium_Tablet`)
- Package id(s): `com.grappim.wayprint.debug` (gplay debug), `com.grappim.wayprint.fdroid.debug`
  (fdroid debug) — `STORE` flavor dimension (`gplay`/`fdroid`) + `.debug` suffix from the
  `debug` build type. Only `gplay` is routinely buildable locally: `fdroid`'s debug variant is
  force-signed (Variant API) with a dedicated `wayprint_fdroid_debug.jks` + env-var password
  that aren't provisioned yet (M6 — F-Droid/Play distribution — sets these up). Use
  `:androidApp:assembleGplayDebug` / the `gplay` package id for day-to-day verification.
- Activity: `com.grappim.wayprint.MainActivity`
- Backend/local server: none — the app is fully offline (no network in MVP, see root
  `CLAUDE.md`).

## App-specific gotchas

- No `local.properties` existed at repo root until M0.2 — `sdk.dir` had to be added by hand
  (`/home/gregory/Android/Sdk`) before any Gradle Android task would configure at all.
- `:androidApp:assembleDebug` (the flavor-aggregate task) currently fails on a clean checkout:
  it also builds `assembleFdroidDebug`, whose signing needs the F-Droid debug keystore/secrets
  above. Build `:androidApp:assembleGplayDebug` instead until M6 sorts out F-Droid signing for
  local/dev builds.
- `WayprintCanvas` is a raw `Canvas` composable (no semantics nodes), so a label's actual tap
  target isn't visible to `uiautomator dump` or reliably eyeballable from a screenshot: it's
  `PlacedLabel.boundingBox` (M3.2's placement-estimate size, `LabelPlacement.kt`'s 7×14
  canvas-units-per-char constants) — much smaller than the label's visually rendered 28sp glyph
  (see `docs/revisit.md`, M7.2). Screen coordinates also need the top app bar's height subtracted
  before comparing against canvas-space math (`WayprintCanvas`'s `Canvas` doesn't start at
  screen y=0). For any drag/tap-precision check on this screen, add a temporary
  `android.util.Log.d` in the hit-test path printing the computed canvas-space point and each
  label's `boundingBox`, rebuild+install, swipe once, read logcat for the real numbers, *then*
  compute the correct screen coordinate from the box center — don't just eyeball the screenshot
  and guess; on `Medium_Phone_API_36.1` at fit `scale≈1.0, offsetY≈93` (canvas-space) plus a
  ~219px top-app-bar offset (screen-space), several guessed coordinates missed the box entirely
  before the log-based approach hit on the first try. Remove the temporary log line before
  finishing the change.
