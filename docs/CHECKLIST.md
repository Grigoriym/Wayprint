# Wayprint checklist

**Current step:** M16 next (add an iOS KMP target — see below). M0–M15 (the full MVP roadmap, the
second layout template, the edit-toolbar decluttering, moving Android-only code out of
`commonMain`, and adding a real Desktop/JVM target) are complete and archived to
`docs/CHECKLIST_ARCHIVE.md`. M16's verification is capped — this machine is Linux, so an iOS app
can't actually be built or run here, only compiled. Release CI/publish was explicitly deferred by
the user (keystores ready since M6.3) — pick that back up only when they say the app is ready to
ship.

## How to use this

Each step below is meant to be done in **one fresh session with zero memory of prior sessions**.
Workflow:

1. Start a fresh session and say "read `docs/CHECKLIST.md` and do step `<N>`."
2. Do exactly that step — don't pull forward into later steps, even if it looks convenient.
3. Pass the step's **Verify** line before considering it done.
4. Tick the box, update the **Current step** banner above, and add a one-line `Note:` under the
   step if anything deviated from what it says.
5. Commit, open a PR, merge once it's green, clear context, repeat with the next step.
6. **Once every step in a milestone is checked off, move that milestone's whole section (heading,
   shared context, all steps, verbatim) from this file into `docs/CHECKLIST_ARCHIVE.md`, and
   update the banner above.** Don't leave a fully-done milestone sitting in this file — do this in
   the same session/commit that ticks its last box, not as separate cleanup later.

Ground rules (see `docs/IMPLEMENTATION_PLAN.md` for the *why* behind any of these):
- Root `CLAUDE.md` overrides this checklist's prose if the two ever conflict.
- A step's own claim from a prior session ("I already checked X") gets re-verified, not trusted.
- Tests are written in the same step as the logic they cover — hand-written fakes in `testing`,
  no mocking library.
- `detekt` and `ktlint` must pass before a step is ticked done.
- Don't do a step's cleanup/polish beyond what its Verify line asks for — that's a later step's
  job if it turns out to be needed.

Completed milestones live in `docs/CHECKLIST_ARCHIVE.md`, each step's full `Note:`/`Verify:` text
preserved verbatim — read that file for history/precedent, not this one.

## M16 — Add an iOS KMP target

Scoped 2026-09-04 alongside M15, but **don't start this until M15 is fully landed and archived**.
Reusing M15's `expect`/`actual` interfaces (proven by two real implementations, Android and JVM)
for a third platform is a much smaller, much less speculative step than designing those
interfaces for three platforms in one shot — this is the whole reason M15/M16 are two milestones
and not one.

**The hard limit that shapes every step here: this dev machine is Linux.** Kotlin/Native can
compile `iosArm64`/`iosSimulatorArm64` klibs on Linux with no Mac — that much is verifiable in
this environment. Producing or running an actual `.app`, an Xcode build, or anything on a
simulator/device requires a Mac with Xcode, which isn't available here. Every step's Verify line
below is capped accordingly, and says so explicitly — **"compiles" is not "works."** Don't let a
future session (or the user) mistake a green M16 checkbox for "the iOS app has been run."

Reference precedent: `../TaigaMobileNova/iosApp` — a real Xcode project (`iosApp.xcodeproj`,
`iOSApp.swift`, `ContentView.swift`, `Info.plist`) consuming `composeApp`'s iOS framework export.

- [ ] **M16.1** — Build-logic: add `iosArm64()`/`iosSimulatorArm64()` to `configureKmp()`
  (`KmpConfiguration.kt`), mirroring `TaigaMobileNova`'s same two lines. Add each M15 `expect`
  interface's iOS `actual` (`PlatformFileHandle` → `NSURL`, file read via `NSFileManager`/`NSData`,
  the picker launcher via a wrapped `UIDocumentPickerViewController`, image export via
  `UIActivityViewController`/`PHPhotoLibrary`).
  **Verify (capped — no Mac in this environment):** `./gradlew :core:gpx:compileIosArm64MainKotlinMetadata`
  (and the equivalent for every other module up through `composeApp`) succeeds on this machine.
  That is the full extent of what's verifiable here — the iOS actuals are **unverified beyond
  compiling** until run on a real Mac + simulator/device.

- [ ] **M16.2** — Add the `iosApp` Xcode project scaffold at the repo root, mirroring
  `TaigaMobileNova/iosApp`'s structure, plus `composeApp`'s iOS framework export config
  (`binaries.framework { ... }` in `composeApp/build.gradle.kts`, per Taiga's own).
  **Verify (capped — no Mac in this environment):** `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
  succeeds on this machine. Actually opening `iosApp.xcodeproj`, building, and running on a
  simulator/device is **out of reach here** — flag this step done-but-unverified-end-to-end in
  its `Note:`, and ask the user to confirm on their own Mac before treating M16 as trustworthy.

## Backlog (growth roadmap, not milestones yet)

- More input sources: Health Connect / Strava OAuth import; on-device live recording (its own
  milestone-scale scope jump when it happens, not an incremental add-on).
- A third layout template — poster — once M12 proves the two-template plumbing. Its own aspect
  ratio/size isn't decided yet; if it changes `canvasWidth` (not just height) from the 1080 every
  M12 template shares, M12's "constants don't need to scale" simplification (see M12 shared
  context) needs revisiting first.
- Color-scheme swatches (and any future style pickers) currently float as a permanent `TopEnd`
  overlay on the edit canvas — revisit if M13's toolbar decluttering makes that fixed position
  feel inconsistent, or once a poster template adds more style choices to pick from.
