# Wayprint checklist

**Current step:** M11.4 done. M0–M11 (the full MVP roadmap) are complete — moved to
`docs/CHECKLIST_ARCHIVE.md`. Nothing is queued next; the Backlog below is growth-roadmap ideas,
not yet broken down into milestones. Release CI/publish was explicitly deferred by the user
(keystores ready since M6.3) — pick that back up only when they say the app is ready to ship.

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

## Backlog (growth roadmap, not milestones yet)

- More input sources: Health Connect / Strava OAuth import; on-device live recording (its own
  milestone-scale scope jump when it happens, not an incremental add-on).
- Multiple layout templates (poster, square post, story) once the story layout is proven —
  includes aspect-ratio picking, deferred out of M7.
- iOS/Desktop targets for `core:gpx` and the Compose `Canvas` renderer.
