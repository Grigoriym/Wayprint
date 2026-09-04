# Wayprint implementation plan

This is the *why* document. For the *what next*, see `docs/CHECKLIST.md`. If the two ever
disagree, `CLAUDE.md` (root) wins over both.

## 1. Goal, restated

Turn a GPX file into a single, fixed-size (story: 1080×1920) styled route-art image, entirely
offline. Not an editor, not a tracking app. See root `CLAUDE.md` for the full concept, origin,
and MVP scope — this doc doesn't repeat it, only the decisions that shape *how* it gets built.

## 2. Reference projects

- **`../TaigaMobileNova`** — the original KMP/Compose/Koin/convention-plugin skeleton for this
  family of apps. 60+ modules, `build-logic/convention` with per-concern plugins
  (`KmpLibraryConventionPlugin`, `KmpLibraryComposeConventionPlugin`, `KmpDiConventionPlugin`,
  `KmpNetworkConventionPlugin`, `KmpSerializationConventionPlugin`,
  `KmpLibraryStabilityConventionPlugin`, `AndroidApplicationConventionPlugin`), Koin via
  koin-annotations (KSP), F-Droid/Play via a `STORE` flavor dimension. Its `uikit` is coupled to
  Taiga's own feature/domain modules — reference for *pattern* only, not portable as a module.
- **`../wallosmobile`** — a newer, cleaner fork of the same skeleton (`wallosmobile.*` plugin
  ids). This is the actual base for Wayprint's `build-logic`, since it already incorporates
  fixes made after Taiga. Layered module tree: `core:*`, `feature:*:{data,domain,dto,mapper,ui}`,
  `utils:*`, plus `uikit`, `strings`, `testing`, `benchmark`, `detekt-rules`.
  `docs/IMPLEMENTATION_PLAN.md` + `docs/CHECKLIST.md` there is the direct model for this pair of
  docs — same workflow, scaled down for a much smaller app.
- **`../agentic-grappim`** — shared Claude Code skills, symlinked (never copied) into
  `.claude/skills/<name>`. Also holds `templates/CLAUDE.md.template`, a skeleton this project's
  `CLAUDE.md` should adopt (working agreements, settled-decisions table, reference-projects
  section) without losing its existing Concept/Origin/MVP-scope/Architecture/Naming content.
- **`/home/gregory/claude/wanderwege/elbe route/scripts/gpx_route_art.py`** — the working
  reference implementation of the route math (haversine, Ramer–Douglas–Peucker simplification,
  equirectangular projection, `day_palette()`). `core:gpx` ports this, not reinvents it.

## 3. Why wallosmobile over Taiga as the build-logic base

wallosmobile is Taiga's own lessons-learned iteration: same convention-plugin shape, but with a
single edit point for KMP targets (`KmpConfiguration.kt`) and a cleaner `feature:*` layer split.
Copying it and renaming (`wallosmobile.*` → `wayprint.*`, package `com.grappim.wallosmobile.
buildlogic` → `com.grappim.wayprint.buildlogic`) gets the refined version for free instead of
re-deriving those fixes against Taiga's older skeleton.

## 4. Why a lean MVP module layout, not the full layered split

wallosmobile's `feature:*:{data,domain,dto,mapper,ui}` split exists because those apps have a
network layer and multiple data sources to map between. Wayprint's MVP has neither (GPX file in,
`Bitmap` out, no backend, no persistence) — building `data`/`dto`/`mapper` layers with nothing
real in them is speculative structure the CLAUDE.md instructions call out to avoid. So:

| Module | Role | Notes |
|---|---|---|
| `androidApp` | Android entry point | thin, same role as in Taiga/wallosmobile |
| `composeApp` | DI root, shell, nav, shared CMP entry | |
| `core:gpx` | GPX parsing, RDP simplification, equirectangular projection, day palette | pure Kotlin, no Android deps — ported from `gpx_route_art.py` |
| `core:storage` | persists saved tracks (raw GPX bytes + label overrides + color-scheme id + display name/date/distance), one per id | `File`-based, not `Context`-based — scaffolded in M7.4 as a single draft, generalized to N tracks in M9.2 |
| `core:navigation` | `Navigator`/`NavigationState`/`toEntries` — the back-stack shell every screen navigates through | scaffolded in M9.1, ported from `../wallosmobile`; single-section (no drawer) shape |
| `feature:wayprint:domain` | route model, style presets, label collision-avoidance layout | the one real "domain" concern this app has |
| `feature:wayprint:ui` | Compose Canvas renderer, import flow, export/share | |
| `uikit` | Theme/Colors/Dimens/Typography + a few shared widgets | built fresh for Wayprint; Taiga's/wallosmobile's `uikit` is coupled to their own feature modules |
| `strings` | CMP string resources | scaffolded now for infra consistency |
| `testing` | hand-written fakes | no mocking library, matching wallosmobile's convention |

Deferred until there's a concrete need (not scaffolded in M0):
- `core:network` — MVP is offline; add alongside `KmpNetworkConventionPlugin` if/when Health
  Connect or Strava OAuth import lands (growth roadmap, not MVP).
- `benchmark` — the `android-baseline-profile` skill covers this when there's a cold-start path
  worth profiling.
- `detekt-rules` (custom rules) — start with build-logic's default `Quality.kt` rule set.

## 5. Module → convention-plugin table

| Module | Plugins applied |
|---|---|
| `core:gpx` | `wayprint.kmp.library`, `wayprint.kmp.library.stability` |
| `core:storage` | `wayprint.kmp.library`, `wayprint.kmp.serialization` |
| `core:navigation` | `wayprint.kmp.library`, `wayprint.kmp.library.compose` |
| `feature:wayprint:domain` | `wayprint.kmp.library`, `wayprint.kmp.library.stability` |
| `feature:wayprint:ui` | `wayprint.kmp.library`, `wayprint.kmp.library.compose`, `wayprint.kmp.di`, `wayprint.kmp.serialization` (from M9 — its `NavKey` routes are `@Serializable`) |
| `uikit` | `wayprint.kmp.library`, `wayprint.kmp.library.compose` |
| `strings` | `wayprint.kmp.library`, `wayprint.kmp.library.compose` (CMP string resources need the compose resource pipeline) |
| `testing` | `wayprint.kmp.library` |
| `composeApp` | `wayprint.kmp.library`, `wayprint.kmp.library.compose`, `wayprint.kmp.di` |
| `androidApp` | `wayprint.android.application` |

`wayprint.kmp.serialization` is applied only to modules that actually (de)serialize something —
`core:storage` (M7.4) is the first; `core:gpx` would also need it if/when style presets move from
hardcoded Kotlin to a serialized format.

## 6. Koin

Same shape as Taiga/wallosmobile: koin-annotations (KSP-based, not manual `module { }` DSL).
Each DI-bearing module declares `@Module @ComponentScan("com.grappim.wayprint.<module>") class
<Module>Module`. `composeApp` aggregates everything in one `Koin.kt`:
`@Module(includes = [...]) @Configuration @ComponentScan("com.grappim.wayprint") class
AppModule` plus `@KoinApplication object KoinApp`. `expect class PlatformComponentModule` for
anything platform-specific (e.g. the file picker / share-intent entry point, `MediaStore` access).

## 7. Flavors and distribution

`STORE` flavor dimension, `GPLAY` / `FDROID`, `.fdroid` applicationIdSuffix — same as Taiga.
Given the MVP has no proprietary deps (no Crashlytics, no in-app updates yet), the two flavors
likely start identical in code and differ only in manifest/store metadata; add
`gplayImplementation(...)` gating only when a real Play-only dependency shows up. `fastlane/`
skeleton copied from wallosmobile/Taiga and adapted (package name, store listing text,
screenshots later).

## 8. Phased roadmap

Mirrors root `CLAUDE.md`'s "Next steps for the first working session" and "Growth roadmap"
sections, expanded into milestones — see `docs/CHECKLIST.md` for the step-by-step breakdown.

1. **M0** — repo/module scaffolding (build-logic port, settings.gradle.kts, version catalog,
   empty module skeletons, agentic-grappim skill symlinks, CLAUDE.md template merge).
2. **M1** — `core:gpx`: port GPX parsing/RDP/projection/palette, with unit tests proving numeric
   parity against `gpx_route_art.py`.
3. **M2** — `uikit`: theme, typography, base widgets.
4. **M3** — `feature:wayprint:domain`: route model, style presets, label layout/collision
   avoidance (the main non-trivial engineering piece).
5. **M4** — `feature:wayprint:ui`: Canvas renderer for one hardcoded story-size preset.
6. **M5** — import (file picker/share-intent) + export (`Bitmap` → `MediaStore`/share sheet)
   end-to-end.
7. **M6** — F-Droid/Play flavors, fastlane, CI.
8. **M7** — editable canvas: drag labels, pick a color scheme, undo, with edits persisted as a
   draft that survives an app kill. First growth-roadmap item pulled off the backlog into an
   actual milestone (Android-only, per the user — other platforms/aspect-ratio templates stay
   backlog).
9. **M8** — start over: an action that clears the persisted draft and returns to the import
   screen, with a confirmation dialog first (no undo across a draft clear). Second
   growth-roadmap/backlog item pulled into a milestone, from a user-reported gap after M7.4
   shipped persisted drafts.
10. **M9** — recent tracks: a tracks list start screen replacing the single-draft model, so every
    GPX import is saved as its own track instead of overwriting the one draft. Introduces
    `core:navigation` (deferred since M0 for exactly this trigger — the app's first second
    screen) and generalizes `core:storage`'s `DraftStorage` into a keyed `TracksStorage`. Third
    growth-roadmap/backlog item pulled into a milestone; supersedes M8's "Start over" action
    (see `docs/CHECKLIST_ARCHIVE.md`'s M9 shared context).
11. **M10** — freeform labels: generalizes the label model from a fixed regenerated set
    (Start/Finish/distance) to an arbitrary, user-editable set — add/remove/place anywhere.
    Prerequisite for M11, not itself scoped to multi-track. Fourth growth-roadmap/backlog item
    pulled into a milestone, during the M11 investigation below.
12. **M11** — combine multiple tracks: a new persisted entity backed by N existing tracks' GPX
    bytes, sharing one projection, reusing M10's freeform labels (default global Start/Finish
    only) — editable exactly like a single track. Fifth growth-roadmap/backlog item pulled into
    a milestone.

MVP roadmap (M0–M11) is complete as of M11.4. Remaining growth roadmap (not milestones yet,
backlog only): more input sources (Health Connect, Strava OAuth, on-device recording — a
distinct scope jump), multiple layout templates (poster, square, story — includes aspect-ratio
picking, deferred out of M7), iOS/Desktop targets for `core:gpx` and the Compose `Canvas`
renderer.

## 9. Open decisions / risks

- ~~**`core:logger` / `detekt-rules` hard dependencies copied in from wallosmobile**~~ —
  **Resolved in M0.2** (surfaced sooner than expected: `composeApp` applying
  `wayprint.kmp.library.compose` already needed this fixed, not just M0.3's modules). Took
  option (b): stripped the unconditional `implementation(project(":core:logger"))` line from
  `KmpConfiguration.kt`'s `configureKmp()` and the `detektPlugins(project(":detekt-rules"))`
  line from `Quality.kt`'s `configureLinting()`. M0.2 also found the same convention-plugin
  module-doesn't-exist-yet problem for `:testing` (guarded with a `findProject(":testing") !=
  null` check instead, since that module *is* still planned for M0.3) and two related gaps from
  M0.1's root scaffolding: no root `build.gradle.kts` (AGP/Compose/Koin/Kover need `apply
  false`-ing there so their DSL types are loadable before a convention plugin references them)
  and no `config/detekt/detekt.yml` / `config/compose/stability_config.conf` (copied from
  wallosmobile; detekt.yml trimmed of its `WallosMobile:` custom-rule section). See
  `docs/CHECKLIST_ARCHIVE.md`'s M0.2 note for the full list.
- ~~**Collision-avoidance algorithm**~~ — **Resolved at M3 step-break-down.** Greedy
  dodge-by-priority, not force-directed: each label gets a fixed anchor point (a route endpoint,
  or the route's bounding-box center for the distance label) plus an ordered list of candidate
  offset placements (compass-style — right/left/above/below, matching how the hand-tuned Elbe
  labels were each nudged in one clear direction with a `text-anchor` of `start`/`end` to dodge
  the line — see `elbe-story-actual.html`'s town-label `<text>` elements). Labels are placed one
  at a time in a fixed priority order (Start, Finish, then the distance label); each tries its
  candidates in order and takes the first whose bounding box doesn't overlap an already-placed
  label or the canvas edge, falling back to its last candidate (accepting the overlap) if none
  clear. Force-directed placement was rejected: MVP only ever has 3 fixed labels (see the label-
  scope decision below), so a physics simulation is solving a much harder problem than this one
  actually is — CLAUDE.md's "Simplicity first" agreement. The Elbe reference's own halo (`paint-
  order: stroke` behind each label) is why label-vs-route-line overlap doesn't need to be part of
  the collision check at all — port that halo technique in M4's renderer, not a geometry check
  here.
- **M3 label scope** — user decided (2026-09-02): Start marker, Finish marker, total distance
  only. No date label in MVP — `core:gpx`'s `TrackPoint` (M1) carries no timestamp
  (`parse_track()` never did either), and adding one would reopen M1's parser scope. CLAUDE.md's
  MVP-scope line "distance/date text" is satisfied partially (distance only); revisit date as its
  own small step later if wanted, ported from a GPX `<metadata><time>` or first `<trkpt>`'s
  `<time>` element.
- **KMP target list at launch** — Android is the only real target for MVP (per CLAUDE.md, iOS/
  Desktop are v2+ bets). Decide in M0 whether `core:gpx`/`uikit` should still declare iOS/Desktop
  source sets from day one (cheap to leave the door open) or Android-only until there's a second
  platform to actually build against.
- **Style presets format** — hardcoded Kotlin objects for M4's single preset is fine; revisit if/
  when "small number of fixed presets" (CLAUDE.md) needs to grow past what's comfortable as code.
- **M4 label/marker halo text — Android-only** — resolved at M4 step-break-down (2026-09-02).
  `feature:wayprint:ui` ports the Elbe reference's `paint-order: stroke` label legibility halo via
  `drawContext.canvas.nativeCanvas` + two `android.graphics.Paint`s (stroke, then fill) — see
  `docs/CHECKLIST_ARCHIVE.md`'s M4 shared context. Unlike M1's `javax.xml.parsers`/`BigDecimal` calls
  (JVM-standard, "Android-only" only because no other KMP target is configured yet),
  `android.graphics.Paint` is genuinely Android-only. Revisit when/if an iOS/Desktop target is
  added — a Compose Multiplatform-portable text-halo approach would replace this.
- **M4 demo route data** — resolved at M4 step-break-down (2026-09-02). GPX import (M5) doesn't
  exist yet, so M4.3's end-to-end `composeApp` wiring feeds `buildWayprintLayout` a short
  hand-written GPX string embedded in code (not the M1 fixture, not a real import) — provisional,
  same spirit as M0.2's placeholder text/M0.4's `GreetingProvider`. M5 replaces the call site with
  the real file-picker/share-intent input.
- **agentic-grappim README edit** (adding Wayprint to its project list) touches a shared repo
  used by other apps — confirm with the user at the M0 step that does this, not by default.
- **M5 import/export decisions** — resolved at M5 step-break-down (2026-09-02). Four decisions,
  see `docs/CHECKLIST_ARCHIVE.md`'s M5 shared context for the full reasoning:
  - GPX picker uses `ActivityResultContracts.GetContent("*/*")`, not SAF `OpenDocument` with a mime
    filter — GPX has no reliable, universally-tagged MIME type across Strava/Komoot/OsmAnd, so a
    mime filter would as often hide the real file as show it. A bad pick is validated by attempting
    the parse (same `Error` state a malformed GPX hits), not by inspecting mime/extension first.
  - Share-intent (`MainActivity`'s M5.2 intent-filter) matches concrete mime types
    (`application/gpx+xml`, `application/octet-stream`) rather than `*/*` or `pathPattern` matching
    — revisit if a real sharing app doesn't surface Wayprint in its share sheet.
  - `MediaStore` save (M5.4) uses `MediaStore.Images.Media.insertImage(...)`, not manual
    `ContentValues`/`RELATIVE_PATH` — works unmodified across `minSdk=24..compileSdk=37` at the
    cost of no custom "Wayprint" subfolder (default Pictures collection instead); pre-29 needs a
    runtime `WRITE_EXTERNAL_STORAGE` (`maxSdkVersion="28"`) request the 29+ scoped-storage path
    doesn't. `minSdk` stays 24 (matches `../wallosmobile`/`../TaigaMobileNova`) rather than being
    raised to 29 just to drop that legacy path.
  - M5 is where `WayprintViewModel` (MVVM, per root `CLAUDE.md`'s planned architecture) gets its
    first real use — `jetbrains-lifecycle-viewmodel-compose`/`koin-compose-viewmodel` have been in
    the version catalog unused since M0.1's seed. It lives in `feature:wayprint:ui` (module table
    in §4 already assigns that module "import flow, export/share," and it already applies
    `wayprint.kmp.di` per §5) — `composeApp` stays a thin DI-root/shell hosting the finished screen.
    No repository/use-case layer: §4's lean-MVP module layout already ruled that structure out.
- **M6 distribution scope** — resolved at M6 step-break-down (2026-09-02), see
  `docs/CHECKLIST_ARCHIVE.md`'s M6 shared context for the full reasoning. Three user decisions:
  - M6.1 renames the ported wallosmobile-named signing config to Wayprint's own (`wayprint_*.jks`,
    `WAYPRINT_*` env vars) and fixes the *debug* F-Droid signing gap flagged since M0.2/M0.3, but
    does not generate real `gplay`/`fdroid` **release** keystores — those are the app's permanent
    publishing identity, deferred until the user is actually ready to publish.
  - M6.3's `ci.yml` is a build/test/lint gate only: no Codecov upload (no Codecov project exists
    for this repo) and no per-flavor Android/Compose lint step, unlike wallosmobile's `ci.yml`.
  - `guardrails.yml` (wallosmobile's own commit-message tripwire convention, not Wayprint's) and
    `release-prepare.yml`/`release.yml`/`release-finalize.yml` (version-bump + `fastlane supply`
    Play/F-Droid publish automation) are out of scope for all of M6 — there's no real release
    keystore or store listing to publish yet (see the first bullet above). Revisit as its own
    milestone when the app is ready to actually ship to a store.
- **M7 editable-canvas scope** — resolved at M7 step-break-down (2026-09-02), see
  `docs/CHECKLIST_ARCHIVE.md`'s M7 shared context for the full reasoning. Three user decisions:
  - Aspect-ratio picking stays out of M7 — it needs multiple canvas-shape presets, which is really
    the backlog's "multiple layout templates" item, not this one. M7 stays scoped to drag-to-
    reposition labels, picking a color scheme, and undo, all within the existing single
    `DEFAULT_STORY_PRESET` canvas shape.
  - Color picking is choosing from a small fixed list of preset color schemes, not a free-form
    picker — same "small number of fixed presets" spirit as CLAUDE.md's MVP-scope line, avoiding
    illegible user-mixed combinations. `core:gpx`'s `dayPalette()` (ported in M1.3, unused ever
    since — no per-day multi-track coloring exists yet) is the color source: its 5 muted hues
    become 5 line-color variants against `DEFAULT_STORY_PRESET`'s existing fixed
    background/text colors, giving M7 its preset list without inventing new palette values.
  - Edits (dragged label positions, chosen color scheme) persist as a draft surviving a full app
    kill/relaunch, not just rotation. The draft stores the *raw GPX bytes*, not the source
    `content://` `Uri` — a share-intent-provided `Uri`'s grant often can't be persisted at all,
    and even a file-picker `Uri`'s persisted-permission grant is an extra failure mode a raw-bytes
    copy avoids entirely. This is what finally gives `core:storage` (deferred since M0, §4) a
    concrete need, and pulls in `wayprint.kmp.serialization` (deferred since M0, §5) for the
    stored draft's format.
- **M8 start-over scope** — resolved at M8 step-break-down (2026-09-03), see
  `docs/CHECKLIST_ARCHIVE.md`'s M8 shared context for the full reasoning. One user decision: starting over
  confirms first (a dialog) rather than discarding the draft immediately on tap, since a draft
  clear has no undo. Stays within the existing single-draft model — saving the discarded draft
  somewhere (a recent-tracks list) is a separate, unscoped backlog item, not part of M8.
- **M9 recent-tracks scope** — resolved at M9 step-break-down (2026-09-03), see
  `docs/CHECKLIST_ARCHIVE.md`'s M9 shared context for the full reasoning. Four user decisions: every
  import auto-saves (no explicit "Save" step); M8's confirm-then-clear "Start over" is replaced
  outright by plain back-navigation to the tracks list (no longer destructive, since the track is
  already saved); the list is uncapped, manual delete only (with its own confirm dialog, the
  destructive action M8's used to be); the pre-M9 single draft file on disk is not migrated —
  dropped, since the app has no released users yet.
