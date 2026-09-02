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
