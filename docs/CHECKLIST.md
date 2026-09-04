# Wayprint checklist

**Current step:** M14 scoped and ready (move Android-only code out of `commonMain`) — see below.
M0–M13 (the full MVP roadmap, the second layout template, and the edit-toolbar decluttering) are
complete and archived to `docs/CHECKLIST_ARCHIVE.md`. Release CI/publish was explicitly deferred
by the user (keystores ready since M6.3) — pick that back up only when they say the app is ready
to ship.

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
