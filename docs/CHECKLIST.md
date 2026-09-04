# Wayprint checklist

**Current step:** M13 and M14 scoped and ready (declutter the edit toolbar/split Save-Share; move
Android-only code out of `commonMain`) — see below. M0–M12 (the full MVP roadmap plus the second
layout template) are complete and archived to `docs/CHECKLIST_ARCHIVE.md`. Release CI/publish was
explicitly deferred by the user (keystores ready since M6.3) — pick that back up only when they
say the app is ready to ship.

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

## M13 — Declutter the edit toolbar: split Save/Share, move global actions off the canvas

Promoted from a design discussion in chat (2026-09-04, not a prior backlog item) after auditing
`WayprintScreen.kt`: every canvas corner plus bottom-center is already occupied by a floating
button (`TopEnd` color swatches, `BottomEnd` Add-label FAB, `TopStart` Undo (conditional),
`BottomStart` Delete-label (conditional), `BottomCenter` Export) — no room is left for the next
feature to add a button. On top of the clutter, `WayprintViewModel.exportAndShare()`
(`edit/WayprintViewModel.kt:174-191`) has a real UX bug: it silently inserts the bitmap into
`MediaStore` (a permanent save, no confirmation shown) *and* opens the share sheet, under one
unlabeled "Export" button — sharing has an undisclosed save side-effect.

Shared context:

- Confirmed by grep: no `FileProvider`, no `<provider>` entry, and no `res/xml/` resource
  directory exist anywhere in `androidApp` yet; no `SnackbarHost`/`Snackbar` exists anywhere in
  the app either. Both are new plumbing this milestone introduces, not a rewire of something
  already there.
- Design split, matching the convention in apps like Google Photos' editor: **Save** writes the
  bitmap into `MediaStore` (what `exportAndShare` already does) and confirms it happened; **Share**
  renders to a temp file under `context.cacheDir`, gets a `content://` URI via `FileProvider`, and
  opens the share sheet — with no `MediaStore` write, so sharing stops having an undisclosed
  permanent save.
- `TopAppBar`'s `actions` slot (Material3-standard; `WayprintScreen.kt:91-98` already has the bar,
  currently empty of actions) is where Undo/Save/Share move to — a `Row` of `IconButton`s, not a
  `DropdownMenu`/overflow. Don't build an empty overflow menu speculatively for a 3rd/4th future
  action that doesn't exist yet (Simplicity First) — a `Row` is trivially extended with one more
  `IconButton` when that day comes, no new infra required.
- What stays put and why: the Add-label FAB (`BottomEnd`) and the selected-label Delete button
  (`BottomStart`) are direct manipulation of canvas content, not global app actions — they don't
  belong in the app bar and this milestone doesn't touch them. Color-scheme swatches (`TopEnd`)
  also stay as-is — relocating them wasn't part of what was scoped here (no bottom-sheet/picker
  precedent exists in this app to reuse) and isn't required to fix either the clutter or the
  Save/Share bug; see Backlog below.
- Undo's existing conditional visibility (`uiState.canUndo`) carries over unchanged, just
  relocated from a floating `Button` to a `TopAppBar` `IconButton`.

- [x] **M13.1** — `feature:wayprint:ui`: split `WayprintViewModel.exportAndShare(bitmap)` into
  `saveToGallery(bitmap)` (the existing `MediaStore.Images.Media.insertImage` call, unchanged,
  plus a one-shot success signal the screen can show as a `Snackbar` — e.g. a buffered
  `SharedFlow`) and `share(bitmap)` (writes the PNG to `context.cacheDir`, builds a `content://`
  URI via `androidx.core.content.FileProvider`, launches the existing share `Intent` with that
  URI, no `MediaStore` write). Add the `androidApp` manifest `<provider>` entry and
  `res/xml/file_paths.xml` (`cache-path`) the `FileProvider` needs. `WayprintScreen`'s single
  `Export` button becomes two buttons (still wherever's convenient for this step — button
  *placement* is M13.2's job, not this one); wire a `SnackbarHost` in the `Scaffold` to show the
  save confirmation.
  **Verify:** `./gradlew :feature:wayprint:ui:testAndroidHostTest`, `detekt`, `ktlintCheck` pass.
  Emulator check (`emulator-testing` skill): tapping Save shows a confirmation and creates exactly
  one new `MediaStore` row (content query, same pattern as M5.2/M5.4's frictions); tapping Share
  opens the share sheet and creates **no** new `MediaStore` row; sharing to another app succeeds
  using the `FileProvider` URI (permission not denied).
  Note: the two buttons landed as a `Row` at `BottomCenter`, replacing the old single `Export`
  button 1:1 — no other placement changed, per this step's own "placement is M13.2's job" scope.
  Emulator check on `gplay debug`/`Medium_Phone_API_36.1`, all three points passed: Save's
  "Saved to gallery" Snackbar showed and the `content://media/external/images/media` row count
  went from 2 → 3 on one tap; Share's chooser opened with the count staying at 3, and picking a
  real target (Messages) opened cleanly with zero exceptions. One logcat wrinkle worth flagging:
  the chooser sheet's own preview-thumbnail loader (`com.android.intentresolver`) throws a
  `SecurityException` reading the URI before a target is picked — cosmetic (no thumbnail, chooser
  still lists apps and the picked target's own read succeeds), documented as a generic gotcha in
  the `emulator-testing` skill rather than here.

- [ ] **M13.2** — `feature:wayprint:ui`: move Undo, Save, and Share from floating `Button`s
  (`TopStart`/`BottomCenter`) into `WayprintScreen`'s `TopAppBar` `actions`, per shared context.
  `BottomStart`/`BottomEnd`/`TopEnd` overlays (Delete-label, Add-label FAB, color swatches) are
  unchanged.
  **Verify:** `./gradlew :feature:wayprint:ui:testAndroidHostTest`, `detekt`, `ktlintCheck` pass.
  Emulator check: Undo only appears in the top bar when `canUndo`, same as before; Save/Share both
  work identically to M13.1's verify from their new location; only the FAB, Delete-label (when a
  label is selected), and color swatches remain as floating canvas overlays.

## M14 — Move Android-only code out of `commonMain`

`commonMain` is supposed to be platform-agnostic per the KMP model, but 8 files import
`android.*` directly there (a deliberate tradeoff to skip `expect`/`actual` boilerplate while
there's only one target — see `WayprintViewModel.kt`'s doc comment). `./gradlew build` accepts it
today since this is a single-target (Android-only) module, but Android Studio doesn't resolve
these imports even after Invalidate Caches — confirmed with the user, not just a stale-cache
guess. It's also a real risk, not just IDE noise: `CLAUDE.md`'s roadmap wants `core:gpx` and the
Compose `Canvas` renderer to ship to iOS/Desktop eventually, and this code would fail to compile
there, not just red-squiggle.

Fix: move these files' *directories* from `src/commonMain` to `src/androidMain` (both source
sets already exist and are wired — `composeApp` already does this correctly for
`PlatformComponentModule`, its one `expect`/`actual` case). No code changes — confirmed by grep
that all cross-references between these 8 files stay within the moving set:

- `composeApp`: `WayprintAppContent.kt`, `WayprintNavHost.kt`, `WayprintEntryProvider.kt`
- `feature:wayprint:ui`: `WayprintScreen.kt`, `WayprintViewModel.kt`, `WayprintCanvas.kt`,
  `RecentsScreen.kt`, `RecentsViewModel.kt`

No other module (`core:gpx`, `feature:wayprint:domain`, `core:storage`, `core:navigation`,
`uikit`) imports `android.*` in `commonMain` — out of scope, nothing to move there. A few files
staying in `commonMain` (`EditableWayprintLayout.kt`, `RecentsUiState.kt`, `WayprintEditRoute.kt`)
reference the moving files only in KDoc `[links]`, not real imports — those may go dangling after
the move; cosmetic, not a compile break, fix opportunistically if noticed.

- [ ] **M14.1** — Move the 8 files above from `src/commonMain` to `src/androidMain` (same package
  path) in `composeApp` and `feature:wayprint:ui`. No logic changes.
  **Verify:** full `./gradlew build` (not per-module — this is a cross-file move, per M9.5's
  frictions note about per-module Verify missing cross-module regressions), `detekt`,
  `ktlintCheck` pass. In Android Studio: `Uri`/`Paint`/etc. resolve with no red squiggles in the
  moved files (Invalidate Caches / Restart once if needed right after the move).

## Backlog (growth roadmap, not milestones yet)

- More input sources: Health Connect / Strava OAuth import; on-device live recording (its own
  milestone-scale scope jump when it happens, not an incremental add-on).
- A third layout template — poster — once M12 proves the two-template plumbing. Its own aspect
  ratio/size isn't decided yet; if it changes `canvasWidth` (not just height) from the 1080 every
  M12 template shares, M12's "constants don't need to scale" simplification (see M12 shared
  context) needs revisiting first.
- iOS/Desktop targets for `core:gpx` and the Compose `Canvas` renderer.
- Color-scheme swatches (and any future style pickers) currently float as a permanent `TopEnd`
  overlay on the edit canvas — revisit if M13's toolbar decluttering makes that fixed position
  feel inconsistent, or once a poster template adds more style choices to pick from.
