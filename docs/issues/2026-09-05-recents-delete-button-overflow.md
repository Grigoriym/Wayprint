# 2026-09-05 — Recents list delete button too small / missing for combined tracks

**Status:** Done
**Link:** none (reported directly in conversation, no GitHub issue)
**Updated:** 2026-09-05

## Report

User's words: "the delete button in the list is very small and somehow is not shown for a
merged route (not sure why, maybe because the title is too big)".

Split out:

| | |
|---|---|
| **Symptom** | On the Recents track list, the per-row delete (trash) icon button looks smaller than expected, and for a combined/merged track it doesn't appear at all. |
| **Environment** | Not stated — no device/emulator, no screen size, no screenshot. Not asked for; the code-level cause turned out fully sufficient to confirm without one (see Findings). |
| **Reporter's diagnosis** | "maybe because the title is too big" — offered as a guess, not a fact. Treated as a hypothesis to verify, not a premise. |

Not stated: how many tracks were merged, how long their filenames were, or whether the button
is fully invisible vs. just clipped/partial. These didn't block root-causing the bug (the code
itself proves the mechanism unconditionally), but they'd help size the real-world frequency.

## Findings

All in `feature/wayprint/ui/src/commonMain/kotlin/com/grappim/wayprint/feature/wayprint/ui/list/`.

**The row layout (`RecentsScreen.kt:219-250`, `RecentTrackRow`) is one composable shared by both
Single and Combined tracks — there is no branch that hides the delete button for combined items:**

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        .padding(horizontal = SCREEN_PADDING, vertical = ROW_VERTICAL_PADDING),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isSelectionMode && item.isCombinable) {
            Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        }
        Column {
            Text(item.displayName)                                    // line 240
            Text("${item.importedDate} · ${item.distanceLabel}")
        }
    }
    if (!isSelectionMode) {
        IconButton(onClick = onDeleteClick) {                         // line 245
            Icon(Icons.Filled.Delete, contentDescription = "Delete")
        }
    }
}
```

- The leading `Row` (checkbox + `Column`) has **no `Modifier.weight(1f)`** — confirmed by
  `RecentsScreen.kt`'s import list, which has no `weight` import at all.
- `Text(item.displayName)` (line 240) has **no `maxLines`/`overflow`** — it lays out at its full
  intrinsic width rather than wrapping to a bound or ellipsizing.
- The trailing `IconButton` (line 245) has no explicit size modifier; it's the plain Material3
  default (48dp touch target, 24dp icon) — no code path shrinks or hides it based on item type.

**`item.isCombinable` (`RecentsViewModel.kt:166`, `this is TrackListEntry.Single`) gates only the
checkbox and multi-select entry — never the delete button:**
- `RecentsScreen.kt:236`: `if (isSelectionMode && item.isCombinable)` → checkbox visibility.
- `RecentsScreen.kt:171`: `onLongClick = { if (item.isCombinable) viewModel.enterSelection(item.id) }`.
- The delete `IconButton` at line 245 is gated only on `!isSelectionMode`, identical for both
  item types.

**A combined track's `displayName` is unbounded and grows with every merged track**
(`RecentsViewModel.kt:141`, inside `combineSelected`):

```kotlin
displayName = tracks.joinToString(" + ") { it.metadata.displayName },
```

A `Single` track's `displayName` is just its imported filename (`RecentsViewModel.kt:71`,
`handle.displayName()` — e.g. `morning_run.gpx`). A `Combined` track's is every constituent
filename joined with literal `" + "`, no length cap or truncation. Two typically-named tracks
already produce a 50+ character string (e.g.
`"morning_run.gpx + evening_loop_around_the_lake.gpx"`); each additional merged track adds more.

## Root cause

`RecentTrackRow` (`RecentsScreen.kt:219-250`) never reserves layout space for the trailing delete
button. The leading content (checkbox + title/subtitle `Column`) is unweighted and its `Text`
title is unbounded, so in a `Row` with `Arrangement.SpaceBetween` the leading content is measured
and placed at its own intrinsic width — which, for a short filename, happens to leave enough room
for the 48dp `IconButton` to fit and look normal-sized, but for a long string (any single filename
long enough, and *systematically* for combined tracks via the unbounded `joinToString(" + ")`
concatenation) grows past the available row width. Compose does not shrink unweighted `Row`
children to make room for a sibling; the excess width instead pushes the trailing `IconButton`
partially or fully outside the row's visible bounds. This is one mechanism explaining both
complaints: a moderately long title squeezes/clips the button (looks "very small" or
off-position), and a combined track's typically much longer joined title pushes it off-screen
entirely ("not shown").

This is a layout bug, not a deliberate design choice — there is no comment, test, or
`isCombinable`-gated branch anywhere suggesting the delete action is meant to differ for combined
tracks (`docs/CHECKLIST_ARCHIVE.md`'s M11.4 note is explicit that only the *checkbox*/multi-select
was scoped to exclude combined items).

## Impact

Every combined/merged track in the Recents list is effectively **undeletable via the UI** once its
joined `displayName` is long enough to overflow — which is close to guaranteed for two or more
typically-named GPX files, given the `" + "`-joined construction has no cap. Single tracks are
affected only when their filename alone is unusually long, so the "very small" complaint likely
applies more narrowly to those, while "not shown" is the common case for anything combined. No
workaround exists in the UI itself; `TracksStorage.delete(id)` is reachable but nothing in the app
UI can invoke it for an affected row (the confirmation `AlertDialog` never opens without the
button). A user stuck with an underletable combined track would need to clear app storage
entirely or use a file manager — no in-app path exists today.

## Open questions

- None blocking. The mechanism is verified directly from the layout code and requires no
  device/screenshot to confirm — Compose's non-weighted `Row` measurement behavior only has one
  outcome here.

## Options

1. **Constrain the title `Text` to one line with ellipsis, and weight the leading content**
   (`Modifier.weight(1f, fill = false)` or `fill = true` on the leading `Row`, plus
   `maxLines = 1, overflow = TextOverflow.Ellipsis` on the title `Text`). Guarantees the trailing
   `IconButton` always gets its full 48dp regardless of title length.
   - **Pros:** Fixes both the missing-button case and the "very small" squeeze case with one
     change; standard, idiomatic Compose list-row pattern; smallest possible diff (a few
     modifiers/params, no new composables).
   - **Cons:** Long combined names get truncated in the list — a user can no longer read the full
     joined filename list at a glance without opening the track. Acceptable given the row already
     shows date + distance as the more useful at-a-glance info, and the full name is presumably
     visible elsewhere (e.g. on the edit screen) — worth confirming but not blocking.
   - **Risk/blast radius:** Contained to `RecentTrackRow`; no other composable reuses it.

2. **Same as (1), but also cap/reformat the combined `displayName` at construction time**
   (`RecentsViewModel.kt:141`) — e.g. `"3 tracks"` or a shortened join with a `"+N more"` suffix,
   instead of the raw filename concatenation — in addition to the layout fix.
   - **Pros:** Avoids ever persisting an unbounded string as the canonical name (it's stored in
     `CombinedTrackMetadata.displayName`, not just computed for display), which matters if that
     name is surfaced anywhere else (e.g. share text, exported image) later.
   - **Cons:** A real (if small) scope increase over the reported bug — changes what's persisted
     to storage, needs a decision on the exact format, and drifts from "the display name is
     literally the source filenames" which may be intentional/useful elsewhere. Not verified
     whether `displayName` is read anywhere else that would care.
   - **Risk/blast radius:** `core:storage`'s `CombinedTrackMetadata` shape unaffected (still a
     plain string field), but the *content* convention changes — touches whatever else reads
     `displayName` for combined tracks, unaudited so far.

3. **Do nothing / won't fix.** Not viable — this makes combined tracks silently undeletable from
   the UI, which is a functional regression, not a cosmetic nit.

**Recommendation: Option 1.** It fixes the actual reported bug (button missing, button looking
small) directly at its root cause with a minimal, contained diff, and doesn't touch what's
persisted to storage. Option 2's storage-format question is a real, separate design decision
(how should a combined track's name look?) that's better made deliberately later if it turns out
to matter, not bundled into a bug fix for an unrelated layout issue.

## Decision

User approved a variant of Option 1, refined during discussion: rather than only truncating the
combined track's long joined title, also show each merged track's original name as its own
one-line (ellipsized) subtitle row, in addition to the existing "imported date · distance" line
(not replacing it — user's explicit choice when asked). This requires persisting the individual
constituent track names, not just the already-joined `displayName` string, since parsing them back
out of the joined string would be lossy (a source filename could itself contain `" + "`).

Final design:
- `core:storage`'s `CombinedTrackMetadata` gains `trackNames: List<String>`, populated at
  `combineSelected` time from each source track's own `displayName` (not derived from the joined
  string). For backward compatibility with combined tracks persisted before this field existed,
  its default value is computed from the existing `displayName` (`displayName.split(" + ")`) —
  lossy only in the rare case a source filename itself contained that separator, same tradeoff the
  reporter's original joining already had.
- `RecentTrackUiItem` gains `mergedTrackNames: List<String>` (empty for `Single` tracks).
- `RecentTrackRow`: the leading content gets `Modifier.weight(1f)` so the trailing delete
  `IconButton` always keeps its full width regardless of title length; the title and the existing
  date/distance `Text` both get `maxLines = 1, overflow = TextOverflow.Ellipsis`; for combined
  tracks, one additional one-line-ellipsized `Text` per entry in `mergedTrackNames` is rendered
  between the title and the date/distance line.

## What landed

- `core/storage/.../TrackMetadata.kt`: `CombinedTrackMetadata` gained `trackNames: List<String>`,
  defaulting to `displayName.split(" + ")` for backward compatibility with metadata persisted
  before this field existed.
- `core/storage/.../TracksStorageTest.kt`: extended the round-trip test to pass distinct
  `trackNames` (independent of `displayName`'s format, proving it's genuinely persisted rather
  than re-derived), plus a new test asserting the legacy-JSON default splits correctly.
- `feature/wayprint/ui/.../RecentsViewModel.kt`: `combineSelected` now populates `trackNames`
  directly from each source track's own `displayName`; `toUiItem()` surfaces it as
  `RecentTrackUiItem.mergedTrackNames` (empty for `Single` tracks).
- `feature/wayprint/ui/.../RecentsUiState.kt`: `RecentTrackUiItem` gained `mergedTrackNames`.
- `feature/wayprint/ui/.../RecentsScreen.kt`: `RecentTrackRow`'s leading content now has
  `Modifier.weight(1f)`, guaranteeing the trailing delete `IconButton` always keeps its full
  width; the title and the date/distance line are both capped to `maxLines = 1,
  overflow = TextOverflow.Ellipsis`; combined tracks render one additional one-line-ellipsized
  `Text` per entry in `mergedTrackNames`, between the title and the date/distance line.

**Verified:**
- `./gradlew :core:storage:testAndroidHostTest :core:storage:jvmTest
  :feature:wayprint:ui:testAndroidHostTest` — all pass, including the two new storage tests.
- `detekt`/`ktlintCheck` pass across the repo.
- `compileKotlinIosArm64`/`compileKotlinIosSimulatorArm64` succeed for `core:storage`,
  `feature:wayprint:ui`, and `composeApp` with the new code (iOS remains compile-only verified
  per the project's own established cap — no Mac in this environment).
- End-to-end on `Medium_Phone_API_36.1` (`gplay` debug): imported two tracks
  (`morning_run.gpx`, `evening_loop_around_the_lake.gpx`), combined them, and confirmed on the
  Recents list — the combined row's title truncates with an ellipsis
  (`evening_loop_around_the_lake.g…`), each source track's name renders as its own full one-line
  subtitle row, the date/distance line still shows (`Sep 5, 2026 · 52.3 km`), and the delete
  icon's real bounds (`uiautomator dump`, `[944,392][1007,455]`) sit fully inside the 1080px-wide
  screen — not clipped. Tapped delete, confirmed the dialog, confirmed deletion, and the row
  disappeared from the list correctly.

**Deliberately left out:** no new automated test covers the `RecentTrackRow` layout itself or
`RecentsViewModel.combineSelected`'s `trackNames` wiring — this project has no Compose UI test
harness set up (confirmed: no `createComposeRule`/`ui-test` dependency anywhere in the repo), and
adding one was out of scope for this bug fix. The storage-layer round-trip/legacy-default tests
plus the manual emulator verification above are the full verification story.

**Found, not fixed (logged to `docs/revisit.md` instead — unrelated to this bug):** while running
a full `./gradlew build` to verify this fix, discovered `core:gpx`'s and `core:storage`'s
`commonTest` source sets both fail to compile for `compileTestKotlinIosArm64`/
`compileTestKotlinIosSimulatorArm64` — pre-existing on `master` before this session (confirmed via
`git stash`), never actually exercised since M16's own Verify lines only ever compiled main
source sets for iOS. Left alone per this project's "a real problem outside the task goes in
writing, not fixed inline" rule.
