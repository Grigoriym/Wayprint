# Revisit

Real problems noticed outside the current task, logged here instead of fixed inline so the diff
stays reviewable. Not chat — this is the persistent record.

- **M6.3 keystore/secrets are blocked on the user generating `wayprint_fdroid_debug.jks`
  themselves.** `.github/workflows/ci.yml` (added in M6.3) expects a base64-encoded
  `WAYPRINT_FILE_FDROID_DEBUG` secret plus `WAYPRINT_STORE_PASS_FDROID_DEBUG`/
  `WAYPRINT_ALIAS_FDROID_DEBUG`/`WAYPRINT_KEY_PASS_FDROID_DEBUG` (see
  `AndroidApplicationConventionPlugin.kt`'s `configureAppSigningConfigs()`), none of which exist
  yet — per the M6.1 decision, the user generates every keystore themselves rather than having one
  AI-generated. Once the user has created `wayprint_fdroid_debug.jks` and says so, base64-encode it
  and `gh secret set` all four values, then push a branch to confirm a green Actions run — that
  completes M6.3's Verify line, which is currently unticked in `docs/CHECKLIST.md` for exactly this
  reason.
