# CLAUDE.md

Wayprint — an Android app (Kotlin Multiplatform, KMP-first) that turns a GPX track into a
styled, shareable route-art image. No code exists yet — this file is the seed for the session
that scaffolds it.

## Concept

Input a GPX file → output a static styled image of the route (MVP target: a fixed-size
story-style image, e.g. 1080×1920). Single-purpose utility app, same sizing/spirit as the
user's other two FOSS apps (`../TaigaMobileNova`, `../wallosmobile`), not a full editor.

## Origin

Grew out of a personal project: a bike-trip itinerary page
(`/home/gregory/claude/wanderwege/elbe route/`) that hand-built a route-art "Instagram story"
image (`elbe-story.html`) for a real trip. That project's design iterations already answered
several questions this app would otherwise have to re-litigate from scratch — reuse them rather
than rediscovering:

- **Flat color per track/day**, not an elevation gradient — a gradient + elevation-silhouette
  graph was tried and found ambiguous/confusing, then removed.
- **Labels placed directly on the route art** (distance, day, etc.), not a separate legend —
  positions hand-tuned to dodge overlaps. For an app this has to become an automatic
  collision-avoidance layout problem — this is the main non-trivial engineering piece, not the
  rendering itself.
- **No live basemap** for the story-style render — a real Leaflet/OSM-tile background was tried
  and looked wrong for this format; the hand-projected pure vector/line-art look is what stuck.
  Don't reintroduce a live-tile background as the default without a fresh design discussion.
- The route math (haversine, Ramer–Douglas–Peucker simplification, equirectangular projection,
  day color palette) is already implemented once, in Python, at
  `/home/gregory/claude/wanderwege/elbe route/scripts/gpx_route_art.py`. Port the same approach
  to Kotlin for `core:gpx` rather than reinventing it — it's a useful reference implementation
  and its tolerance/palette values (`day_palette()`) are a known-good starting point.

## MVP scope (decided)

- **Input**: GPX file only (file picker / share-intent from Strava, Komoot, OsmAnd, etc.).
  Other sources (Health Connect, Strava OAuth, on-device recording) are explicitly deferred —
  on-device recording in particular turns this into a tracking app, a much bigger scope jump.
- **Output**: one fixed-size static image (start with a single preset, story-sized), exported
  to a `Bitmap` and saved/shared via `MediaStore` / share sheet. No live/editable canvas yet.
- **Styling**: a small number of fixed presets (flat line color, background, distance/date
  text), not a full customization UI.
- **No network, no account** — fully offline, matching the FOSS/privacy posture of the user's
  other apps.

## Planned architecture

Match the user's existing template (`TaigaMobileNova`, `wallosmobile`): Kotlin Multiplatform,
Compose Multiplatform, MVVM + Clean Architecture, Koin DI, modular (`core`/`feature`/`uikit`),
F-Droid + Play flavors, shared `build-logic` convention plugins, fastlane.

- `core:gpx` — GPX parsing, RDP simplification, equirectangular projection, day/segment color
  palette. Pure Kotlin, no Android deps — portable and unit-testable, ported from
  `gpx_route_art.py` above.
- `feature:wayprint` — Compose UI: import flow, Canvas rendering of the route art, export/share.
- Reuse vs. fork `build-logic`/`uikit` from an existing app is an **open question** — decide at
  scaffold time whether to extract shared convention plugins into `agentic-grappim` or start
  fresh and copy what's needed.

## Growth roadmap (v2+, not MVP)

- Editable canvas: drag labels, pick colors/aspect ratio, undo — before this, MVP is "generate
  and export," not "design tool."
- More input sources: Health Connect / Strava OAuth import; on-device live recording (scope
  jump — treat as a distinct milestone, not an MVP add-on).
- Multiple layout templates (poster, square post, story) once one layout is proven.
- KMP-first is partly a bet that `core:gpx` and the Compose Multiplatform `Canvas` rendering
  ship to iOS/Desktop later at low incremental cost, the same jump Taiga already made.

## Naming

App name: **Wayprint** ("print of your way" — chosen over `Routecard`/`Trackframe`; "way"
leans hiking/waymarking-flavored rather than cycling-only, "print" signals a tangible art piece
rather than a throwaway story image).

## Next steps for the first working session

1. Decide `build-logic`/module scaffolding approach (fresh vs. shared with existing apps).
2. Scaffold the KMP project skeleton (`androidApp`, `composeApp` or equivalent, `core:gpx`,
   `feature:wayprint`).
3. Port `gpx_route_art.py`'s GPX parsing / RDP / projection logic to Kotlin in `core:gpx`,
   with unit tests.
4. Build the single story-size Canvas renderer and export/share flow end-to-end for one
   hardcoded style preset before adding any customization.
