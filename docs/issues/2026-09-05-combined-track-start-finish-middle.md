# 2026-09-05 — Combined track's Start/Finish labels land in the middle of the route

**Status:** Done
**Link:** none (reported directly in conversation, no GitHub issue)
**Updated:** 2026-09-05

## Report

User's words (paraphrased from chat): "when I merge three files, the finish and start are kinda
in the middle, i.e. between the central paths. Not sure how we define it and what is the order
the gpx tracks are added, we need to investigate that."

Split out:

| | |
|---|---|
| **Symptom** | Combining 3 GPX tracks into one image: the "Start" and "Finish" labels sit somewhere in the interior of the drawn route (between the other tracks' paths), not at either geographic end of the whole combined journey. |
| **Environment** | Not stated — no device, no which 3 tracks, no screenshot. Not needed to root-cause: the ordering mechanism below produces this symptom unconditionally for the layout the user describes (a 3-day continuous route), independent of device. |
| **Reporter's diagnosis** | None offered as fact — the user explicitly asked to investigate "what is the order the gpx tracks are added," i.e. named the right area to look but not a specific cause. Treated as the starting thread to pull, not a premise. |

Not stated: which 3 tracks, whether they were imported in trip order, or the exact tap order used
to select them for combining. These didn't block root-causing (the mechanism below reproduces the
symptom for *any* selection order that doesn't match trip chronology, which is easy to fall into
by construction — see Findings), but they'd help confirm this specific instance if wanted.

## Findings

**1. A combined layout's global Start/Finish anchor to the first and last track in the list, by
list position — not by any geographic or chronological property:**

`feature/wayprint/domain/src/commonMain/kotlin/com/grappim/wayprint/feature/wayprint/domain/WayprintLayout.kt:111-131`:

```kotlin
fun defaultCombinedLabelRequests(tracks: List<List<Pair<Double, Double>>>): List<LabelRequest> {
    val (startX, startY) = tracks.first().first()
    val (finishX, finishY) = tracks.last().last()
    ...
```

"Start" is the first point of `tracks[0]`; "Finish" is the last point of `tracks[last]`. Whatever
order `tracks` (i.e. the `inputs: List<Source>` passed to `buildCombinedWayprintLayout`) arrives
in is taken as truth. `buildCombinedWayprintRoute`
(`feature/wayprint/domain/.../WayprintRoute.kt:48-62`) does no reordering either — `inputs.map {
parseTrack(it) }` preserves whatever order it's given.

**2. That order is the order the user tapped tracks in the Recents multi-select list — nothing
else:**

`feature/wayprint/ui/src/commonMain/kotlin/com/grappim/wayprint/feature/wayprint/ui/list/RecentsViewModel.kt:106-134`:

```kotlin
/** Toggles [id]'s selection; appended to the end so [RecentsUiState.selectedIds] stays selection-ordered. */
fun toggleSelected(id: String) {
    val selected = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id
    ...
}

/**
 * Builds a new combined track (M11) under [storyPresetIndex] from the currently selected
 * tracks' GPX bytes, in selection order, ...
 */
fun combineSelected(storyPresetIndex: Int) {
    val ids = _uiState.value.selectedIds
    ...
    val tracks = ids.map { id -> tracksStorage.load(id) ?: error("Missing track $id") }
```

The code's own doc comments confirm this is deliberate design, not an oversight: "selection
order." There is no sort by date, filename, or any GPX-derived timestamp anywhere in this path.

**3. The Recents list itself is sorted newest-imported-first, which actively works against
tapping in trip order:**

`core/storage/src/commonMain/kotlin/com/grappim/wayprint/core/storage/TracksStorage.kt:80`:
`.sortedByDescending { it.importedAtEpochMillis }`.

If a user imports day 1, day 2, day 3 in that chronological order (the natural way to import a
multi-day trip), the Recents list displays them **newest-import-first**: day 3, day 2, day 1. A
user who then multi-selects top-to-bottom (the natural gesture) produces `selectedIds = [day3,
day2, day1]` — reversed relative to the trip.

**4. No GPX-derived timestamp exists anywhere to fall back on, even in principle:**

`core/gpx/src/commonMain/kotlin/com/grappim/wayprint/core/gpx/GpxParser.kt`'s `parseTrack` only
ever captures `lat`/`lon`/`ele` per `<trkpt>` (`TrackScan.startTrkpt`/`captureEle`/`finishTrkpt`,
lines 77-94) — there is no `<time>` handling at all, and `TrackMetadata`/`CombinedTrackMetadata`
(`core/storage/.../TrackMetadata.kt`) only store `importedAtEpochMillis` (when the file was
*imported into the app*, not when the recorded activity happened). So today, selection order is
the **only** signal the app has for "which track comes first" — there's no independent
chronological or spatial data to sanity-check it against, or to auto-derive it from.

**Reproduction of the reported symptom:** for a 3-day continuous route (day 1 end ≈ day 2 start,
day 2 end ≈ day 3 start — the ordinary shape of a multi-day trip), selecting in reversed order
`[day3, day2, day1]` makes:
- "Start" = `tracks.first().first()` = day 3's first point → geographically, this is wherever day
  3 begins, i.e. roughly where day 2 ended — **in the middle** of the whole route.
- "Finish" = `tracks.last().last()` = day 1's last point → wherever day 1 ended, i.e. roughly
  where day 2 began — **also in the middle**, and on the *other* side of day 2's central path from
  Start.

This matches "start and finish are kinda in the middle, between the central paths" exactly: with
3 tracks, the true geographic ends of the trip (day 1's actual start, day 3's actual finish) never
get picked at all when the selection order is fully reversed, and what gets labeled Start/Finish
instead are two points that both sit near the day-2 segment.

## Root cause

`defaultCombinedLabelRequests` (`WayprintLayout.kt:111-131`) treats **list order** of the selected
tracks as **route order** (first track's first point = trip start, last track's last point = trip
finish), but the only thing that currently determines list order is the order the user tapped rows
in the Recents multi-select UI (`RecentsViewModel.kt:106-134`, explicitly "selection order" per its
own doc comment) — a UI interaction order, not a property of the tracks themselves. Nothing
verifies, warns, or auto-corrects when that tap order doesn't match the tracks' real chronology.
This is made easy to trigger by `TracksStorage.list()`'s newest-first sort (`TracksStorage.kt:80`),
which presents multi-day trips in reverse-import order by default, and there is no GPX timestamp
parsed anywhere (`GpxParser.kt`) that could serve as an independent check even if one were added
later without also adding time parsing.

## Impact

Any combine of 2+ tracks where the user's tap order doesn't match the tracks' real chronological/
geographic order gets a wrong global Start/Finish. With the default Recents sort (newest-first),
this is the likely outcome for anyone selecting top-to-bottom after importing a trip in its
natural day-by-day order — i.e., this is not an edge case, it's close to the default path through
the UI as it stands today. No workaround in the UI: there's no way to see or change the resulting
order before or after combining (the labels can be manually dragged afterward via the existing
label-edit feature, but nothing tells the user *that* they need to, or which points are actually
the true ends).

## Open questions

- Should reordering be automatic (derived from data) or manual (user-controlled), or both? This is
  a real product decision, not just a bug — see Options below.
- If automatic ordering via GPX `<time>` is chosen: what's the fallback when one or more of the
  combined tracks has no `<time>` data at all (some exporters omit it)? Falling back to selection
  order silently would reproduce today's bug in that case.

## Options

1. **Sort tracks chronologically using each GPX's own recorded start time, before combining.**
   Requires adding `<time>` parsing to `core:gpx` (currently absent entirely — `parseTrack` only
   captures lat/lon/ele) and using each track's first point's timestamp (or an average) as its
   sort key in `combineSelected`/`buildCombinedWayprintRoute`.
   - **Pros:** Fixes the root cause with a real, computable, deterministic signal, independent of
     any UI interaction order — matches this project's "determinism over process" working
     agreement (one correct, computable answer). Once done, tap order stops mattering entirely.
   - **Cons:** Real scope: new parsing logic in `core:gpx` (a module this project treats as a
     ported-from-Python reference implementation, `gpx_route_art.py`, which itself has no time
     handling — this would be new behavior beyond the port). Needs a documented fallback for
     tracks missing `<time>` (common in some hand-edited or stripped GPX exports) — silently
     falling back to selection order reintroduces this exact bug for that case, so the fallback
     needs its own decision (e.g. warn the user, or refuse to auto-sort and require manual order
     for that combine).
   - **Risk/blast radius:** `core:gpx` (new parsing surface, needs unit tests per its existing
     coverage style), `WayprintRoute.kt`/`WayprintLayout.kt` (sort step), `RecentsViewModel.kt`/
     `WayprintViewModel.kt` (both call sites of `buildCombinedWayprintLayout`).

2. **Let the user see and reorder the selected tracks before combining** (e.g. a confirm step
   showing the selected tracks as a reorderable list, or drag-to-reorder chips), instead of
   silently trusting tap order.
   - **Pros:** No new GPX parsing; correct for every case including tracks with no `<time>` data at
     all (GPS art from disconnected days, non-chronological combines, etc.) since the user is the
     ground truth. Also makes the *existing* "selection order" model legible instead of hidden —
     right now nothing in the UI signals that tap order matters at all.
   - **Cons:** New UI surface (a reorder step/dialog) — more UI work than option 1's backend-only
     change, and still requires the user to notice and get it right rather than fixing it for them.
   - **Risk/blast radius:** Contained to the Recents combine flow
     (`RecentsScreen.kt`/`RecentsViewModel.kt`); doesn't touch `core:gpx` or the layout/render
     pipeline at all — `defaultCombinedLabelRequests` keeps working exactly as designed (first =
     start, last = finish), only the list feeding it changes.
3. **Both:** auto-sort by `<time>` when every track has it (option 1), fall back to showing the
   reorder UI (option 2) only when at least one track lacks `<time>` data (or always let the user
   review/override the auto-sorted order before confirming).
   - **Pros:** Correct by default without any extra taps for the common case (all tracks have
     `<time>`), with a real fallback instead of a silent wrong answer for the uncommon case.
   - **Cons:** Largest of the three — combines both diffs, and adds a decision about when exactly
     to show the review/override step.
   - **Risk/blast radius:** Union of options 1 and 2's.
4. **Do nothing / won't fix.** Not viable — a multi-day combine is the flagship use case for this
   feature (M11's whole purpose), and getting Start/Finish wrong is a visible, easily-triggered
   correctness bug in exactly that use case, not a cosmetic nit.

**Recommendation: Option 1**, with a documented fallback for missing `<time>` (leave that track's
position as-is relative to its neighbors in selection order, and surface via `error`/an inline
note that auto-ordering was only partial — exact fallback UX to be decided if this option is
approved). Reasoning: this project's working agreements favor a computable, deterministic fix over
one that depends on the user doing the right thing by hand every time (option 2), and the actual
trip-chronology signal already exists inside every real-world GPX export from Strava/Komoot/OsmAnd
— it's just never been read. Option 2 is a reasonable fallback/complement for the no-`<time>` case
(option 3) but shouldn't be the *only* fix, since it leaves the common case exactly as fragile as
it is today (still relies on the user tapping in the right order, with no cue that order matters).

## Decision

User chose **Option 2**: let the user see and reorder the selected tracks before combining,
rather than auto-sorting by GPX `<time>` (Option 1/3) — no new GPX parsing surface, and correct
for every case (including tracks with no `<time>` data at all) since the user is the ground
truth.

## What landed

- `feature/wayprint/ui/.../list/RecentsUiState.kt`: `RecentsUiState` gained
  `moveSelected(id, offset)`, a pure reducer that moves an id by `offset` positions within
  `selectedIds`, clamped to the list's bounds, a no-op if `id` isn't currently selected — same
  "pure state-transition function" pattern `WayprintUiState` already uses for testability without
  a ViewModel/DI harness.
- `feature/wayprint/ui/.../list/RecentsViewModel.kt`: `moveSelectedUp(id)`/`moveSelectedDown(id)`
  wire that reducer into the ViewModel (`_uiState.update { it.moveSelected(id, ±1) }`).
  `combineSelected` is unchanged — it still reads `selectedIds` in whatever order it's in, which
  is now the *reordered* order rather than raw tap order.
- `feature/wayprint/ui/.../list/RecentsScreen.kt`: the top-bar checkmark (previously opening the
  template-pick dialog directly) now opens a new `ReorderTracksDialog` first — an `AlertDialog`
  listing the selected tracks numbered 1..N in their current order, each row with up/down
  `IconButton`s (disabled at the list's own ends) calling `moveSelectedUp`/`moveSelectedDown`.
  Its confirm ("Next") proceeds to the existing template-pick dialog exactly as before; "Cancel"
  closes the reorder dialog without leaving selection mode.
- `feature/wayprint/ui/src/commonTest/.../list/RecentsUiStateTest.kt` (new): 6 tests covering
  `moveSelected` — swap earlier/later, clamp at both ends instead of wrapping, no-op for an
  unselected id, and a multi-position move past intermediate ids.

**Verified:**
- `./gradlew :feature:wayprint:ui:testAndroidHostTest :feature:wayprint:ui:jvmTest` — all pass,
  including the 6 new `RecentsUiStateTest` cases.
- `./gradlew :feature:wayprint:ui:compileAndroidMain :feature:wayprint:ui:compileKotlinIosArm64` —
  both succeed (iOS remains compile-only verified per this project's established cap).
- `detekt`/`ktlintCheck` pass.
- End-to-end on `Medium_Phone_API_36.1` (`gplay` debug): imported 3 synthetic GPX tracks forming a
  continuous 3-day route (day1 end ≈ day2 start ≈ day3 start), multi-selected them by tapping in
  Recents' default newest-first order (`day3`, `day2`, `day1` — the exact reversed order that
  reproduces the bug), tapped the combine checkmark. The new "Order the tracks" dialog opened
  showing `1. day3.gpx / 2. day2.gpx / 3. day1.gpx` with up disabled on row 1 and down disabled on
  row 3; used the down/up arrows to reorder to `1. day1.gpx / 2. day2.gpx / 3. day3.gpx` (the real
  chronology), confirmed the numbering updated live at each step, tapped "Next", picked "Story",
  and the combined image rendered with **Start at day 1's true first point and Finish at day 3's
  true last point** — the two actual geographic ends of the trip, not a point in the middle of
  the route as the original bug produced. Separately confirmed "Cancel" on the reorder dialog
  returns to the Recents list still in multi-select mode with the same 3 tracks checked, rather
  than exiting selection.

**Deliberately left out:** no automated test drives `RecentsScreen`'s Compose UI directly (same
"no Compose UI test harness in this project" gap the 2026-09-05 delete-button-overflow issue
already documented) — `RecentsUiStateTest`'s reducer coverage plus the manual emulator run above
are the full verification story.
