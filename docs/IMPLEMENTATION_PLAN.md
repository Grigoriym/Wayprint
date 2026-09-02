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
| `feature:wayprint:domain` | route model, style presets, label collision-avoidance layout | the one real "domain" concern this app has |
| `feature:wayprint:ui` | Compose Canvas renderer, import flow, export/share | |
| `uikit` | Theme/Colors/Dimens/Typography + a few shared widgets | built fresh for Wayprint; Taiga's/wallosmobile's `uikit` is coupled to their own feature modules |
| `strings` | CMP string resources | scaffolded now for infra consistency |
| `testing` | hand-written fakes | no mocking library, matching wallosmobile's convention |

Deferred until there's a concrete need (not scaffolded in M0):
- `core:storage` — no persistence in MVP.
- `core:navigation` — single-screen MVP; add when there's a second screen worth routing between.
- `core:network` — MVP is offline; add alongside `KmpNetworkConventionPlugin` if/when Health
  Connect or Strava OAuth import lands (growth roadmap, not MVP).
- `benchmark` — the `android-baseline-profile` skill covers this when there's a cold-start path
  worth profiling.
- `detekt-rules` (custom rules) — start with build-logic's default `Quality.kt` rule set.

## 5. Module → convention-plugin table

| Module | Plugins applied |
|---|---|
| `core:gpx` | `wayprint.kmp.library`, `wayprint.kmp.library.stability` |
| `feature:wayprint:domain` | `wayprint.kmp.library`, `wayprint.kmp.library.stability` |
| `feature:wayprint:ui` | `wayprint.kmp.library`, `wayprint.kmp.library.compose`, `wayprint.kmp.di` |
| `uikit` | `wayprint.kmp.library`, `wayprint.kmp.library.compose` |
| `strings` | `wayprint.kmp.library`, `wayprint.kmp.library.compose` (CMP string resources need the compose resource pipeline) |
| `testing` | `wayprint.kmp.library` |
| `composeApp` | `wayprint.kmp.library`, `wayprint.kmp.library.compose`, `wayprint.kmp.di` |
| `androidApp` | `wayprint.android.application` |

`wayprint.kmp.serialization` is applied only to modules that actually (de)serialize something —
none do yet at M0/M1; add it to `core:gpx` if/when style presets move from hardcoded Kotlin to a
serialized format.

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

Growth roadmap (not milestones yet, backlog only): editable canvas (drag labels, colors, aspect
ratio, undo), more input sources (Health Connect, Strava OAuth, on-device recording — a distinct
scope jump), multiple layout templates (poster, square, story), iOS/Desktop targets for
`core:gpx` and the Compose `Canvas` renderer.

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
  `docs/CHECKLIST.md`'s M0.2 note for the full list.
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
- **agentic-grappim README edit** (adding Wayprint to its project list) touches a shared repo
  used by other apps — confirm with the user at the M0 step that does this, not by default.
