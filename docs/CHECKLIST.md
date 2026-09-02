# Wayprint checklist

**Current step:** M1.3

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

## M0 — repo/module scaffolding

- [x] **M0.1** — Copy `wallosmobile/build-logic/convention` into `wayprint/build-logic/
  convention`. Rename: plugin ids `wallosmobile.*` → `wayprint.*`, package
  `com.grappim.wallosmobile.buildlogic` → `com.grappim.wayprint.buildlogic`, and update
  `KmpConfiguration.kt`'s group/namespace to `com.grappim.wayprint`. Keep
  `KmpLibraryConventionPlugin`, `KmpLibraryComposeConventionPlugin`, `KmpDiConventionPlugin`,
  `KmpSerializationConventionPlugin`, `KmpLibraryStabilityConventionPlugin`,
  `AndroidApplicationConventionPlugin`. Skip `KmpNetworkConventionPlugin` (see
  IMPLEMENTATION_PLAN.md §4 — no network in MVP). Also root `settings.gradle.kts`
  (`pluginManagement { includeBuild("build-logic") }`), `gradle/libs.versions.toml` version
  catalog seeded from wallosmobile's (KMP/Compose/Koin/AGP/Kotlin versions — drop entries for
  deps Wayprint doesn't use yet, e.g. networking/serialization libraries), `gradle.properties`,
  `.editorconfig` (ktlint style config — without it, `android_studio`-style chained calls in
  `convention/build.gradle.kts` fail `ktlintCheck` under ktlint's default style), Gradle
  wrapper, and `build-logic`'s own `settings.gradle.kts`/`gradle.properties` (an included build
  needs these to resolve its own `../gradle/libs.versions.toml`).
  **Note:** originally split across M0.1 (just the directory copy/rename) and M0.2 (root
  scaffolding), but M0.1's own Verify line requires a root wrapper + settings.gradle.kts +
  version catalog to exist before `:build-logic:convention:build` can run at all — merged the
  two into one step so the step's Verify line is actually satisfiable on its own.
  **Note:** `git init` run here too (repo had no VCS history yet); default branch `master` to
  match `wallosmobile`'s convention.
  **Note:** found two latent issues while reading the copied source, left unfixed here (out of
  scope for a copy/rename step) but flagged in IMPLEMENTATION_PLAN.md §9 for M0.4/M1 to resolve:
  `KmpConfiguration.kt`'s `configureKmp()` unconditionally adds `project(":core:logger")` to
  every KMP module's `commonMain`, and `Quality.kt`'s `configureLinting()` unconditionally adds
  `detektPlugins(project(":detekt-rules"))` — neither module exists in Wayprint's planned module
  list, so the first module to apply `wayprint.kmp.library` will fail to resolve until this is
  addressed.
  **Verify:** `./gradlew :build-logic:convention:build` succeeds, and `./gradlew help` succeeds
  at the root with no modules declared yet beyond `build-logic`. Both confirmed green.

- [x] **M0.2** — `androidApp` + `composeApp` skeletons: `composeApp` applies
  `wayprint.kmp.library.compose` + `wayprint.kmp.di`, shows a single hardcoded "Wayprint" text
  screen; `androidApp` applies `wayprint.android.application` and launches it.
  **Note:** applying `wayprint.kmp.library.compose` to `composeApp` surfaced three more latent
  M0.1 scaffolding gaps beyond the core:logger/detekt-rules one already flagged (that one was
  fixed here too, taking IMPLEMENTATION_PLAN.md §9 option (b) — the unconditional
  `project(":core:logger")` and `detektPlugins(project(":detekt-rules"))` lines were stripped
  from `KmpConfiguration.kt`/`Quality.kt`, since neither module is in Wayprint's plan):
  (1) no root `build.gradle.kts` existed — without one applying the AGP/Compose/Koin/Kover
  plugins `apply false`, the first module to actually apply a convention plugin referencing
  their DSL types (`ApplicationExtension`, then the plain-string `org.jetbrains.kotlinx.kover`
  apply) hit `NoClassDefFoundError`/`Plugin ... not found` — added one mirroring wallosmobile's,
  scoped to only the plugins Wayprint's M0 modules actually use; (2) `config/detekt/detekt.yml`
  and `config/compose/stability_config.conf` didn't exist — `configureLinting()`/
  `configureComposeStabilityConfig()` reference them unconditionally — copied from wallosmobile
  (detekt.yml trimmed of its `WallosMobile:` custom-rule section, since `:detekt-rules` isn't
  scaffolded); (3) `configureTests()`'s unconditional `implementation(project(":testing"))` hit
  the same "module doesn't exist yet" problem — unlike (1)'s modules, `:testing` *is* still
  planned (M0.3), so this one got a `findProject(":testing") != null` guard instead of being
  stripped, so it starts working automatically once M0.3 scaffolds it. Also missing: root
  `local.properties` (`sdk.dir`), now added (gitignored, machine-local as usual).
  **Note:** `:androidApp:assembleDebug` (the flavor-aggregate task) fails on a clean checkout —
  it also builds `assembleFdroidDebug`, which the ported `AndroidApplicationConventionPlugin`
  force-signs (Variant API) with an F-Droid debug keystore + env-var password that were never
  Wayprint-specific to begin with (still named/expecting wallosmobile's, and M6 is where
  F-Droid/Play signing/secrets actually get set up) — verified against
  `:androidApp:assembleGplayDebug` instead, which uses AGP's own auto-generated debug signing
  and needs no secrets. Recorded in `docs/EMULATOR_TESTING.md` (new) for future sessions.
  **Verify:** `./gradlew :androidApp:assembleGplayDebug` succeeds (confirmed); installed via
  `adb install` and launched on a `Medium_Phone_API_36.1` emulator — screenshot confirmed the
  centered "Wayprint" text screen. `detekt`/`ktlintCheck` pass on `composeApp`, `androidApp`,
  `build-logic:convention`, and the root project.

- [x] **M0.3** — Empty module skeletons wired into `settings.gradle.kts`, each with only the
  convention plugins from IMPLEMENTATION_PLAN.md §5 applied and a placeholder file (no real
  code yet): `core:gpx`, `feature:wayprint:domain`, `feature:wayprint:ui`, `uikit`, `strings`,
  `testing`. Before ticking this done, resolve the `:core:logger` / `:detekt-rules` issue flagged
  in M0.1's note and IMPLEMENTATION_PLAN.md §9 — every module here applies
  `wayprint.kmp.library`, which will fail to resolve those unconditional project dependencies
  otherwise.
  **Note:** the `:core:logger` / `:detekt-rules` issue was already resolved in M0.2 (confirmed
  still fine here — every new module applies `wayprint.kmp.library`/`.compose`/`.di` cleanly).
  **Note:** a bare `package ...`-only placeholder file trips detekt's `EmptyKotlinFile` rule
  (`strings:detekt` failed on it first); each module's `Placeholder.kt` holds a single
  `internal object Placeholder` instead.
  **Note:** `./gradlew build` unmodified still fails — not from anything in this step, but from
  M0.2's already-flagged pre-existing gap: `androidApp`'s F-Droid debug signing and both stores'
  release signing configs point at wallosmobile-named keystores/secrets that were never set up
  for Wayprint (M6 scope). Verified instead with
  `./gradlew build -x :androidApp:assembleFdroidDebug -x :androidApp:assembleFdroidRelease
  -x :androidApp:assembleGplayRelease -x :androidApp:bundleFdroidDebug
  -x :androidApp:bundleFdroidRelease -x :androidApp:bundleGplayRelease`, which builds/checks
  every module including all six new ones, plus `androidApp`'s gplay debug variant end to end.
  **Verify:** `./gradlew build` (with the above exclusions for the pre-existing M6-scoped signing
  gap) succeeds across the whole project with all six new modules present but empty; each new
  module's `build`/`check` (detekt, ktlint, kover) also confirmed individually.

- [x] **M0.4** — Koin skeleton: `composeApp`'s `Koin.kt` with `@Module(includes = [...])
  @Configuration @ComponentScan("com.grappim.wayprint") class AppModule` and `@KoinApplication
  object KoinApp`, plus `expect class PlatformComponentModule` (actual on Android). No real
  bindings yet — this just proves the DI graph initializes.
  **Note:** followed TaigaMobileNova's exact shape rather than wallosmobile's (wallosmobile has
  no `PlatformComponentModule` at all): `AppModule`'s `@ComponentScan` alone reaches every
  definition since `composeApp` is Android-only today — no `includes` list needed yet, and
  `PlatformComponentModule` isn't manually included in it either, same as Taiga. An explicit
  `includes` list becomes necessary the moment a second (iOS/Desktop) target exists, since
  `@ComponentScan` doesn't reach across an iOS Native compilation — see the doc comment on
  `KoinGraphTest` in TaigaMobileNova for why.
  **Note:** the "trivial injected placeholder" is `GreetingProvider` (`composeApp`'s
  `greeting` package, `@Single`, `greeting(): String = "Wayprint"`), injected into
  `WayprintAppContent` via `koinInject()` — the M0.2 screen's text now comes from it instead of
  a hardcoded string, same displayed output.
  **Note:** added `WayprintApp : Application()` in `androidApp` (`startKoin<KoinApp> {
  androidContext(...) }`, registered as `android:name=".WayprintApp"` in the manifest) —
  needed for M0.4's own Verify line (nothing called `startKoin` before this step) and not
  called out as its own step in the plan, so folded in here. `androidApp/build.gradle.kts`
  gained `koin-bom`/`koin-android`/`koin-annotations` deps for it (wallosmobile's `androidApp`
  carries the same three, unrelated to its DI convention plugin, which only `composeApp`
  applies).
  **Note:** added a `KoinGraphTest` (`composeApp/src/commonTest`, wallosmobile's
  `koin-test`/`.verify()` shape rather than Taiga's JVM-target one, since Wayprint like
  wallosmobile is Android-only) per the "tests written in the same step as the logic they
  cover" ground rule — `commonTest.dependencies { implementation(libs.koin.test) }` added to
  `composeApp/build.gradle.kts`.
  **Note:** two detekt findings surfaced and were fixed: `Koin.android.kt` renamed to
  `PlatformComponentModule.android.kt` (`MatchingDeclarationName` — file name must match its
  single top-level declaration); `GreetingProvider.greeting()` needed
  `@Suppress("FunctionOnlyReturningConstant")` (deliberate hardcoded placeholder, M4 replaces
  the call site).
  **Verify:** `./gradlew build` (same pre-existing F-Droid-signing exclusions as M0.3) succeeds
  project-wide; `composeApp`'s `KoinGraphTest` passes. Installed `:androidApp:assembleGplayDebug`
  on `Medium_Phone_API_36.1`: logcat shows no `FATAL EXCEPTION`/Koin error around startup
  (`ActivityTaskManager: Displayed ... +728ms`), and a screenshot confirms the centered
  "Wayprint" text — now rendered via the injected `GreetingProvider`.

- [x] **M0.5** — Symlink agentic-grappim skills into `wayprint/.claude/skills/`: `finalize`,
  `update-gradle-wrapper`, `emulator-testing`, `kover-coverage-sweep`, `compose-stability-audit`,
  `masvs-review` (relative symlinks, `../../../agentic-grappim/skills/<name>` — never copied,
  per that repo's README). Then, with the user's explicit confirmation in that session, add
  Wayprint to the project list in `agentic-grappim/README.md`.
  **Note:** `finalize`, `update-gradle-wrapper`, `emulator-testing`, `masvs-review` were already
  reachable in this session via pre-existing user-level symlinks
  (`~/.claude/skills/<name>` → `agentic-grappim/skills/<name>`), so those four were already
  confirmed invokable (they appeared in this session's skill listing) before the project-level
  symlinks existed. `kover-coverage-sweep` and `compose-stability-audit` have no user-level
  symlink and only became reachable via this step's new `wayprint/.claude/skills/` symlinks;
  confirming *those* two are recognized needs a fresh session (skill discovery is scanned at
  session start), consistent with this checklist's one-fresh-session-per-step workflow — not
  re-verified here to avoid actually running either skill's workflow as a side effect just to
  prove it resolves.
  **Note:** user confirmed via AskUserQuestion before the `agentic-grappim/README.md` edit, per
  this step's explicit-confirmation requirement.
  **Verify:** `ls -la .claude/skills/` shows each of the six as a symlink resolving (via
  `readlink -f`) to an existing directory under `agentic-grappim/skills/` — confirmed. Skill
  invokability confirmed for `finalize`/`update-gradle-wrapper`/`emulator-testing`/
  `masvs-review` (already in this session's listing); `kover-coverage-sweep`/
  `compose-stability-audit` deferred to a fresh session per the note above.

- [x] **M0.6** — Merge `agentic-grappim/templates/CLAUDE.md.template`'s structure (working
  agreements, settled-decisions table, reference-projects section) into Wayprint's `CLAUDE.md`,
  keeping all existing content (Concept/Origin/MVP scope/Architecture/Naming/Next steps)
  untouched — add the template's sections around it, don't replace anything.
  **Note:** only the three sections named in this step's own text were merged (not the template's
  other sections — "What this file is not", "Shared skills and agents", "Close-out", "Changing a
  check means saying so", "Verification", "Plain technical English", "Chat replies") — the step's
  parenthetical reads as an explicit, narrower scope than "the whole template," consistent with
  the "don't do a step's cleanup/polish beyond what its Verify line asks for" ground rule.
  **Note:** followed TaigaMobileNova's/wallosmobile's precedent (both have evolved past the raw
  template's wording) for how to fill the template's placeholders on a first pass: `Settled
  decisions` populated from decisions already stated in Concept/Origin/MVP scope above plus
  M0.1's `KmpNetworkConventionPlugin`-skip note, rather than left as an empty table; `Reference
  projects` points at `../TaigaMobileNova` and `../wallosmobile` with the specific things ported
  from each so far (build-logic convention plugins, `KoinGraphTest` shape). `docs/revisit.md` and
  `docs/frictions.md` are referenced but not created — neither has been needed yet; per the
  template's own text, `frictions.md` gets created the first time a session actually hits friction
  worth logging, not pre-emptively here.
  **Verify:** `CLAUDE.md` still reads correctly top-to-bottom as a single coherent doc; nothing
  from the original content was lost, diffed against git history for this file — confirmed
  (`git diff CLAUDE.md` shows only additions, `git diff --stat` reports `141 insertions(+)`, `0
  deletions(-)`).

## M1 — `core:gpx`

Port `gpx_route_art.py`'s parsing, RDP simplification, equirectangular projection, and
`day_palette()` to Kotlin, with unit tests proving numeric parity against the Python reference
on the Elbe route's GPX file. Broken down below by porting the reference file's own four pieces
(`parse_track`, `haversine_km`/`rdp`, `fit_projection`/`day_palette`) one at a time, then a
capstone step wiring them into one pipeline and checking the whole thing against the Python
reference — the per-piece tests alone don't prove the *assembled* pipeline matches.

Shared context for all of M1 (re-derive nothing below from scratch):
- Reference: `/home/gregory/claude/wanderwege/elbe route/scripts/gpx_route_art.py`.
- `core:gpx` targets Android only today (`configureKmp()` adds no extra KMP targets — see
  `build-logic/convention/.../KmpConfiguration.kt`), so `commonMain`/`commonTest` here compile
  to JVM/Android bytecode only, same as every other module. That means JDK-standard libraries
  (e.g. `javax.xml.parsers`) are usable from `commonMain` right now even though they wouldn't be
  once/if an iOS target is added — a known gap, already flagged as an open KMP-target-list
  decision in `docs/IMPLEMENTATION_PLAN.md` §9, not M1's to solve.
- No XML library is in `gradle/libs.versions.toml`, and GPX's `<trkpt lat lon><ele>` shape is
  simple enough that the reference script parses it with the stdlib
  (`xml.etree.ElementTree`) rather than a dedicated GPX library. Match that: use the JDK's own
  `javax.xml.parsers` (DOM) rather than adding a new Gradle dependency for this — simplest thing
  that satisfies "pure Kotlin, no *Android* deps" (`javax.xml` is core Java, not `android.*`).
- Fixture: copy one real Elbe-route GPX file into `core:gpx`'s test resources — use
  `04 Riesa - Meissen.gpx` (the smallest, ~53KB/1857 lines, from
  `/home/gregory/claude/wanderwege/elbe route/elberadweg - osmand/tracks/Elberadweg/`). This
  repo has no established Android-KMP test-resource convention yet (check `../wallosmobile` for
  the pattern, or embed the file as a `commonTest` resource under a source set detekt/ktlint
  won't lint as Kotlin) — first step that needs it decides and the rest reuse it.
- "Numeric parity against the Python reference" means: actually run the relevant Python
  function(s) from `gpx_route_art.py` against the same input during the step that needs the
  reference values, and hardcode the captured output as the Kotlin test's expected values —
  don't hand-approximate them.

- [x] **M1.1** — `TrackPoint` data class (`lat`, `lon`, `ele: Double`) and GPX parsing, ported
  from `parse_track()`: read a GPX file's first `<trk>`'s `<trkseg>/<trkpt>` elements (with
  optional child `<ele>`, defaulting to `0.0` when absent, matching the Python) into
  `List<TrackPoint>`, via `javax.xml.parsers` DOM parsing (see shared context above). Establishes
  the `core:gpx` test-fixture convention (see shared context) using the `04 Riesa - Meissen.gpx`
  fixture.
  **Note:** resolved the open test-fixture-convention question (neither `wallosmobile` nor
  `TaigaMobileNova` has one to copy — confirmed by search). Empirically verified (a probe file +
  `processAndroidHostTestJavaRes`) that plain files under `core/gpx/src/commonTest/resources/`
  land on the `androidHostTest` runtime classpath via ordinary `getResourceAsStream`, despite
  `core:gpx` compiling Android-only — no extra Gradle config needed. Fixture now lives at
  `core/gpx/src/commonTest/resources/fixtures/04 Riesa - Meissen.gpx`; later M1 steps reuse this
  path/convention.
  **Note:** `parseTrack` takes an `InputStream` rather than a path/`File`, since `core:gpx` is
  pure Kotlin (no `java.io.File`-from-Android-URI coupling) and this is also what a classpath
  fixture (`getResourceAsStream`) and a future content-resolver stream (M5) both naturally are.
  **Note:** one deliberate deviation from a literal port: `parseTrack`'s `DocumentBuilderFactory`
  disables DOCTYPE/external-entity resolution (XXE hardening) before parsing, which
  `gpx_route_art.py` doesn't do. The Python script only ever runs against the author's own
  trusted files; this exact function will parse attacker-controlled GPX files from a share-intent
  once M5 wires up import, so the hardening was added now rather than left as a gap to remember
  later.
  **Note:** `Placeholder.kt` (M0.3) deleted now that `core:gpx` has real code.
  **Verify:** `GpxParserTest` parses the fixture and asserts point count (614) and first/last
  point's `lat`/`lon`/`ele` against `parse_track()` run on the same file via the Python reference
  — confirmed matching (`(51.305712, 13.307704, 97.0)` first, `(51.161584, 13.47739, 105.0)`
  last). `./gradlew :core:gpx:build` (detekt, ktlintCheck, testAndroidHostTest, kover) passes.

- [x] **M1.2** — `haversineKm(a, b)` and `rdp(points, epsilon)`, ported from `haversine_km()` and
  `rdp()` exactly as written — including that the reference's `rdp()` measures perpendicular
  distance in raw lat/lon degrees, not via `haversine_km`, despite living next to it; port that
  as-is rather than "fixing" it, per CLAUDE.md's instruction to port the reference, not redesign
  it.
  **Note:** `rdp` operates on `List<TrackPoint>` directly (not a generic tuple type like the
  Python reference) — `TrackPoint`'s `ele` field rides along unchanged on surviving points for
  free via data-class equality/copying, and this is the shape M1.4's pipeline needs anyway.
  **Note:** the synthetic zigzag test's expected output was hand-computed, then cross-checked by
  running `rdp()` from the Python reference on the identical input — both agreed:
  `[(0,0),(7,3),(10,0)]` for input `[(0,0),(5,1),(7,3),(10,0)]` at `epsilon=2.0`.
  **Verify:** `haversineKm` unit-tested against three of the reference's own `TOWNS` coordinate
  pairs (Dessau/Wittenberg, Riesa/Meissen, Torgau/Dresden), each against `haversine_km()`'s actual
  output run on the same pair (`1e-9` tolerance). `rdp` unit-tested against (a) the hand-verified
  synthetic zigzag above, and (b) the M1.1 fixture at the reference's default `epsilon=0.0006`,
  asserting the simplified point count (51) matches `rdp()` run on the same file at the same
  epsilon, plus first/last point identity. `./gradlew :core:gpx:build` (detekt, ktlintCheck,
  testAndroidHostTest, kover) passes — confirmed.

- [ ] **M1.3** — `fitProjection(points, boxW, boxH)` (mean-latitude equirectangular projection,
  uniform scale-to-fit, `toSvg(lat, lon)`) and `dayPalette(n, hues, s, l)`, ported from
  `fit_projection()` and `day_palette()`. `day_palette()` calls `colorsys.hls_to_rgb`, which has
  no Kotlin stdlib equivalent — port the HLS→RGB formula itself, and watch the argument order
  (Python's `hls_to_rgb(h, l, s)` is hue/lightness/saturation, not hue/saturation/lightness — easy
  to transpose by mistake).
  **Verify:** `fitProjection`/`toSvg` unit-tested by projecting the reference's `TOWNS` coordinates
  and comparing against `to_svg()` output for the same box size. `dayPalette(5)` (the reference's
  default hues/s/l) asserted against the exact 5 hex colors `day_palette(5)` produces.
  `detekt`/`ktlintCheck` pass.

- [ ] **M1.4** — Wire M1.1–M1.3 into one pipeline matching the reference's `build()`: parse →
  `rdp`-simplify → project, run on the M1.1 fixture at the reference's defaults
  (`epsilon=0.0006`, `box=(860, 980)`).
  **Verify:** a unit test runs the assembled pipeline on the fixture and asserts the full
  simplified+projected point list matches `build()`'s (or the equivalent inline call sequence's)
  output for the same file, exactly or within a tight floating-point tolerance — the one test that
  proves the *assembled* pipeline matches the Python reference, not just its individual pieces.
  `./gradlew :core:gpx:build` (detekt/ktlint/kover) passes.

## M2 — `uikit`

Theme (`Colors.kt`, `Dimens.kt`, `Typography.kt`, `Theme.kt`), and the small set of shared
widgets M4's screen will actually need (not more). Step-break-down at the start of M2.

## M3 — `feature:wayprint:domain`

Route model, style presets, label collision-avoidance layout. **Blocked on an algorithm
decision** — see IMPLEMENTATION_PLAN.md §9. Resolve that first, then step-break-down M3.

## M4 — `feature:wayprint:ui`

Canvas renderer for one hardcoded story-size (1080×1920) preset, driven by M3's domain models.
Step-break-down at the start of M4.

## M5 — import/export end-to-end

File picker / share-intent GPX import; `Bitmap` render → `MediaStore`/share sheet export. Wires
M1–M4 into one working flow. Step-break-down at the start of M5.

## M6 — distribution

F-Droid/Play flavor dimension (`STORE`, `.fdroid` applicationIdSuffix), `fastlane/` skeleton
copied and adapted, CI. Step-break-down at the start of M6.

## Backlog (growth roadmap, not milestones yet)

- Editable canvas: drag labels, pick colors/aspect ratio, undo.
- More input sources: Health Connect / Strava OAuth import; on-device live recording (its own
  milestone-scale scope jump when it happens, not an incremental add-on).
- Multiple layout templates (poster, square post, story) once the story layout is proven.
- iOS/Desktop targets for `core:gpx` and the Compose `Canvas` renderer.
