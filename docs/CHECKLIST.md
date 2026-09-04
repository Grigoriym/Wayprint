# Wayprint checklist

**Current step:** M12.4 done — M12 (second layout template) is fully complete. M0–M11 (the full
MVP roadmap) are also complete — moved to `docs/CHECKLIST_ARCHIVE.md`. Release CI/publish was
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

Ground rules (see `docs/IMPLEMENTATION_PLAN.md` for the *why* behind any of these):
- Root `CLAUDE.md` overrides this checklist's prose if the two ever conflict.
- A step's own claim from a prior session ("I already checked X") gets re-verified, not trusted.
- Tests are written in the same step as the logic they cover — hand-written fakes in `testing`,
  no mocking library.
- `detekt` and `ktlint` must pass before a step is ticked done.
- Don't do a step's cleanup/polish beyond what its Verify line asks for — that's a later step's
  job if it turns out to be needed.

Completed milestones (M0–M11) live in `docs/CHECKLIST_ARCHIVE.md`, each step's full `Note:`/
`Verify:` text preserved verbatim — read that file for history/precedent, not this one.

## M12 — a second layout template (square post)

Adds a square-canvas template alongside the existing story canvas — the "multiple layout
templates" backlog item, scoped down to exactly one new shape. Backlog item promoted to a
milestone (2026-09-04, user-decided during investigation): the template a track uses is **locked
in at creation time** (fresh GPX import, or the moment two tracks are combined into one) and
never changes after, the same way the source GPX itself is immutable once imported — this rules
out any story for reflowing M10's freeform label positions onto a new aspect ratio, since the
canvas shape under them never changes post-creation. Scope for this milestone is exactly one new
template (square) proving the plumbing end-to-end; **poster stays backlog** (see below) — a third
shape can reuse this milestone's pattern later without redesigning it.

Shared context:

- `StoryPreset` (`feature/wayprint/domain/.../StoryPreset.kt:10-26`) is already a plain
  `data class`, not a singleton — the pipeline that consumes it (`buildWayprintLayout`/
  `buildCombinedWayprintLayout`, `fitScale`, `renderWayprintStoryBitmap`/
  `renderCombinedWayprintStoryBitmap`) is already fully parameterized by whichever instance it's
  given, confirmed by survey — no pipeline changes needed. The actual gaps are: (a) only one
  instance (`DEFAULT_STORY_PRESET`) exists, no list/registry to pick from; (b) nothing persists
  which preset a track was created with; (c) nothing lets the user choose one; (d) the call sites
  that render/export a track (`WayprintScreen.kt`) hardcode the `DEFAULT_STORY_PRESET` literal
  instead of reading it from the loaded track.
- **Key simplification, decide once here:** give the square preset the *same* `canvasWidth =
  1080.0` as the story preset (e.g. `SQUARE_STORY_PRESET = StoryPreset(canvasWidth = 1080.0,
  canvasHeight = 1080.0, routeBoxWidth = 860.0, routeBoxHeight = 860.0, marginX = 110.0, marginY
  = 110.0)` — square route box/margins mirroring the story preset's own `marginX = 110.0`, just
  applied symmetrically). Every absolute-pixel drawing/placement constant in the codebase today
  (`WayprintCanvas.kt:36-42`'s `ROUTE_LINE_WIDTH`/marker radii/`LABEL_TEXT_SIZE`/
  `LABEL_HALO_WIDTH`, `LabelPlacement.kt:3-4`'s `CHAR_WIDTH`/`TEXT_HEIGHT`,
  `WayprintLayout.kt:7`'s `LABEL_OFFSET`) is tuned for "a 1080-wide canvas," not specifically
  "1080×1920" — keeping every preset's `canvasWidth` at 1080 means **none of those constants need
  to change or become preset fields** for this milestone. This only holds as an invariant while
  every template shares the same width; a future *poster* template that changes width (not just
  height) reopens the question of whether these become explicit `StoryPreset` fields or a derived
  scale factor — flag that in `IMPLEMENTATION_PLAN.md` §9 when poster is scoped, don't solve it
  now.
- Precedent for "small fixed list, index-based, persisted": M7's `PRESET_COLOR_SCHEMES`/
  `colorSchemeIndex` (`StoryPreset.kt:38-40`, `ColorSchemeSwatches` in
  `WayprintScreen.kt:236-259`, `TrackMetadata.colorSchemeIndex`). Reuse the same shape — a
  `STORY_PRESETS: List<StoryPreset>` with the existing story preset at index `0` (so a *missing*
  stored value, i.e. every track persisted before this milestone, defaults to the unchanged story
  shape with no migration needed) and square at index `1` — but note `ColorSchemeSwatches` itself
  is rendered post-import inside the edit screen (`WayprintScreen.kt:128-133`), which doesn't fit
  "picked once at creation" — this milestone needs a *new* picker shown during import/combine, not
  a reuse of that composable's call site (its clickable-circle-row visual shape can still be
  copied).
- Where template choice must be threaded in: `RecentsViewModel.importGpx`
  (`list/RecentsViewModel.kt:58-90`) and `combineSelected()` (same file, `:124-157`) both build
  `TrackMetadata`/`CombinedTrackMetadata` and call `tracksStorage.save`/`saveCombined` directly,
  synchronously, with no user choice in between (`colorSchemeIndex` is hardcoded to `0` at
  `:72`). A template pick has to happen before that `save` call, for both entry points.
  `CombinedTrackMetadata` (`core/storage/.../TrackMetadata.kt:24-31`) has no `colorSchemeIndex`
  at all today (a pre-existing, unrelated gap — not this milestone's to fix) but *does* need a new
  `storyPresetIndex`-equivalent field, same as `TrackMetadata`, since both creation paths need one.
- This app's only existing Compose dialog pattern is `AlertDialog` (delete-confirmation and
  `AddLabelDialog` in `RecentsScreen.kt`/`WayprintScreen.kt`) — no `ModalBottomSheet`/`DropdownMenu`
  precedent exists anywhere in `feature/wayprint/ui`. Match that precedent for the new picker
  rather than introducing a new dialog primitive for this one case.
- `WayprintViewModel.loadTrack()` (`edit/WayprintViewModel.kt:59-95`) already reads
  `colorSchemeIndex` back for `Single` tracks and hardcodes `0` for `Combined` (line 81, since
  `CombinedTrackMetadata` has no field to read) — the new `storyPresetIndex` needs the same
  read-back for both kinds now that both persist one.

- [x] **M12.1** — `feature:wayprint:domain`: add `SQUARE_STORY_PRESET` and `STORY_PRESETS:
  List<StoryPreset>` (story at index 0, square at index 1) to `StoryPreset.kt`, per the exact
  values and the canvas-width-invariant reasoning in shared context above. Confirm — don't
  assume — that no downstream numeric drift exists by running the pipeline against the square
  preset with a fixture GPX, same pattern `WayprintLayoutTest`/`WayprintRouteTest` already use for
  the story preset.
  **Verify:** `./gradlew :feature:wayprint:domain:testAndroidHostTest`, `detekt`, `ktlintCheck`
  pass.
  Note: added a `StoryPresetTest` case reusing `WayprintLayoutTest`'s fixture GPX against
  `SQUARE_STORY_PRESET` — confirmed 3 non-overlapping labels stay within the square canvas
  bounds, no numeric drift from the existing story-preset pipeline.

- [x] **M12.2** — `core:storage`: add a persisted preset selector (e.g. `storyPresetIndex: Int =
  0`) to both `TrackMetadata` and `CombinedTrackMetadata` — the default of `0` is what lets every
  track persisted before this milestone keep loading as the story shape with no migration step.
  **Verify:** `./gradlew :core:storage:testAndroidHostTest`, `detekt`, `ktlintCheck` pass. A test
  deserializing a pre-M12.2-shaped JSON fixture (no `storyPresetIndex` key) confirms it still
  loads, defaulting to index 0.
  Note: added the field to both data classes with a `storyPresetIndex: Int = 0` default, plus a
  round-trip test for a non-default value and two legacy-JSON-fixture tests (one per metadata
  type, writing raw pre-M12.2 `metadata.json` content directly) confirming both `load` and
  `loadCombined` default to 0 when the key is absent.

- [x] **M12.3** — `feature:wayprint:ui`: a template-pick step (new `AlertDialog`, matching this
  app's existing dialog precedent — see shared context) inserted into `RecentsViewModel.importGpx`
  and `combineSelected()`'s call sites in `RecentsScreen.kt`, shown once before `save`/
  `saveCombined` fires, its chosen index threaded into the new `TrackMetadata`/
  `CombinedTrackMetadata` field from M12.2. `WayprintViewModel.loadTrack()` reads it back for both
  `Single` and `Combined` (no more hardcoded `0` for `Combined`).
  **Verify:** `./gradlew :feature:wayprint:ui:testAndroidHostTest`, `detekt`, `ktlintCheck` pass.
  Note: added `RecentsScreen`'s `TemplatePickDialog` (two `TextButton`s, "Story"/"Square", picking
  index 0/1) shown via a new `TemplatePickTarget` (`Import(uri)`/`Combine`) state, gating both the
  file-picker result and the share-intent `pendingImportUri` effect, and the combine check-icon
  click — each now stages a target instead of calling the view model directly, and the dialog's
  `onSelect` fires the real `importGpx`/`combineSelected` call with the chosen index.
  `importGpx`/`combineSelected` gained a `storyPresetIndex: Int` parameter threaded straight into
  the M12.2 metadata field. `WayprintViewModel.loadTrack()`'s `Triple` became a named
  `RestoredTrack(loaded, colorSchemeIndex, storyPresetIndex, layout)` so both `Single` and
  `Combined` branches read `metadata.storyPresetIndex` back into a new
  `WayprintUiState.storyPresetIndex` field (plain, not part of the undo `EditSnapshot` — the
  template is locked at creation and never edited, per M12's design). Full `./gradlew build`
  (all modules, not just `feature:wayprint:ui`) also passes. M12.4 is what actually makes
  `storyPresetIndex` affect rendering — this step only plumbs it through storage/state.

- [x] **M12.4** — `feature:wayprint:ui`: `WayprintScreen`'s render/export call sites
  (`WayprintCanvas`/`CombinedWayprintCanvas`, `renderWayprintStoryBitmap`/
  `renderCombinedWayprintStoryBitmap`) stop hardcoding the `DEFAULT_STORY_PRESET` literal and read
  the loaded track's actual preset (via `STORY_PRESETS[storyPresetIndex]`) instead.
  **Verify:** `./gradlew :feature:wayprint:ui:testAndroidHostTest`, `detekt`, `ktlintCheck` pass.
  Emulator check (`emulator-testing` skill): import a track choosing square — edit screen renders
  a square canvas, exported/shared bitmap is square-dimensioned, force-stop/relaunch preserves the
  choice; import and combine choosing story still render/export unchanged; a track from before
  this milestone (or one created choosing story) still opens correctly.
  Note: scope had to expand past the step's literal wording. Fixing only `WayprintScreen`'s draw
  calls left the route badly broken for square: `buildWayprintLayout`/`buildCombinedWayprintLayout`
  project the route into `preset.routeBoxWidth/Height` (860×980 for story vs 860×860 for square) —
  that preset has to match at *layout-build* time, not just at draw time, or the route/marker
  positions are computed for the wrong box and overflow the canvas. Two more call sites were
  silently defaulting to `DEFAULT_STORY_PRESET` and needed the same fix: `WayprintViewModel
  .loadTrack()` (rebuilds the layout on every load/restore) and `RecentsViewModel.importGpx`/
  `combineSelected()` (build the *initial* layout whose labels get persisted into
  `TrackMetadata`/`CombinedTrackMetadata` at creation time — already had `storyPresetIndex` in
  scope as a parameter, just wasn't threading it into the layout builder call). Caught on the
  emulator, not by the unit tests: importing a real GPX fixture as square rendered the Finish
  marker/label ~370px below the square canvas's bottom edge — M12.1's `StoryPresetTest` fixture
  apparently didn't exercise this path's numeric drift. All three call sites now build with
  `STORY_PRESETS[storyPresetIndex]`; shared context's "pipeline is already fully parameterized, no
  pipeline changes needed" was accurate for the pipeline functions' signatures, just not for
  which of their callers actually passed the non-default preset through.

## Backlog (growth roadmap, not milestones yet)

- More input sources: Health Connect / Strava OAuth import; on-device live recording (its own
  milestone-scale scope jump when it happens, not an incremental add-on).
- A third layout template — poster — once M12 proves the two-template plumbing. Its own aspect
  ratio/size isn't decided yet; if it changes `canvasWidth` (not just height) from the 1080 every
  M12 template shares, M12's "constants don't need to scale" simplification (see M12 shared
  context) needs revisiting first.
- iOS/Desktop targets for `core:gpx` and the Compose `Canvas` renderer.
