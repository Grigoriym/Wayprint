# Wayprint checklist

**Current step:** No milestone scoped yet — M0–M16 (the full MVP roadmap, the second layout
template, the edit-toolbar decluttering, moving Android-only code out of `commonMain`, adding a
real Desktop/JVM target, and adding an iOS KMP target + Xcode scaffold) are complete and archived
to `docs/CHECKLIST_ARCHIVE.md`. M16's iOS work is unverified beyond compiling — this machine is
Linux, so the app can't actually be built, linked, or run here; ask the user to confirm on a real
Mac before trusting it end-to-end. Release CI/publish was explicitly deferred by the user
(keystores ready since M6.3) — pick that back up only when they say the app is ready to ship.
Next milestone (a third "poster" layout template, per the Backlog below) isn't scoped yet.

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

## Backlog (growth roadmap, not milestones yet)

- More input sources: Health Connect / Strava OAuth import; on-device live recording (its own
  milestone-scale scope jump when it happens, not an incremental add-on).
- A third layout template — poster — once M12 proves the two-template plumbing. Its own aspect
  ratio/size isn't decided yet; if it changes `canvasWidth` (not just height) from the 1080 every
  M12 template shares, M12's "constants don't need to scale" simplification (see M12 shared
  context) needs revisiting first.
- Color-scheme swatches (and any future style pickers) currently float as a permanent `TopEnd`
  overlay on the edit canvas — revisit if M13's toolbar decluttering makes that fixed position
  feel inconsistent, or once a poster template adds more style choices to pick from.
