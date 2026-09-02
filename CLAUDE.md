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

## Working agreements

**Tradeoff:** these bias toward caution over speed. For trivial tasks, use judgment.

### Think before coding

Don't assume. Don't hide confusion. Surface tradeoffs.

- State assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.
- **An answer is not an instruction to act.** If the user states a preference or decision that a
  later, not-yet-requested step will need, record it for when that step is asked for — don't treat
  it as authorization to run the step now.

### Simplicity first

Minimum code that solves the problem. Nothing speculative.

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask: "would a senior engineer say this is overcomplicated?" If yes, simplify.

### Surgical changes

Touch only what you must. Clean up only your own mess.

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor what isn't broken. Match existing style, even if you'd do it
  differently.
- Don't add UI or navigation that wasn't asked for.
- Remove imports, variables and functions **your** change orphaned. Leave pre-existing
  dead code alone — mention it instead.

**Every changed line should trace directly to the request.**

### Don't break production in favor of tests

Production code must not be shaped by testing needs. If a code path is flaky or can't be
observed deterministically as written, fix or remove the *test* — don't add a seam,
injectable parameter, or abstraction to production code purely so a test can control
it. This holds even when the change is small, additive, and provably safe (e.g. a
defaulted constructor parameter verified not to affect the DI graph) — the question is
not "is this change safe," it's "does this belong in production code at all."

Prefer, in order: (1) a lower-level test that already covers the behavior
deterministically without the racy synchronization; (2) simplify the test to avoid it;
(3) delete the test and say so plainly, rather than leaving a known flake undocumented.

Always ask before adding any production-code testability seam, even a well-verified
one.

### Determinism over process

If a task has one correct, computable answer, use a tool for it. Don't ask the agent to
follow a fixed procedure by hand.

- A checksum, a sort order, a date calculation, a schema check: write a script or a
  hook. Call it as a tool.
- An agent following prose steps can skip a step, or get one wrong. A script cannot.
- Reserve the agent's judgment for what needs judgment: ambiguous input, a plan, a
  choice between options.
- Writing a new skill: find a step that says "always do X the same way." Replace it
  with a tool call, not a longer instruction.

Ask: "does this step have one right answer, computable without judgment?" If yes,
write the tool, not the instruction.

### Goal-driven execution

Turn a task into a verifiable goal — "fix the bug" becomes "write a failing test, then
make it pass". For multi-step work, state the steps with a check each, then loop until
they pass.

### A real problem outside the task goes in writing

Write it into `docs/revisit.md` and keep going. Not fixed inline — that makes the
diff unreviewable. Not dropped. And **not just mentioned in chat: chat is not
persistence.** Give the entry enough evidence (`file:line`, or a link) that a cold
session can act on it without re-deriving anything.

### Friction goes in writing too

The rule above is for problems in the **code**. This one is for friction in the
**tooling**, and it is the one that silently never gets reported: a guessed URL that
404s, an auth error on something another tool already reaches, a command that needed
different quoting, a check that confidently returned the wrong answer. The reflex is to
route around it and say nothing.

Add a line to `docs/frictions.md` before moving on — **create the file if it isn't there**,
that is not a decision worth pausing over:

```markdown
# Frictions

Tooling friction hit during work, newest last. One line each. Promoted or fixed
entries get deleted — see /finalize.
```

One line, past tense, naming the tool and the surprise. Don't stop working to write it
and don't editorialise. **This is for what you routed around without mentioning** — not
for failures you were going to report anyway.

At the end of the task, **read the file** and list what you added, with a count, even when
the count is zero. Read it rather than recalling it: small friction is gone from recall by
then, which is the whole reason the file exists. And a silent miss must not look like a
smooth run.

The same friction three times is a fix, not a fourth line — a permission entry, a line in
this file, or a skill. `/finalize` is where that promotion happens.

## Close-out

At the end of each checklist step, without being asked:

1. Run the **`/finalize` skill** if the step taught something non-obvious — the plan didn't
   know it going in, and this is where it gets written down instead of dying with the context.
2. Commit the step's changes (one commit per checklist step, per the working agreements above).
3. **Push to `master` directly.** There is no dev branch and no PR workflow yet — the user has
   said explicitly, repeatedly, that push-to-master-every-task is the standing instruction until
   they say otherwise. Do not hold a commit back "until told to push"; that is not a real
   distinction here — commit and push are the same action. **Only** once the user says a dev
   branch now exists does this switch to feature branches + PRs, and even then only for the
   scope they name.

Session-scoped work still gets confirmed before pushing (see "Executing actions with care" in
the system prompt) — a force-push, a rewrite of already-pushed history, anything destructive.
A normal fast-forward push of a normal commit is not that; it's the default, not an ask.

## Settled decisions

Weighed and declined — don't re-propose these.

| Not used | Instead | Why |
|---|---|---|
| Elevation gradient + silhouette graph | Flat color per track/day | Tried in the Elbe prototype; found ambiguous/confusing, removed. See Origin above. |
| Separate legend for route labels | Labels placed directly on the route art | Prototype's design iteration; needs an automatic collision-avoidance layout in the app (main non-trivial engineering piece, not the rendering itself). |
| Live Leaflet/OSM-tile basemap | Hand-projected pure vector/line-art rendering | Tried in the Elbe prototype; looked wrong for the story format. Don't reintroduce as the default without a fresh design discussion. |
| Health Connect / Strava OAuth import, on-device recording | GPX file only (file picker / share-intent) | Deferred past MVP; on-device recording in particular turns this into a tracking app, a much bigger scope jump. |
| `KmpNetworkConventionPlugin` (ported from `wallosmobile`'s `build-logic`) | Skipped entirely | No network in MVP (IMPLEMENTATION_PLAN.md §4). |

## Reference projects

Read these rather than guessing; the conventions here are ported from them.

- `../TaigaMobileNova` — module structure, Koin DI graph shape (`KoinGraphTest`), KMP project
  scaffolding pattern.
- `../wallosmobile` — `build-logic` convention plugins, ported directly in M0.1; Android-only KMP
  module shape; `KoinGraphTest`'s `koin-test`/`.verify()` form, used in M0.4 since Wayprint, like
  wallosmobile, is Android-only.

**Trust their code over their docs.** Another project's `CLAUDE.md` can contradict its own
implementation — both of these repos have had exactly that. Note a drift here when you find one.
