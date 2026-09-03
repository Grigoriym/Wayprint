# Wayprint checklist

**Current step:** M8.1

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

- [x] **M1.3** — `fitProjection(points, boxW, boxH)` (mean-latitude equirectangular projection,
  uniform scale-to-fit, `toSvg(lat, lon)`) and `dayPalette(n, hues, s, l)`, ported from
  `fit_projection()` and `day_palette()`. `day_palette()` calls `colorsys.hls_to_rgb`, which has
  no Kotlin stdlib equivalent — port the HLS→RGB formula itself, and watch the argument order
  (Python's `hls_to_rgb(h, l, s)` is hue/lightness/saturation, not hue/saturation/lightness — easy
  to transpose by mistake).
  **Note:** `fitProjection` returns a `Projection` class (`meanLat`, `scale`, `toSvg(lat, lon):
  Pair<Double, Double>`) rather than the Python reference's dict-plus-closure, since Kotlin has no
  closure-returning-function idiom as natural as Python's nested `to_svg`; `cosLat`/`minX`/`minY`
  are stored as private constructor fields captured once at construction, same values the Python
  closure captures.
  **Note:** both `round(x, 1)` (SVG coordinates) and `round(x)` (RGB channels) needed a faithful
  port of Python's round-half-to-even float rounding, which `kotlin.math` has no equivalent for —
  used `BigDecimal(value).setScale(n, RoundingMode.HALF_EVEN)`, which operates on the same exact
  binary value Python's correctly-rounded decimal conversion does; verified to match the Python
  reference's actual output (not just assumed to match) via the numeric-parity tests below.
  **Verify:** `fitProjection`/`toSvg` unit-tested by projecting the reference's `TOWNS` coordinates
  and comparing against `to_svg()` output for the same box size (`meanLat`, `scale`, and all six
  towns' projected coordinates matched exactly). `dayPalette(5)` (the reference's default
  hues/s/l) asserted against the exact 5 hex colors `day_palette(5)` produces — matched exactly
  (`#cea573`, `#88ce73`, `#73c5ce`, `#8773ce`, `#ce73ad`). `./gradlew :core:gpx:build` (detekt,
  ktlintCheck, testAndroidHostTest, kover) passes.

- [x] **M1.4** — Wire M1.1–M1.3 into one pipeline matching the reference's `build()`: parse →
  `rdp`-simplify → project, run on the M1.1 fixture at the reference's defaults
  (`epsilon=0.0006`, `box=(860, 980)`).
  **Note:** added `RouteArt.kt`'s `buildRouteArt(input, epsilon, boxW, boxH)`, matching a single
  track's slice of `build()`'s call sequence (`parse_track` → `rdp` → `fit_projection` → `to_svg`
  on the same, simplified points) — `build()`'s multi-stage/`day_palette`/`towns` output is
  outside a single-file pipeline and not part of what this step asks for.
  **Note:** expected values captured by running `parse_track()` → `rdp(epsilon=0.0006)` →
  `fit_projection(860, 980)` → `to_svg()` from `gpx_route_art.py` on the M1.1 fixture, hardcoded
  as the full 51-point list in `RouteArtTest`, per CLAUDE.md's numeric-parity requirement.
  **Verify:** `RouteArtTest` runs `buildRouteArt` on the fixture and asserts the full
  simplified+projected point list (51 points) matches the Python reference's output for the same
  file exactly — confirmed. `./gradlew :core:gpx:build` (detekt, ktlintCheck, testAndroidHostTest,
  kover) passes.

## M2 — `uikit`

Theme (`Colors.kt`, `Dimens.kt`, `Typography.kt`, `Theme.kt`), and the small set of shared
widgets M4's screen will actually need (not more). Broken down below into the color
scheme/`Theme.kt` piece and the typography/spacing piece, each wired into `composeApp`'s existing
M0.2/M0.4 placeholder screen so the step is actually exercised, not just compiled in isolation.

Shared context for all of M2 (re-derive nothing below from scratch):
- Reference: `../wallosmobile`'s `uikit/src/commonMain/kotlin/.../Color.kt`/`Theme.kt`/`Type.kt`
  and `../TaigaMobileNova`'s `uikit/src/commonMain/kotlin/.../theme/{Colors,Dimens,Typography,
  Theme}.kt` — port the *pattern* from each, not the file; both are reference-only, coupled to
  their own apps, per `IMPLEMENTATION_PLAN.md` §2/§4. The four filenames this milestone uses
  (`Colors.kt`, `Dimens.kt`, `Typography.kt`, `Theme.kt`) match Taiga's flat `theme/` package
  shape, not wallosmobile's.
- wallosmobile's `Theme.kt` also wires three composition locals of its own (`LocalTopBarConfig`,
  `LocalIsOffline`, `LocalSnackbarHostController`) because that app has a shell with a top bar,
  an offline banner, and a snackbar host. Wayprint has none of that infrastructure yet — M4
  hasn't been designed, and today's placeholder screen is a single centered `Text`. `WayprintTheme`
  here is just `MaterialTheme` + `Surface` (wallosmobile's own doc comment on why the `Surface`
  belongs to the theme, not the caller, still applies — port that reasoning, not the locals).
  Add a composition local later only when a real step needs one.
- No shared widgets in M2, despite IMPLEMENTATION_PLAN.md §4's "small set of shared widgets M4's
  screen will actually need" phrasing. M4 is explicitly "step-break-down at the start of M4" —
  still undesigned — so any widget built now would be a guess at M4's UI before M4 decides what
  it needs, which is exactly the speculative structure CLAUDE.md's "Simplicity first" agreement
  warns against. Leave every widget for the M4 step that first needs it.
- Color values: neither root `CLAUDE.md` nor `IMPLEMENTATION_PLAN.md` decides a brand palette for
  app chrome (buttons, backgrounds, app bar if one ever exists) — that's a different thing from
  `core:gpx`'s `dayPalette()`, which colors the route art itself and is unrelated to this. M2.1
  picks *a* reasonable static Material3 light/dark `ColorScheme` (own seed hue, not dynamic
  color, matching Taiga's/wallosmobile's static-palette precedent over Android 12's
  wallpaper-based dynamic color) rather than leaving the choice open — the exact hue is not
  load-bearing this early (nothing downstream depends on a specific color yet) and can be
  revisited freely later.

- [x] **M2.1** — `Colors.kt` (a light and a dark Material3 `ColorScheme`, own static seed
  palette) and `Theme.kt` (`@Composable fun WayprintTheme(darkTheme: Boolean =
  isSystemInDarkTheme(), content: @Composable () -> Unit)`, wrapping `content` in `MaterialTheme`
  + a theme-owned `Surface`, per shared context above — no composition locals). Wire it into
  `composeApp`: `WayprintAppContent.kt`'s bare `MaterialTheme { Surface { ... } }` becomes
  `WayprintTheme { ... }` (the `Surface` moves into the theme, so the call site drops its own).
  **Note:** ported wallosmobile's full `Colors.kt`/`Theme.kt`/`ContrastTest.kt` shape (10
  light + 10 dark text-on-background pairs: primary/secondary/tertiary/error × base+container,
  plus surface/surfaceVariant) rather than Taiga's sparser partial-`ColorScheme` one, since M2.1's
  own Verify line requires "every ... pair used for text-on-background" to pass `ContrastTest` —
  the fuller pair set is what that line is asking to be tested. Left out wallosmobile's
  outline/inverse/surface-container-ladder colors and its `DARK_BACKGROUND_COLOR_FOR_PREVIEW`
  constant: none has a caller yet (no `Card`/`Dialog`/`Snackbar`/`@Preview` exists), so adding them
  now would be dead code ahead of the step that actually needs them, per CLAUDE.md's
  "no speculative structure" agreement.
  **Note:** seed hue is a forest green (`Green40`/`Green80` etc. in `Colors.kt`) — waymarking/trail
  themed, matching "Wayprint" per M2's own naming rationale in root `CLAUDE.md`. Not brand-critical
  per M2's shared context; picked and verified only for WCAG contrast, not for any deeper meaning.
  **Note:** `composeApp/build.gradle.kts` gained `commonMain.dependencies { implementation(project(
  ":uikit")) }` — the module had no dependency on `uikit` before this step.
  **Note:** ktlint's `compose:modifier-missing-check` flagged `WayprintTheme` (a `@Composable` that
  emits content but takes no `Modifier` param, same shape as wallosmobile's own `Theme.kt`) —
  wallosmobile's `.editorconfig` already carries a `[**/Theme.kt]` section disabling that rule for
  exactly this reason; ported the same section into Wayprint's `.editorconfig` rather than adding a
  `Modifier` parameter the theme function has no use for.
  **Verify:** a `ContrastTest` (ported pattern from wallosmobile's, WCAG AA 4.5:1 normal-text
  contrast) asserts every light/dark `ColorScheme` pair used for text-on-background in `Colors.kt`
  meets the bar — confirmed passing (all 20 pairs, ratios 5.48–16.78, well above 4.5). `./gradlew
  :uikit:build` (detekt, ktlintCheck, kover) and `./gradlew build` (same pre-existing M0.3/M0.4
  F-Droid-signing exclusions) pass. Installed `:androidApp:assembleGplayDebug` on
  `Medium_Phone_API_36.1`: screenshot confirms the centered "Wayprint" text screen still renders
  correctly, now through `WayprintTheme` (light `SurfaceLight` background, `OnSurfaceLight` text).

- [x] **M2.2** — `Typography.kt` (a `Typography` instance for `MaterialTheme`'s standard Material3
  type scale, default platform font — no custom font asset) and `Dimens.kt` (start with exactly
  one named spacing constant — more get added by whichever later step first needs them, not
  pre-emptively here, per the "no speculative structure" reasoning in shared context above). Wire
  both into `composeApp`: `WayprintTheme` passes the new `Typography` into its `MaterialTheme`
  call, and the placeholder screen's `Box` uses the new spacing constant as padding (so `Dimens.kt`
  has an actual caller, not a dangling unused value).
  **Note:** followed wallosmobile's `Type.kt` pattern exactly (`val WayprintTypography =
  Typography()`, untouched Material3 default) rather than Taiga's partial custom type scale —
  this step's own text says "standard Material3 type scale, default platform font," which is
  wallosmobile's shape, not a customized one.
  **Note:** the one `Dimens.kt` constant is `screenPadding = 16.dp`, applied to the placeholder
  `Box`'s `Modifier.padding`.
  **Verify:** `./gradlew :uikit:build` (detekt, ktlintCheck, kover) and `./gradlew build` (same
  pre-existing M0.3/M0.4 F-Droid-signing exclusions) pass — confirmed. Installed
  `:androidApp:assembleGplayDebug` on `Medium_Phone_API_36.1`: screenshot confirms the "Wayprint"
  text still renders, now padded and through `WayprintTypography`.

## M3 — `feature:wayprint:domain`

Route model, style presets, label collision-avoidance layout — the one real "domain" concern
this app has (IMPLEMENTATION_PLAN.md §4). Broken down below into the geometry primitives the
placement algorithm needs, the algorithm itself, the style preset + route/distance model, and a
capstone step wiring all three into one pipeline M4 can render from directly.

Shared context for all of M3 (re-derive nothing below from scratch):
- Algorithm and label-scope decisions: see IMPLEMENTATION_PLAN.md §9 (greedy dodge-by-priority,
  fixed priority order Start → Finish → distance; label set is Start/Finish markers + total
  distance only, no date).
- `feature:wayprint:domain` applies only `wayprint.kmp.library` + `wayprint.kmp.library.stability`
  (IMPLEMENTATION_PLAN.md §5) — no Compose plugin, so nothing here may depend on Compose types
  (`androidx.compose.ui.graphics.Color`, `Dp`, etc.). Colors stay hex strings (matching
  `core:gpx`'s `dayPalette()` return shape); sizes stay plain `Double`/`Int` in the same SVG-space
  units `core:gpx`'s `fitProjection`/`toSvg` already use. M4 (which does apply the Compose plugin)
  converts to Compose types at the boundary, not this module.
- Total distance is summed with `core:gpx`'s `haversineKm` over the **raw** (pre-`rdp`) points,
  not the simplified ones — `rdp` exists to declutter the drawn line, not to change what number is
  reported as the ridden distance.
- Route-line vs. label overlap is deliberately **not** part of the collision check (see the
  algorithm decision in IMPLEMENTATION_PLAN.md §9) — M4 handles legibility over the line with a
  stroke halo behind label text, the same `paint-order: stroke` technique
  `elbe-story-actual.html` uses. Don't add route-line geometry to this module's overlap checks.

- [x] **M3.1** — Geometry primitives: a `Rect` (or equivalent bounding-box) type with an
  `overlaps(other: Rect): Boolean` check, a `TextAnchor` enum (`START`/`MIDDLE`/`END`, matching
  SVG `text-anchor` — M4 needs to know which edge of a label's box is pinned to its `x,y`), and a
  `PlacedLabel` data class (`text`, `x`, `y`, `anchor`, derived/stored bounding box). Unit tests
  cover `Rect.overlaps` edge cases (disjoint, touching-but-not-overlapping, fully contained,
  partial overlap).
  **Note:** `Rect` stores `minX`/`minY`/`maxX`/`maxY` (matching `core:gpx`'s `Projection.toSvg`
  SVG-space coordinates, y grows downward); `overlaps` uses strict inequalities on all four sides
  so a shared edge counts as touching, not overlapping. `PlacedLabel.boundingBox` is a plain stored
  `Rect` field — M3.2 is what computes it, not this step.
  **Note:** ktlint auto-reformatted all three data classes' multi-param constructors from
  one-param-per-line back to single-line (`ktlintCommonMainSourceSetFormat`) — matches
  `core:gpx`'s `TrackPoint.kt` one-liner style; ran the formatter rather than hand-guessing the
  expected layout.
  **Verify:** `./gradlew :feature:wayprint:domain:build` (detekt, ktlintCheck, testAndroidHostTest,
  kover) passes — confirmed.

- [x] **M3.2** — The greedy placement algorithm itself: given a label's anchor point, its text
  (for estimated bbox size — a fixed char-width-times-length approximation is fine, no real font
  metrics available in a pure-Kotlin module), and an ordered list of candidate offset/anchor pairs
  (compass-style: right, left, above, below), plus the set of already-placed labels' boxes and the
  canvas bounds, return the first candidate that clears all of them, or the last candidate if none
  do. A second function folds this over a priority-ordered list of labels into a
  `List<PlacedLabel>`. Unit tests: (a) labels far apart place at their first/preferred candidate;
  (b) two labels whose preferred candidates collide — the lower-priority one is verified to fall
  back to a later candidate; (c) a forced-unresolvable case (every candidate collides) falls back
  to the last candidate rather than throwing.
  **Note:** added `LabelPlacement.kt`: `LabelCandidate` (`dx`/`dy` offset + `TextAnchor`),
  `LabelRequest` (text/anchor point/ordered candidates), `compassCandidates(offset)` (the
  right/left/above/below list in that fixed order — the offset function IMPLEMENTATION_PLAN.md §9
  describes, not new scope), `placeLabel` (single-label algorithm), and `placeLabels` (the fold over
  a priority-ordered list). Estimated label size is `text.length * 7.0` wide × `14.0` tall, hardcoded
  module-private constants — no `StoryPreset` exists yet for these to come from (that's M3.3).
  **Note:** "clears" per the step's own text means both no overlap with any already-placed box *and*
  fully inside `canvasBounds` — a candidate box exceeding the canvas edge is treated the same as one
  overlapping another label.
  **Note:** the empty-`candidates`-list case (impossible via `compassCandidates`, but not provably so
  to the compiler from `placeLabel`'s signature) falls through to `error(...)` rather than a defensive
  check — Kotlin needs some expression after the loop for exhaustiveness; this is that expression, not
  extra validation logic.
  **Verify:** `./gradlew :feature:wayprint:domain:build` (detekt, ktlintCheck, testAndroidHostTest,
  kover) passes — confirmed. `LabelPlacementTest` covers all three specified cases: (a) two
  far-apart labels each land on their first (right) candidate; (b) a second label anchored 5 units
  from the first has its right candidate collide, verified to fall back to its left candidate; (c) a
  1×1 canvas forces every candidate out of bounds, verified to fall back to the last (below)
  candidate without throwing. `./gradlew build` (same pre-existing M0.3/M0.4 F-Droid-signing
  exclusions) passes project-wide.

- [x] **M3.3** — `StoryPreset` (the single hardcoded MVP style preset: 1080×1920 canvas, the
  route-art box size/margins within it, line/background/text colors as hex strings — own palette,
  not the Elbe reference's paper/ochre one, consistent with `uikit`'s M2 forest-green theme) and a
  route/distance model wrapping `core:gpx`'s `buildRouteArt` output: total distance (see shared
  context above) plus the projected path ready to draw. No label placement yet — that's M3.4.
  **Note:** `feature:wayprint:domain` had no dependency on `core:gpx` before this step —
  `build.gradle.kts` gained `commonMain.dependencies { implementation(project(":core:gpx")) }`.
  **Note:** `buildWayprintRoute` doesn't call `buildRouteArt` as a black box: `buildRouteArt`
  consumes its `InputStream` once internally, but total distance needs the *raw* (pre-`rdp`)
  points from that same parse too — so it replicates `buildRouteArt`'s parse → `rdp` → project
  call sequence directly via `core:gpx`'s own exposed `parseTrack`/`rdp`/`fitProjection`
  functions, producing an identical path for the same input while also summing `haversineKm` over
  the raw points for distance.
  **Note:** `StoryPreset`'s route-art box (860×980) and margins (110/470, centering it in the
  1080×1920 canvas) reuse `core:gpx`'s own `DEFAULT_BOX_WIDTH`/`DEFAULT_BOX_HEIGHT` values rather
  than picking new ones. Colors: `#F7F4EC` background, `#2E6B4F` line (uikit's `Green40` seed
  hue), `#1B2E22` text — own palette, chosen for this step, not ported from the Elbe reference.
  **Note:** the M1 fixture GPX (`04 Riesa - Meissen.gpx`) was copied into this module's own
  `src/commonTest/resources/fixtures/` — Kotlin test resources aren't shared across modules, so
  each module that needs it keeps its own copy, per the convention M1.1 established.
  **Verify:** `WayprintRouteTest` runs `buildWayprintRoute` on the fixture and asserts
  `totalDistanceKm` (26.15576342355938) matches `gpx_route_art.py`'s `parse_track()` +
  `haversine_km()` summed over consecutive raw points, run on the same file — confirmed.
  `./gradlew :feature:wayprint:domain:build` (detekt, ktlintCheck, testAndroidHostTest, kover)
  passes; `./gradlew build` (same pre-existing M0.3/M0.4 F-Droid-signing exclusions) passes
  project-wide.

- [x] **M3.4** — Wire M3.1–M3.3 into one pipeline (e.g. `buildWayprintLayout(gpxInput,
  preset): WayprintLayout`): parse + project the route (via `core:gpx`), compute the Start/Finish
  label anchors from the route's projected first/last points and the distance label's anchor from
  the projected bounding-box center, run M3.2's greedy placer over them in the fixed priority
  order, and return the projected path plus the 3 placed labels as one `WayprintLayout` — the
  shape M4 renders directly.
  **Note:** `WayprintRoute.path` (M3.3) is in route-box-local coordinates (`0..routeBoxWidth,
  0..routeBoxHeight`), not canvas coordinates — `buildWayprintLayout` translates every path point
  by `(preset.marginX, preset.marginY)` before computing anchors/placing labels, since
  `marginX`/`marginY` exist precisely to center that box in the full canvas
  (`marginX*2 + routeBoxWidth == canvasWidth`, same for Y) and "the shape M4 renders directly"
  reads as already-final canvas coordinates, not something M4 has to offset itself.
  IMPLEMENTATION_PLAN.md §9's collision-avoidance write-up also says candidates fall back when
  they'd cross "the canvas edge," confirming `placeLabels`' `canvasBounds` here is the full
  `0,0,canvasWidth,canvasHeight` rect, not the smaller route box.
  **Note:** the distance label's bounding-box-center anchor is the bbox of the (translated) route
  path itself, per IMPLEMENTATION_PLAN.md §9 ("the route's bounding-box center for the distance
  label") — not the canvas center.
  **Note:** the distance label's text (`"%.1f km"`, e.g. `"26.2 km"`, via
  `String.format(Locale.ROOT, ...)` for a locale-independent decimal separator) and the candidate
  offset distance (`LABEL_OFFSET = 24.0`, a module-private constant alongside the others M3.2
  hardcoded) were both undecided by this step's own text or IMPLEMENTATION_PLAN.md — picked here
  since `LabelRequest`/`PlacedLabel` need concrete values, not deferred further.
  **Verify:** `WayprintLayoutTest` runs the pipeline on the M1 fixture and asserts: exactly 3
  labels are returned (Start/Finish/distance), none of their bounding boxes overlap each other,
  and each stays within the canvas bounds — confirmed. No Python reference exists for this
  pipeline (this logic isn't in `gpx_route_art.py`), so expectations are hand-verified against the
  fixture's actual geometry, not ported numeric parity. `./gradlew :feature:wayprint:domain:build`
  (detekt, ktlintCheck, testAndroidHostTest, kover) passes; `./gradlew build` (same pre-existing
  M0.3/M0.4 F-Droid-signing exclusions) passes project-wide.

## M4 — `feature:wayprint:ui`

Canvas renderer for one hardcoded story-size (1080×1920) preset, driven by M3's domain models.
Broken down below into the background/route-line drawing, the marker/label drawing (the
stroke-halo legibility technique from the Elbe reference), and a capstone step wiring the
finished renderer into `composeApp`, replacing the M0.2/M0.4 placeholder screen end-to-end.

Shared context for all of M4 (re-derive nothing below from scratch):
- Visual reference: `/home/gregory/claude/wanderwege/elbe route/elbe-story-actual.html`'s
  `.route-line`/`.town-label`/`.marker-tag` CSS and its `<circle>` start/finish markers — the
  `paint-order: stroke` halo (`stroke: var(--paper); stroke-width: 8-9`) behind every label is
  the legibility technique M4.2 ports; there's no route-line-vs-label collision check in
  `feature:wayprint:domain` (M3's own shared context) *because* this halo makes one unnecessary.
- `feature:wayprint:domain`'s M3 output is everything the renderer needs: `WayprintLayout`
  (`path: List<Pair<Double, Double>>`, `totalDistanceKm`, 3 `labels: List<PlacedLabel>`) plus
  `StoryPreset` (canvas size, margins, `backgroundColor`/`lineColor`/`textColor` as hex strings).
  No new domain state — `feature:wayprint:ui` only needs a dependency on
  `feature:wayprint:domain` added to its `build.gradle.kts`.
- `WayprintLayout.path` and every `PlacedLabel`'s `x`/`y` are already in `StoryPreset`'s
  canvas-space units (`0..canvasWidth, 0..canvasHeight`, y grows downward) — the composable maps
  that fixed space onto its actual on-screen pixel size with one uniform scale factor (matching
  how the Elbe reference's SVG `viewBox` scales to its container), not a second layout pass.
- Start/Finish circle markers are drawn from `WayprintLayout.path.first()`/`.last()` (the actual
  route endpoints), not from the "Start"/"Finish" `PlacedLabel`s — those two labels' `x`/`y` are
  their *offset* dodge positions (M3.2's compass candidates), not marker centers.
- Halo text has no Compose Multiplatform-portable primitive (`DrawScope` has no `paint-order`/
  dual-paint text draw) — port it via `drawContext.canvas.nativeCanvas` + two
  `android.graphics.Paint`s (one `STROKE` style in `backgroundColor` drawn first, one `FILL`
  style in `textColor` drawn second). Unlike M1's `javax.xml.parsers`/`BigDecimal` calls
  (JVM-standard, only "Android-only" because no other KMP target is configured yet),
  `android.graphics.Paint` is genuinely Android-only — flag it as a real iOS/Desktop porting gap
  in `IMPLEMENTATION_PLAN.md` §9 when M4.2 lands, not a "happens to work for now" one.
- No GPX import UI exists yet (M5's scope). M4.3's end-to-end wiring still needs *some* route to
  render — use a short, hand-written GPX string (a handful of `<trkpt>`s, not the full M1
  fixture) embedded directly in `composeApp` and fed to `buildWayprintLayout` via
  `ByteArrayInputStream`. This is provisional, same spirit as M0.2's placeholder text / M0.4's
  `GreetingProvider` — M5 replaces the call site with the real file-picker/share-intent input,
  and `GreetingProvider` itself (now orphaned) should be deleted in M4.3 per CLAUDE.md's "remove
  what your change orphaned."

- [x] **M4.1** — A pure `fitScale`-type function (given `StoryPreset`'s canvas width/height and
  the composable's actual available width/height, return the uniform scale factor + centering
  offset that letterboxes the fixed canvas into that space, matching the Elbe reference's
  `viewBox` behavior) plus a `WayprintCanvas` composable in `feature:wayprint:ui` that uses it to
  draw `StoryPreset.backgroundColor` full-bleed and `WayprintLayout.path` as one stroked polyline
  in `StoryPreset.lineColor`. No markers/labels yet (M4.2). Add `feature:wayprint:ui`'s dependency
  on `feature:wayprint:domain`.
  **Note:** added `CanvasFit.kt` (`fitScale(canvasWidth, canvasHeight, availableWidth,
  availableHeight): CanvasFit`, a `scale`/`offsetX`/`offsetY` data class) — file name is
  `CanvasFit.kt`, not `FitScale.kt`, since detekt's `MatchingDeclarationName` requires the file to
  match its single top-level declaration.
  **Note:** `WayprintCanvas(layout: WayprintLayout, preset: StoryPreset, modifier: Modifier =
  Modifier)` parses `StoryPreset`'s hex color strings with a small local `parseHexColor` (own
  6-digit-hex-to-`Color` parser, pure Kotlin — not `android.graphics.Color.parseColor`, to avoid
  pulling in a genuinely-Android-only API a step earlier than M4.2 flags one as necessary) and
  draws the route polyline with the Elbe reference's own `.route-line` stroke style (`width = 6f`,
  round cap/join) ported directly rather than picked arbitrarily.
  **Note:** `Placeholder.kt` deleted now that `feature:wayprint:ui` has real code, matching M1.1's
  precedent.
  **Verify:** unit tests for the scale/offset function cover exact-fit, letterboxed-wider, and
  letterboxed-taller cases — confirmed passing. `./gradlew :feature:wayprint:ui:build` (detekt,
  ktlintCheck, testAndroidHostTest, kover) passes; `./gradlew build` (same pre-existing M0.3/M0.4
  F-Droid-signing exclusions) passes project-wide. (No screenshot yet — `WayprintCanvas` isn't
  wired into any screen until M4.3; that step is where visual output first gets confirmed
  on-device, same as M2's theme/typography pieces.)

- [x] **M4.2** — Extend `WayprintCanvas` (or add to it): Start/Finish circle markers at
  `WayprintLayout.path.first()`/`.last()`, and the 3 `WayprintLayout.labels` drawn with the
  halo-stroke technique (see shared context) in `StoryPreset.textColor`.
  **Note:** Start marker is a hollow ring (fill `backgroundColor`, stroke `lineColor`, width 6,
  radius 13 — the Elbe reference's own start-marker values); Finish marker is a solid filled
  circle in `lineColor` (radius 14, the reference's finish-marker radius) — Wayprint's palette has
  no third accent color like the reference's ochre, so hollow-vs-filled carries the Start/Finish
  distinction instead of a color change.
  **Note:** halo text ported via `drawContext.canvas.nativeCanvas` + two `android.graphics.Paint`s
  (`STROKE` in `backgroundColor` drawn first, `FILL` in `textColor` drawn second, width 8/text size
  28 — the reference's town-label halo values), per shared context; `PlacedLabel.anchor`
  (`TextAnchor.START`/`MIDDLE`/`END`) maps directly to `Paint.Align.LEFT`/`CENTER`/`RIGHT`.
  `PlacedLabel.y` is the domain layer's vertical *center* (`LabelPlacement.kt`'s `boundingBox`
  computes `minY/maxY` as `y ∓ halfHeight`), not a text baseline, so the draw call offsets by
  `textSize * 0.35` to approximate vertical centering — no exact-baseline requirement exists
  anywhere in M3's domain model to port instead.
  **Verify:** `./gradlew :feature:wayprint:ui:build` (detekt, ktlintCheck, kover) passes —
  confirmed. No new unit-testable logic beyond M4.1's (Canvas drawing itself isn't unit-testable
  without an instrumented/Robolectric harness this project doesn't have) — visual confirmation
  deferred to M4.3. `./gradlew build` (same pre-existing M0.3/M0.4 F-Droid-signing exclusions)
  passes project-wide.

- [x] **M4.3** — Wire `WayprintCanvas` into `composeApp`: `WayprintAppContent.kt`'s placeholder
  `Text(greetingProvider.greeting())` becomes `WayprintCanvas(layout =
  buildWayprintLayout(demoGpxInput), preset = DEFAULT_STORY_PRESET)` (or equivalent), reading the
  demo GPX string described in shared context. Delete `GreetingProvider` and its `KoinGraphTest`
  coverage, now orphaned. Add `composeApp`'s dependency on `feature:wayprint:ui`.
  **Note:** `composeApp/build.gradle.kts` gained `implementation(project(":feature:wayprint:ui"))`
  and `implementation(project(":feature:wayprint:domain"))` — `WayprintAppContent.kt` calls
  `buildWayprintLayout`/`DEFAULT_STORY_PRESET` directly, and `feature:wayprint:ui`'s own dependency
  on `:feature:wayprint:domain` is `implementation`, not `api`, so it isn't visible transitively.
  **Note:** the demo GPX (`composeApp`'s new `DemoRoute.kt`, `internal const val DEMO_GPX`) is a
  6-point hand-written zigzag near Dresden (~1.2km), fed to `buildWayprintLayout` via
  `ByteArrayInputStream`, wrapped in `remember { }` in `WayprintAppContent` so it isn't reparsed
  every recomposition. `GreetingProvider.kt` (and its now-empty `greeting/` package) deleted;
  `KoinGraphTest` itself needed no edit — it verifies the whole Koin module generically rather than
  asserting on `GreetingProvider` by name, so it keeps passing with fewer definitions in the graph.
  **Note:** `uikit`'s `screenPadding` (M2.2) is now unused — its only caller was the old
  `WayprintAppContent` `Box.padding`, which this step's replacement content doesn't use (the canvas
  letterboxes itself to the available space per M4's shared context; padding would just shrink that
  space). Left in `uikit/.../Dimens.kt` rather than deleted: it lives in a module this step didn't
  otherwise touch, and per the "leave pre-existing dead code alone — mention it instead" ground
  rule, deleting a different M2.2-verified module's exported constant reads as cleanup beyond this
  step's own scope.
  **Note:** this step's own on-device Verify surfaced a real, pre-existing bug: `core:gpx`'s
  `GpxParser.kt` (M1.1) had never actually run on a real Android runtime before now — `core:gpx`'s
  tests all run on the JVM host test target (`testAndroidHostTest`), not an instrumented/on-device
  target, so its `DocumentBuilderFactory`-based XXE hardening was only ever exercised against
  desktop-JDK's Xerces implementation. On the real emulator, `parseTrack` crashed immediately:
  Android's built-in `DocumentBuilderFactory` throws `ParserConfigurationException`/
  `UnsupportedOperationException` on *every one* of the four hardening calls it used
  (`setFeature("...disallow-doctype-decl", true)`, both `setFeature("...external-*-entities",
  false)` calls, and `isXIncludeAware = false`) — confirmed by probing each individually on-device
  before fixing. Replaced with a `DocumentBuilder.setEntityResolver { _, _ -> InputSource(empty) }`
  override (a standard SAX/DOM mechanism, not an implementation-specific feature flag, so it works
  identically on both platforms) plus the one property Android *does* support
  (`isExpandEntityReferences = false`); dropped the four unsupported calls rather than wrapping them
  in try/catch, since silently swallowing "feature not recognized" would read as hardening still in
  effect when it plainly isn't on Android. `core:gpx`'s existing `GpxParserTest`/host-test suite
  still passes unchanged (the fixture GPX has no DOCTYPE/entities to begin with, so this was never
  exercising XXE protection either — just confirming basic parsing still works after the fix). No
  `core:gpx` step is marked `[ ]` to reopen for this — M1.1 is already ticked and its own Verify line
  (numeric parity + build passing) is unaffected; this note plus the code fix is the record.
  **Verify:** `./gradlew build` (same pre-existing M0.3/M0.4 F-Droid-signing exclusions) passes
  project-wide, including `core:gpx`'s full `testAndroidHostTest` suite after the `GpxParser` fix.
  Installed `:androidApp:assembleGplayDebug` on `Medium_Phone_API_36.1`: no crash (confirmed via
  logcat, no `FATAL EXCEPTION`), and a screenshot confirms the full story image — background, route
  line, hollow Start / filled Finish markers, and all 3 legible (haloed) labels ("Start", "Finish",
  "1.2 km") — replacing the old "Wayprint" placeholder text.

## M5 — import/export end-to-end

File picker / share-intent GPX import; `Bitmap` render → `MediaStore`/share sheet export. Wires
M1–M4 into one working flow. Broken down below into the state model + file-picker import, then
share-intent import reusing that same path, then headless `Bitmap` rendering, then the
`MediaStore` save + share-sheet step that completes the flow.

Shared context for all of M5 (re-derive nothing below from scratch):
- Reference: root `CLAUDE.md`'s MVP scope — Input is "GPX file only (file picker / share-intent
  from Strava, Komoot, OsmAnd, etc.)"; Output is "exported to a `Bitmap` and saved/shared via
  `MediaStore` / share sheet."
- Module ownership: IMPLEMENTATION_PLAN.md §4's module table already assigns `feature:wayprint:ui`
  "Compose Canvas renderer, **import flow, export/share**" and it already applies `wayprint.kmp.di`
  (§5) — so M5's screen/ViewModel/import/export logic all land there, not in `composeApp`.
  `composeApp` goes back to being a thin DI-root/shell that just hosts the finished screen, the
  same role M4's shared context described for it.
- This is the first place the app has real state that can fail (a picked/shared file might not be
  a valid GPX). Root `CLAUDE.md`'s "Planned architecture" already commits to MVVM + Koin DI — this
  milestone is where that pattern gets its first real use: one `WayprintViewModel` using
  `jetbrains-lifecycle-viewmodel-compose`/`koin-compose-viewmodel` (both already in the version
  catalog since M0.1's seed, unused until now). No repository/use-case layers — IMPLEMENTATION_PLAN
  .md §4's lean-MVP/no-speculative-structure call already ruled those out for this app.
  `feature:wayprint:domain`'s `buildWayprintLayout` is called directly from the ViewModel.
- M4.3's precedent (direct `android.graphics.Paint` calls in `feature:wayprint:ui`'s commonMain,
  flagged as an Android-only porting gap in IMPLEMENTATION_PLAN.md §9 rather than built behind an
  expect/actual, since only one KMP target exists today) extends to M5: `ContentResolver`/
  `MediaStore`/share-`Intent` calls also go directly in commonMain here, flagged the same way.
- M4.3's demo route (`composeApp`'s `DemoRoute.kt`/`DEMO_GPX`) is the provisional scaffolding that
  step's own note already called out for M5 to replace — delete it once M5.1's real import path is
  wired in, per the "remove what your change orphaned" ground rule.
- Picker mechanism: `ActivityResultContracts.GetContent("*/*")`, not SAF `OpenDocument` with a mime
  filter — GPX has no reliable, universally-tagged MIME type across the picker apps CLAUDE.md names
  (Strava/Komoot/OsmAnd), so filtering by mime would as often hide the real file as show it.
  Validation is by attempting the parse, not by mime/extension inspection beforehand — a bad pick
  becomes the same `Error` state a malformed GPX would.
- Share-intent mime types: register `MainActivity`'s intent-filter for the concrete mime types
  GPX-sharing apps are actually observed to use (`application/gpx+xml`, `application/octet-stream`)
  rather than `*/*` (too broad — would make Wayprint claim every share target on the device) or
  `pathPattern`-based matching (doesn't reliably match opaque `content://` URIs). Revisit if a real
  app's share sheet doesn't surface Wayprint.
- `Bitmap`-backed drawing isn't unit-testable in this project (M4.2's precedent: host tests run with
  `isReturnDefaultValues = true` and no Robolectric, so `android.graphics` calls return stubbed
  defaults, not real pixels) — M5.3/M5.4's Verify lines are on-device only, same as M4.2.
- `MediaStore` save target: `MediaStore.Images.Media.insertImage(...)` (the simple deprecated
  helper, not manual `ContentValues`/`RELATIVE_PATH`) — works unmodified across this project's full
  `minSdk=24..compileSdk=37` range (`gradle/libs.versions.toml`), at the cost of no custom
  "Wayprint" subfolder (images land in the default Pictures collection) and needing
  `WRITE_EXTERNAL_STORAGE` (`maxSdkVersion="28"`) requested only below API 29 (scoped storage from
  29 on needs no permission for an app's own inserted media). A version-branching
  `ContentValues`+`RELATIVE_PATH` approach would allow a custom subfolder but is real extra
  complexity for a folder name CLAUDE.md doesn't actually require — simplicity first. `minSdk`
  itself stays at 24, matching `../wallosmobile`/`../TaigaMobileNova`'s template value — not raised
  just to dodge the legacy permission path.

- [x] **M5.1** — `WayprintUiState` (`Empty` / `Loading` / `Success(WayprintLayout)` /
  `Error(message)`) and a `WayprintViewModel` (Koin-injected, in `feature:wayprint:ui`) exposing a
  `loadFromUri(Uri)`-style entry point that opens the `Uri` via `ContentResolver.openInputStream`
  and runs `buildWayprintLayout` off the main thread. A `WayprintScreen` composable: `Empty` shows
  an "Import GPX" button wired to `ActivityResultContracts.GetContent("*/*")`; `Loading` shows a
  spinner; `Success` shows the existing `WayprintCanvas`; `Error` shows the message plus a retry
  button. `composeApp`'s `WayprintAppContent` becomes `WayprintTheme { WayprintScreen() }`; delete
  `DemoRoute.kt`/`DEMO_GPX` (now orphaned, see shared context).
  **Note:** `WayprintUiState` is one `data class` (`isLoading: Boolean = false`,
  `layout: WayprintLayout? = null`, `error: String? = null`) rather than a sealed hierarchy —
  first tried as a `sealed interface` with exclusive `Empty`/`Loading`/`Success`/`Error`
  variants, but the user pushed back wanting a unified solution: `../wallosmobile`'s own
  `UiState`s (e.g. `CurrenciesUiState`) all use this same flags-on-one-data-class shape, and
  root `CLAUDE.md`'s reference-projects guidance is to port their patterns rather than
  introduce a one-off. Reworked to match — `Empty` is just the all-defaults case, `WayprintScreen`
  branches on `isLoading`/`error`/`layout` in that priority order instead of `is` checks.
  **Note:** `WayprintViewModel` takes `android.content.Context` straight in its constructor
  (Koin's `androidContext()` registers it) rather than a narrower seam, same precedent as M4/M5's
  shared context (Android-only platform types go directly in `commonMain`, not behind an
  expect/actual, since only one KMP target exists). `loadFromUri` catches any `Exception` from
  `buildWayprintLayout` (malformed XML, a non-numeric `lat`/`lon`, an empty track) and maps it to
  `WayprintUiState(error = ...)` — there's no narrower exception type to catch instead, since a bad
  GPX file can fail in several unrelated ways through the parse/RDP/projection pipeline.
  **Note:** a Koin definition in one Gradle module is invisible to another module's
  `@ComponentScan`, confirmed against `../wallosmobile`'s per-feature-module pattern — added
  `WayprintUiModule` (`@Module @ComponentScan("com.grappim.wayprint.feature.wayprint.ui")`) in
  `feature:wayprint:ui` and listed it in `composeApp`'s `AppModule(includes = [...])`. `KoinGraphTest`
  needed `verify(extraTypes = listOf(Context::class))` — `Context` comes from `androidContext()` in
  `WayprintApp`, not from any scanned definition, same as wallosmobile's own `KoinGraphTest`.
  **Note:** `androidx.activity:activity-compose` (for `rememberLauncherForActivityResult`/
  `ActivityResultContracts`, already in the version catalog since M0.1's seed but previously only
  depended on by `androidApp`) added to `feature:wayprint:ui`'s `commonMain.dependencies` — needed
  for the file-picker launcher in `WayprintScreen`.
  **Verify:** on-device — pushed the M1 fixture GPX (`fixture.gpx`) and a non-GPX text file
  (`notgpx.txt`) onto the emulator; picking `fixture.gpx` via the file picker renders the same
  story image M4.3 confirmed (background, route line, Start/Finish markers, "26.2 km" label), now
  from a real picked file instead of the hardcoded demo — screenshot confirmed. Picking the non-GPX
  file renders the `Error` state (message plus a Retry button) instead of a crash — screenshot
  confirmed, `adb logcat` shows no `FATAL EXCEPTION` for the whole session. `./gradlew build` (same
  pre-existing M0.3/M0.4 F-Droid-signing exclusions) passes project-wide, including the updated
  `KoinGraphTest`.

- [x] **M5.2** — `AndroidManifest.xml` intent-filter on `MainActivity` for `ACTION_SEND` (and
  `ACTION_VIEW`, for opening a `.gpx` directly from a file manager) matching the mime types in
  shared context. `MainActivity` extracts the shared/viewed `content://` `Uri` (`ACTION_SEND`'s
  `EXTRA_STREAM`, or `ACTION_VIEW`'s data `Uri`) in `onCreate`/`onNewIntent` and forwards it into
  `WayprintViewModel`'s same `loadFromUri` entry point M5.1 built — no parsing logic duplicated.
  **Note:** one combined `<intent-filter>` on `MainActivity` carries both `SEND`/`VIEW` actions,
  `category.DEFAULT`, and the two `<data android:mimeType>` entries from shared context — rather
  than two separate filters — since both actions share the identical category/data spec.
  `MainActivity` also gained `android:launchMode="singleTop"`: without it, a share/open arriving
  while the app is already in the foreground would spawn a second `MainActivity` instance instead
  of routing through `onNewIntent`, which the step's own text requires handling.
  **Note:** to get the *same* `WayprintViewModel` instance `WayprintScreen`'s `koinViewModel()`
  resolves (so a `MainActivity`-forwarded `loadFromUri` call updates the state that screen is
  already collecting), `MainActivity` obtains it via Koin's own `org.koin.androidx.viewmodel.ext
  .android.viewModel()` Activity delegate (from `koin-android`, already a dependency) rather than
  a second/duplicate resolution path — both go through `ViewModelProvider(activity, factory)` with
  the same default class-based key, backed by the same `ComponentActivity`'s `ViewModelStore`,
  confirmed on-device (see Verify) rather than assumed. `androidApp/build.gradle.kts` gained
  `implementation(project(":feature:wayprint:ui"))` (for the `WayprintViewModel` class reference —
  `composeApp`'s own dependency on it is `implementation`, not `api`, so not visible transitively)
  and `implementation(libs.androidx.core.ktx)` (for `androidx.core.content.IntentCompat`, used to
  read `Intent.EXTRA_STREAM` as a typed `Uri` without the API-33-deprecated raw
  `getParcelableExtra` overload).
  **Note:** three tooling frictions hit while proving this on-device (adb `content query --where`
  quoting, `am start --grant-read-uri-permission` unable to forward access to a shell-inserted
  MediaStore row, one mis-tapped action-bar icon caught before confirming) — logged in
  `docs/frictions.md` rather than repeated here.
  **Verify:** on-device — pushed the M1 fixture GPX to `/sdcard/Download/fixture.gpx` and drove the
  on-device Files app (`com.google.android.documentsui`) for both real flows rather than
  synthesizing intents directly (a shell-issued `am start --grant-read-uri-permission` against a
  MediaStore-inserted row was rejected with "has no access to" — an adb-simulation limitation, not
  an app bug; see `docs/frictions.md`): (1) selected `fixture.gpx` → **Share** → **Wayprint** in the
  system share sheet (confirmed via `uiautomator dump` this is a real `ACTION_SEND` chooser, not a
  synthesized intent) — screenshot confirms the story image renders identically to M5.1's picker
  path (background, route line, hollow Start / filled Finish markers, "26.2 km" label); (2) same
  file → overflow menu → **Open with** → **Wayprint** — `dumpsys activity activities` confirmed the
  resulting `Intent { act=android.intent.action.VIEW dat=content://.../document/msf%3A37
  typ=application/octet-stream cmp=com.grappim.wayprint.debug/com.grappim.wayprint.MainActivity }`
  reached `MainActivity`, and the screenshot shows the identical rendered story image. `adb logcat`
  showed no `FATAL EXCEPTION` across either flow. `./gradlew build` (same pre-existing M0.3/M0.4
  F-Droid-signing exclusions) passes project-wide, including `detekt`/`ktlintCheck`/lint on the
  changed `androidApp` module.

- [x] **M5.3** — Factor `WayprintCanvas`'s draw body into a reusable `fun
  DrawScope.drawWayprintStory(layout, preset)` (the existing on-screen `Canvas { }` calls it — same
  visual output, no behavior change). Add a headless `fun renderWayprintStoryBitmap(layout,
  preset): Bitmap`, using `CanvasDrawScope` + `Bitmap.createBitmap` at the preset's exact canvas
  size (1080×1920, no fit-scale/letterbox — that's only for on-screen display). `WayprintScreen`'s
  `Success` state gains an "Export" button that calls it (the resulting `Bitmap` is unused until
  M5.4).
  **Note:** `WayprintCanvas` now just computes `fitScale`, applies the `withTransform` letterbox,
  and calls `drawWayprintStory` inside it — `drawWayprintStory` itself takes only `layout`/`preset`
  and draws in the preset's own canvas-space coordinates, with no transform of its own, so
  `renderWayprintStoryBitmap` can call it directly at full size with no letterboxing. Hex-color
  parsing moved from `WayprintCanvas`'s composable body into `drawWayprintStory`, since it's now the
  function both callers share.
  **Note:** `renderWayprintStoryBitmap` builds the `Canvas` via `CanvasDrawScope().draw(density =
  Density(1f), layoutDirection = LayoutDirection.Ltr, canvas =
  androidx.compose.ui.graphics.Canvas(android.graphics.Canvas(bitmap)), size = ...)` — the
  `androidx.compose.ui.graphics.Canvas(...)` factory function needed a fully-qualified reference
  since `androidx.compose.foundation.Canvas` (the composable) is already imported under the same
  simple name.
  **Note:** the "Export" button is placed as a `BottomCenter`-aligned overlay in a `Box` wrapping
  the existing full-size `WayprintCanvas`, rather than a separate row below it — the step's own text
  didn't specify a layout and this keeps the canvas at its full letterboxed size rather than
  shrinking it to make room for a button row.
  **Note:** no logging framework exists in this project yet — added one `android.util.Log.d`
  call in the Export button's `onClick` (`"Exported ${bitmap.width}x${bitmap.height} bitmap"`) so
  this step's own Verify line ("confirm via logcat... a non-null 1080×1920 Bitmap") has something
  concrete to grep for; the `Bitmap` itself stays otherwise unused per the step's own text.
  **Note:** on-device verification hit a tap-coordinate friction (eyeballed the Export button's
  position from a screenshot, missed by ~500px) — logged in `docs/frictions.md` rather than
  repeated here; re-derived the tap from a `uiautomator dump` instead.
  **Verify:** on-device — installed `:androidApp:assembleGplayDebug` on `Medium_Phone_API_36.1`,
  picked the M5.1 fixture GPX via the file picker (same story image as M5.1/M5.2), tapped Export:
  `adb logcat` shows `WayprintScreen: Exported 1080x1920 bitmap` and no `FATAL EXCEPTION` anywhere in
  the session. `./gradlew :feature:wayprint:ui:build` (detekt, ktlintCheck, testAndroidHostTest,
  kover) passes; `./gradlew build` (same pre-existing M0.3/M0.4 F-Droid-signing exclusions) passes
  project-wide.

- [x] **M5.4** — Save M5.3's `Bitmap` via `MediaStore.Images.Media.insertImage` (see shared
  context), requesting `WRITE_EXTERNAL_STORAGE` (`maxSdkVersion="28"`) only when
  `Build.VERSION.SDK_INT < 29`. Pass the resulting `content://` `Uri` to `Intent.ACTION_SEND` +
  `Intent.createChooser` for the share sheet. Wire as one action on the Export button (save-and-
  share together, matching CLAUDE.md's "saved/shared via `MediaStore`/share sheet" wording — not
  two separate buttons).
  **Note:** `WayprintViewModel` gained `exportAndShare(bitmap: Bitmap)` (the natural place — it
  already holds `context` per M5.1's precedent, and `context.contentResolver` is exactly what
  `insertImage` needs), run on `Dispatchers.IO`: `insertImage` returns the new row's `content://`
  `Uri` as a `String?` (`?: return@launch` if the write failed — no narrower handling than that,
  same "no error handling for impossible scenarios" spirit as `loadFromUri`), then builds an
  `ACTION_SEND` intent (`type = "image/png"`, `EXTRA_STREAM` = that `Uri`,
  `FLAG_GRANT_READ_URI_PERMISSION` so the receiving app can actually read it) and starts
  `Intent.createChooser(...)` via the held `context`. Since that `context` is the `Application`
  instance (M0.4/M5.1's `androidContext()` registration), the chooser intent also needs
  `FLAG_ACTIVITY_NEW_TASK` — starting an activity from a non-`Activity` `Context` requires it.
  **Note:** the permission check/request lives in `WayprintScreen` (not the ViewModel), alongside
  its existing `pickGpx` launcher: a `rememberLauncherForActivityResult(RequestPermission())`, a
  `remember { mutableStateOf<Bitmap?>(null) }` holding the bitmap while the request is in flight,
  and the Export button's `onClick` branching on `Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
  ContextCompat.checkSelfPermission(...) != PERMISSION_GRANTED` — requesting first if so, calling
  `viewModel.exportAndShare` directly otherwise. `feature:wayprint:ui/build.gradle.kts` gained
  `implementation(libs.androidx.core.ktx)` (already in the version catalog since M0.1's seed, not
  previously a dependency of this module) for `ContextCompat`.
  **Note:** M5.3's `Log.d(TAG, "Exported ...")` call (and its `TAG` constant, `android.util.Log`
  import) is now orphaned — the bitmap it logged about is put to real use here instead of just
  being measured — deleted per "remove what your change orphaned."
  **Note:** `androidApp/src/main/AndroidManifest.xml` gained `<uses-permission
  android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />` above
  the `<application>` block — the manifest had no `<uses-permission>` entries before this step.
  **Note:** on-device verify ran on `Medium_Phone_API_36.1` (API 36) — well above the SDK 29 cutoff
  — so only the `SDK_INT >= 29` code path (straight to `insertImage`, no permission prompt) was
  actually exercised. The `SDK_INT < 29` runtime-permission-request branch compiles and follows the
  standard `ActivityResultContracts.RequestPermission()` pattern already used elsewhere in this app
  (`pickGpx`'s `GetContent()`), but is unverified on a real pre-29 device/AVD — noted here rather
  than claimed as confirmed, per the emulator-testing skill's own guidance not to call an
  API-gated path verified when only tested on a modern AVD.
  **Note:** two tooling frictions hit while proving this on-device (a mis-tapped file-picker row
  from mixed screenshot coordinate spaces, and `content query --projection col1,col2,col3` — comma
  syntax — rejected as an invalid column) — logged in `docs/frictions.md` rather than repeated
  here.
  **Verify:** on-device — installed `:androidApp:assembleGplayDebug` on `Medium_Phone_API_36.1`,
  picked the M5.1 fixture GPX via the file picker, tapped Export: the system share sheet appeared
  ("Sharing image", targets including Quick Share/Print/Drive/Maps/Messages) with the rendered
  story visible behind it — screenshot confirmed. Selecting the Print target resolved to
  `com.android.bips/.ImagePrintActivity` (confirmed via `adb logcat`'s `ActivityTaskManager`/
  `ChooserHelper` lines), proving the granted `content://` `Uri` was actually readable by another
  app, not just attached. `adb shell content query --uri
  content://media/external/images/media` confirmed the saved row
  (`_display_name=wayprint-<timestamp>.jpg`, `_data=/storage/emulated/0/Pictures/...`,
  `resolution=1080×1920`, `mime_type=image/jpeg`, `bucket_display_name=Pictures`,
  `owner_package_name=com.grappim.wayprint.debug`); pulling that file and opening it confirmed a
  valid 1080×1920 RGB JPEG matching the on-screen story (background, route line, Start/Finish
  markers, "26.2 km" label). `adb logcat -d | grep -i "FATAL EXCEPTION"` was empty across the whole
  session. `./gradlew :feature:wayprint:ui:build` (detekt, ktlintCheck, testAndroidHostTest, kover)
  and `./gradlew build` (same pre-existing M0.3/M0.4 F-Droid-signing exclusions) both pass
  project-wide.

## M6 — distribution

F-Droid/Play flavor dimension (`STORE`, `.fdroid` applicationIdSuffix), `fastlane/` skeleton
copied and adapted, CI. Broken down below into fixing the ported-but-still-wallosmobile-named
signing config, the `fastlane/` metadata skeleton, and a build/test/lint CI gate.

Shared context for all of M6 (re-derive nothing below from scratch):
- The `STORE` flavor dimension (`GPLAY`/`FDROID`, `.fdroid` applicationIdSuffix) already exists —
  ported in M0.1 as part of `AndroidApplicationConventionPlugin`
  (`build-logic/convention/.../AppFlavors.kt`) and confirmed working since M0.2. Nothing in M6
  needs to add or change the flavor dimension itself.
- What M0.2/M0.3's notes flagged and deferred to M6: `AndroidApplicationConventionPlugin.kt`'s
  `configureAppSigningConfigs()` still points at wallosmobile's own identity — keystore filenames
  `wallos_mobile_{gplay,fdroid}.jks`/`wallos_mobile_fdroid_debug.jks` and env vars
  `WALLOS_STORE_PASS_*`/`WALLOS_ALIAS_*`/`WALLOS_KEY_PASS_*` — never renamed for Wayprint. This is
  why `./gradlew build` has carried `-x :androidApp:assembleFdroidDebug -x
  :androidApp:assembleFdroidRelease -x :androidApp:assembleGplayRelease -x
  :androidApp:bundleFdroidDebug -x :androidApp:bundleFdroidRelease -x
  :androidApp:bundleGplayRelease` since M0.3 — every step's Verify line has been dodging the
  wallosmobile-named signing configs those variants resolve, not a Wayprint-specific gap in the
  variants themselves.
- User decision (2026-09-02, this session): M6.1 renames the signing plumbing to Wayprint's own
  names and gets `assembleFdroidDebug` building again (a debug-only identity, not sensitive — see
  M6.1). It does **not** generate the real `gplay`/`fdroid` **release** keystores — those are the
  app's permanent publishing identity on each store, a hard-to-reverse decision (losing or
  mis-issuing one means the app can never be updated under that identity again) that belongs to
  whenever the user is actually ready to publish, not to this scaffolding step. So
  `assembleGplayRelease`/`assembleFdroidRelease`/their `bundle*` equivalents keep the same
  exclusions after M6 that they've carried since M0.3 — M6 does not close that gap, it just stops
  the *debug* half of it from blocking CI.
- User decision (2026-09-02, this session): CI (M6.3) is a build/test/lint gate only — no Codecov
  upload (would need an external Codecov account/project this repo doesn't have) and no per-flavor
  Android/Compose lint step (`:androidApp:lintFdroidDebug`/`lintGplayDebug`), unlike
  wallosmobile's `ci.yml`. Also out of scope for all of M6: `guardrails.yml` (a wallosmobile-
  specific commit-message tripwire check, not a Wayprint convention) and
  `release-prepare.yml`/`release.yml`/`release-finalize.yml` (version-bump + Play/F-Droid publish
  automation via `fastlane supply`) — Wayprint has no real release keystore or store listing yet
  (see the decision above), so wiring publish automation now would be exercising a pipeline with
  nothing real to publish. Flagged in `IMPLEMENTATION_PLAN.md` §9 as future work for whenever the
  app is actually ready to ship.
- Reference: `../wallosmobile/.github/workflows/ci.yml`, `../wallosmobile/fastlane/` (`Fastfile`,
  `Gemfile`, `metadata/android/en-US/`) — port the *shape*, adapt the content (package name, store
  listing text) to Wayprint, same "reference-only, coupled to their own app" caveat as M2's shared
  context gave for `uikit`.
- `*.jks` is already gitignored (ported in M0.1) — no `.gitignore` change needed for M6.1's local
  debug keystore.
- This repo already has a GitHub remote (`github.com/Grigoriym/Wayprint`) and an authenticated
  `gh` CLI — M6.3's CI workflow can be verified with a real Actions run, not just YAML inspection.

- [x] **M6.1** — Rename the ported signing config to Wayprint's own identity:
  `AndroidApplicationConventionPlugin.kt`'s `configureAppSigningConfigs()` — keystore filenames
  `wallos_mobile_${flavor.title}.jks` → `wayprint_${flavor.title}.jks`,
  `wallos_mobile_fdroid_debug.jks` → `wayprint_fdroid_debug.jks`, and every `WALLOS_*` env var →
  the equivalent `WAYPRINT_*` name (`WAYPRINT_STORE_PASS_$envSuffix`,
  `WAYPRINT_ALIAS_$envSuffix`, `WAYPRINT_KEY_PASS_$envSuffix`, plus the three
  `WAYPRINT_*_FDROID_DEBUG` ones). Generate a real local `wayprint_fdroid_debug.jks` via `keytool`
  (self-signed, debug-only — its only job is a stable-across-builds identity for F-Droid's debug
  channel, not a store-facing credential, same as wallosmobile's own one) and export the matching
  `WAYPRINT_*_FDROID_DEBUG` env vars locally so `:androidApp:assembleFdroidDebug` builds. Do
  **not** generate `wayprint_gplay.jks`/`wayprint_fdroid.jks` (real release keystores) — see shared
  context; `assembleGplayRelease`/`assembleFdroidRelease`/their `bundle*` equivalents keep their
  existing exclusion from `./gradlew build`.
  **Note:** a keystore was generated at the repo root (`wayprint_fdroid_debug.jks`, matching
  `configureAppSigningConfigs()`'s `File(rootDir, ...)` lookup) via `keytool -genkeypair` to prove
  this step's Verify line, then deleted at the user's explicit request in the same session: the
  user wants to generate every keystore — debug and release alike — themselves rather than have
  one AI-generated, so no `wayprint_fdroid_debug.jks` exists on disk as of this step's close.
  Already covered by the existing repo-root `*.jks` gitignore entry (M0.1) regardless. A future
  session (whenever the user has generated their own debug keystore) or M6.3 itself is where the
  matching `WAYPRINT_*_FDROID_DEBUG` env vars/secrets get set — not this step.
  **Verify:** `:androidApp:assembleFdroidDebug` succeeds locally with the new keystore/env vars —
  confirmed (`BUILD SUCCESSFUL`, a build that has been exclusion-listed since M0.3). `./gradlew
  build -x :androidApp:assembleFdroidRelease -x :androidApp:assembleGplayRelease -x
  :androidApp:bundleFdroidRelease -x :androidApp:bundleGplayRelease` (M0.3's exclusions, now
  narrowed to just the release variants per shared context) passes project-wide — confirmed.
  `detekt`/`ktlintCheck` clean on `:build-logic:convention` — confirmed. `grep -ri wallos
  build-logic/` finds nothing — confirmed (empty, exit code 1).

- [x] **M6.2** — `fastlane/` skeleton: `Fastfile` (a `test` lane running `./gradlew allTests`,
  ported as-is from wallosmobile's — no Wayprint-specific lane needed yet), `Gemfile` (`gem
  "fastlane"`), and `fastlane/metadata/android/en-US/` — `title.txt` ("Wayprint"),
  `short_description.txt` and `full_description.txt` (Wayprint's own text, drawn from root
  `CLAUDE.md`'s Concept/MVP scope — not wallosmobile's copy, adapted), `changelogs/1.txt`
  ("Initial release."). No `images/` (screenshots/feature graphic) yet — root
  `CLAUDE.md`/IMPLEMENTATION_PLAN.md §7 already defers those to whenever there's a real build to
  screenshot.
  **Note:** `Fastfile`/`Gemfile` are byte-for-byte ports of wallosmobile's (only the `desc`/lane
  name/gradle task carry over — nothing in either file is app-specific to begin with).
  `full_description.txt` covers Import/Route art/Export/Fully offline, drawn from root
  `CLAUDE.md`'s Concept and MVP scope sections (GPX-only input via picker/share-intent from
  Strava/Komoot/OsmAnd, automatic label placement, `MediaStore`/share-sheet export, no
  network/account) — not wallosmobile's server-client wording, which doesn't apply here.
  **Note:** this environment has no Ruby/fastlane installed (`ruby`/`gem`/`fastlane`/`bundle` all
  absent; `ruby-full` is available via `apt` but installing it needs `sudo`, a system-wide change).
  Docker is available but there's no ready-made image with both fastlane and this project's
  JDK/Android SDK toolchain (the `test` lane's `gradle(task: "allTests")` shells out to the
  project's own `./gradlew`). Asked the user how to proceed (install ruby-full via apt / user
  installs it themselves / inspect-only); user chose inspect-only — so this step's own Verify
  line's live-execution half (`fastlane android test` actually invoking `./gradlew allTests`) is
  **not** confirmed by an actual run, only by structural inspection. Flagged here rather than
  silently claimed as passing.
  **Verify:** structural inspection only (see note above) — `fastlane/Fastfile`/`Gemfile`/
  `metadata/android/en-US/{title,short_description,full_description}.txt` and
  `changelogs/1.txt` match wallosmobile's file layout exactly (`find fastlane -type f`), and
  `Fastfile`'s `test` lane calls `gradle(task: "allTests")`, the same task `./gradlew allTests`
  invokes directly. `short_description.txt` is 57 characters (`wc -c`), under the 80-character
  F-Droid/Play limit — confirmed. Live `fastlane android test` execution not run — deferred to
  whoever installs Ruby locally, or to M6.3's CI run (which installs its own toolchain in the
  GitHub Actions runner and isn't blocked by this local gap).

- [x] **M6.3** — `.github/workflows/ci.yml`: checkout, JDK 21 (`temurin`), Gradle setup
  (`gradle/actions/setup-gradle`, `validate-wrappers: true`), Android SDK setup
  (`android-actions/setup-android`), restore `wayprint_fdroid_debug.jks` from a base64-encoded
  GitHub Actions secret (mirrors M6.1's local keystore), assemble `fdroidDebug` + `gplayDebug`,
  run `./gradlew allTests`, run `detekt`/`ktlintCheck`, generate the Kover XML report as a build
  artifact (no Codecov upload — see shared context). Triggered on push/PR to `main`. Add the
  actual `wayprint_fdroid_debug.jks`'s base64 contents and its three `WAYPRINT_*_FDROID_DEBUG`
  values as GitHub Actions repo secrets (`gh secret set`) so the restore step has something to
  restore.
  **Note:** `.github/workflows/ci.yml` written and committed (checkout, JDK 21, Gradle/Android SDK
  setup, `wayprint_fdroid_debug.jks` restore from `WAYPRINT_FILE_FDROID_DEBUG`, `assembleFdroidDebug`
  + `assembleGplayDebug`, `allTests`, `detekt`/`ktlintCheck`, `koverXmlReport` uploaded as a build
  artifact — no `google-services.json` restore/Codecov upload/per-flavor lint step, none of which
  apply to Wayprint per this milestone's shared context).
  **Note:** triggers on push/PR to `master`, not `main` as this step's own text says — `gh repo view`
  and `git branch -a` confirm the actual (and only) branch/default is `master`, matching M0.1's own
  note that it deliberately set the default branch to `master` to match wallosmobile's convention;
  this step's "main" wording is stale relative to that decision.
  **Note (2026-09-02):** the user generated `wayprint_fdroid_debug.jks` (plus the release
  `wayprint_fdroid.jks`/`wayprint_gplay.jks`, not required by this step) and set all twelve
  `WAYPRINT_*` GitHub Actions secrets (`gh secret list` confirms `WAYPRINT_FILE_FDROID_DEBUG`,
  `WAYPRINT_STORE_PASS_FDROID_DEBUG`, `WAYPRINT_ALIAS_FDROID_DEBUG`,
  `WAYPRINT_KEY_PASS_FDROID_DEBUG` — this step's four — plus the eight `_FDROID`/`_GPLAY` release
  equivalents for a future release-signing milestone). This unblocked the Verify line below; see
  `docs/revisit.md` (entry removed) for the original blocker.
  **Note:** added a `workflow_dispatch:` trigger to `ci.yml` — needed to manually re-trip the
  workflow for this Verify (its `paths-ignore` skips both docs-only pushes and empty commits, see
  `docs/frictions.md`).
  **Verify:** push a branch (or open a PR) that trips the workflow; `gh run watch` (or the Actions
  tab) confirms a green run — checkout through Kover report all succeed, matching what
  `./gradlew build`'s local exclusions already prove passes offline. `gh secret list` confirms the
  three `WAYPRINT_*_FDROID_DEBUG` secrets plus the base64 keystore secret are present.
  Confirmed: run [33674771429](https://github.com/Grigoriym/Wayprint/actions/runs/33674771429)
  green end-to-end (checkout → both debug APK assembles → `allTests` → `detekt`/`ktlintCheck` →
  Kover report upload); `gh secret list` shows all four `WAYPRINT_*_FDROID_DEBUG` secrets present.

## M7 — editable canvas

Drag labels to reposition them, pick a color scheme, undo — with edits persisted as a draft that
survives an app kill. First growth-roadmap item promoted to a milestone; Android-only, aspect
ratio deferred. Broken down below into the domain model, drag/undo, the color-scheme picker, and
draft persistence.

Shared context for all of M7 (re-derive nothing below from scratch):
- User decision (2026-09-02, this session): aspect-ratio picking stays out of M7 — see
  `IMPLEMENTATION_PLAN.md` §9 "M7 editable-canvas scope" for the full reasoning. M7 works within
  the existing single `DEFAULT_STORY_PRESET` canvas shape only.
- User decision (2026-09-02, this session): color picking is a small fixed preset list, not a
  free-form picker. Source the list from `core:gpx`'s `dayPalette()` (ported M1.3, unused since —
  no caller anywhere outside its own file/tests) — its 5 hues become 5 line-color variants against
  `DEFAULT_STORY_PRESET`'s existing background/text colors, which stay fixed across every scheme.
- User decision (2026-09-02, this session): edits persist as a draft surviving a full app kill,
  storing the *raw GPX bytes* (not the source `content://` `Uri` — a share-intent grant often
  isn't persistable, and even a file-picker `Uri`'s persisted-permission grant is an extra failure
  mode raw bytes avoid). This is `core:storage`'s first concrete need (deferred since M0, §4) and
  pulls in the `wayprint.kmp.serialization` convention plugin (deferred since M0, §5).
- Current model shape (re-read before touching): `StoryPreset` (`feature:wayprint:domain`)
  bundles canvas dimensions *and* the 3 hex colors into one object — M7.1 needs to split those so
  multiple color schemes can apply to the same canvas shape. `PlacedLabel.boundingBox` already
  exists (computed in `LabelPlacement.kt`'s private `boundingBox()`/`estimatedSize()`) — reuse
  those for a new reposition function rather than duplicating the size estimate.
  `WayprintViewModel.loadFromUri` currently reads the `Uri` once via `contentResolver
  .openInputStream` and discards it; `WayprintUiState` is a flat `(isLoading, layout, error)` with
  no edit/undo state.
- `CanvasFit`/`fitScale` (`feature:wayprint:ui`) already computes the on-screen letterbox
  transform (offset + scale) `WayprintCanvas` applies — a drag gesture's screen-space touch point
  needs the inverse of that same transform to hit-test against `PlacedLabel.boundingBox` in canvas
  space.
- Reference: `elbe-story-actual.html`'s hand-tuned label nudges (already the model for M3.2's
  compass candidates) has no drag/undo equivalent — this is genuinely new ground, not a port.

- [x] **M7.1** — `feature:wayprint:domain`: split `StoryPreset` into canvas-shape fields (width/
  height/routeBox/margins) plus a `ColorScheme(backgroundColor, lineColor, textColor)`, add a
  `PRESET_COLOR_SCHEMES` list (5 entries, `core:gpx`'s `dayPalette(5)` for `lineColor`,
  `DEFAULT_STORY_PRESET`'s existing background/text held fixed across all 5), and a pure
  `PlacedLabel` reposition function (e.g. `fun PlacedLabel.movedTo(x: Double, y: Double):
  PlacedLabel`, recomputing `boundingBox` via `LabelPlacement.kt`'s existing size-estimate/
  bounding-box logic — expose what's currently private as needed). Unit tests for the reposition
  function and the preset list (5 entries, background/text identical across all, line colors match
  `dayPalette(5)`'s output).
  **Note:** `movedTo` didn't need any visibility change — it lives in `LabelPlacement.kt` itself,
  so it calls the existing `private` `estimatedSize`/`boundingBox` helpers directly. The
  `StoryPreset` split broke `feature:wayprint:ui`'s two callers (`WayprintCanvas`/
  `drawWayprintStory`/`renderWayprintStoryBitmap` in `WayprintCanvas.kt`, and `WayprintScreen.kt`)
  since they read colors off `preset`; both were updated to take a separate `ColorScheme` param,
  with `WayprintScreen` passing `PRESET_COLOR_SCHEMES.first()` as today's still-hardcoded default
  (M7.3 is what actually wires a picker) — required to keep the project building, not a new
  feature.
  **Verify:** `feature:wayprint:domain`'s tests pass (`./gradlew
  :feature:wayprint:domain:testAndroidHostTest` — this is an Android-only KMP module, so plain
  `:test` is ambiguous, see `docs/frictions.md`), `detekt`/`ktlintCheck` clean — confirmed.
  `WayprintLayoutTest`/other existing tests still pass unmodified — confirmed, confirms the
  `StoryPreset` split didn't change `buildWayprintLayout`'s behavior. `feature:wayprint:ui`'s
  `compileAndroidMain`/`ktlintCheck`/`detekt` also confirmed clean (not this step's own module,
  but needed since its two callers changed).

- [x] **M7.2** — `feature:wayprint:ui`: drag-to-reposition on `WayprintCanvas` via
  `Modifier.pointerInput`/`detectDragGestures`, inverse-transforming the drag's screen-space
  offset through `CanvasFit`'s scale/offset to canvas space, hit-testing against each
  `PlacedLabel.boundingBox` to pick which label is being dragged, then calling M7.1's reposition
  function on drag. `WayprintViewModel`/`WayprintUiState` gain an in-memory undo stack (push the
  pre-drag `WayprintLayout` before applying each drag's final position; an Undo button pops and
  restores it) — no persistence yet, this step is drag+undo only.
  **Note:** the undo-stack push/pop logic (and the drag-start/live-move logic) ended up as pure
  extension functions on `WayprintUiState` (`WayprintUiState.kt`: `dragStarted`/`labelMoved`/
  `dragEnded`/`undone`) rather than methods tested through `WayprintViewModel` directly — this
  needed no `Context` fake at all (the module has no Robolectric/`Context`-faking test infra yet),
  and `WayprintViewModel`'s new methods (`onLabelDragStart`/`onLabelDragged`/`onLabelDragEnd`/
  `undo`) are thin one-line delegations to them. Added `Rect.contains(x, y)` to `feature:wayprint:
  domain` for the hit-test itself (with unit tests), reusing the existing `Rect`/`PlacedLabel.
  boundingBox` shape rather than a new UI-side geometry type. `WayprintCanvas`'s drag tracks
  position as running deltas local to the gesture (not read back off the `layout` parameter) since
  `pointerInput` is keyed on `preset` only — keying it on `layout` too would restart the gesture
  (and cancel an in-progress drag) on every position update; `rememberUpdatedState(layout)` is used
  only for the one-time hit-test at drag start.
  **Verify:** `feature:wayprint:domain:testAndroidHostTest`/`feature:wayprint:ui:testAndroidHostTest`
  pass, `detekt`/`ktlintCheck` clean on both modules — confirmed. Emulator check (per the
  `emulator-testing` skill, `Medium_Phone_API_36.1`, `gplay` debug build, `feature:wayprint:
  domain`'s `04 Riesa - Meissen.gpx` fixture pushed to `/sdcard/Download`) — loaded the GPX,
  dragged the "26.2 km" distance label to a new position: the render updated live and the label
  stayed at the new position on release, the Undo button appeared; tapping Undo snapped it back to
  the pre-drag position and the button disappeared — confirmed. See `docs/revisit.md` for a
  real-device UX gap found during this check (the label's drag hit-box is much smaller than its
  rendered text) and `docs/EMULATOR_TESTING.md` for the coordinate-math gotcha that made it hard to
  verify by eyeballing screenshots alone.

- [x] **M7.3** — `feature:wayprint:ui`: color-scheme picker — a row of swatches (one per
  `PRESET_COLOR_SCHEMES` entry) in `WayprintScreen`, selecting one re-renders `WayprintCanvas`
  with that scheme's colors. Scheme changes also push onto M7.2's undo stack.
  **Note:** M7.2's undo stack held bare `WayprintLayout`s, which can't also carry a scheme
  selection, so it's now `List<EditSnapshot>` (`EditSnapshot(layout, colorSchemeIndex)`) —
  `dragStarted`/`dragEnded`/`undone` all updated to snapshot/restore both together, and the old
  `dragStartLayout` field renamed to `dragStartSnapshot` to match. `WayprintUiState` gained
  `colorSchemeIndex: Int = 0`; `colorSchemeSelected(index)` pushes the pre-change snapshot then
  updates it, same shape as `dragEnded`. `WayprintViewModel.onColorSchemeSelected` is a one-line
  delegation, matching M7.2's other edit methods. `WayprintScreen` reads
  `PRESET_COLOR_SCHEMES[uiState.colorSchemeIndex]` for both the live `WayprintCanvas` and the
  Export bitmap render (previously both hardcoded to `PRESET_COLOR_SCHEMES.first()`). The swatch
  row reuses `WayprintCanvas.kt`'s existing `parseHexColor` (made non-private) rather than
  duplicating hex parsing. ktlint's compose-rules flagged the swatch callback as
  `onSelected`/past-tense — renamed to `onSelect` (present tense, matching `onClick`-style
  naming) to pass `detekt`.
  **Verify:** `feature:wayprint:ui:testAndroidHostTest`/`feature:wayprint:domain:testAndroidHostTest`
  pass (added `colorSchemeSelected`/undo-after-scheme-change cases to `WayprintUiStateTest`),
  `detekt`/`ktlintCheck` clean — confirmed. Emulator check (`Medium_Phone_API_36.1`, `gplay`
  debug build, `feature:wayprint:domain`'s `04 Riesa - Meissen.gpx` fixture) — loaded the GPX,
  tapped through all 5 swatches: route line/markers/labels recolored live each time and the
  selection border tracked the tapped swatch; tapped Undo after the 5th (pink) selection and it
  reverted to the 4th (purple) scheme, matching the pre-tap state — confirmed.

- [x] **M7.4** — `core:storage` module (new — `wayprint.kmp.library` +
  `wayprint.kmp.serialization` per §5; add it to §4/§5's module tables when scaffolded): persists
  a draft as the raw GPX bytes + the current label position overrides + the selected color-scheme
  id, saved on every edit (debounced or on each undo-stack push) and loaded on `WayprintViewModel`
  init if a draft exists, re-running `buildWayprintLayout` against the stored bytes and reapplying
  the stored overrides/scheme. Decide the actual storage mechanism at this step (e.g. DataStore/
  plain file in `filesDir` — no need for a database for one single draft).
  **Note:** storage mechanism: two plain files in a directory (`DraftStorage(directory: File)`) —
  raw GPX bytes (`wayprint-draft.gpx`) plus a `kotlinx.serialization` JSON sidecar
  (`wayprint-draft.json`) for `DraftMetadata(labelPositions: List<LabelPosition>,
  colorSchemeIndex: Int)` — not DataStore/a database, per this step's own "no need for a database"
  steer. `DraftStorage` takes a `File` directory rather than an Android `Context`: the ViewModel
  passes `context.filesDir`, so the module itself has zero Android dependency — no seam/fake was
  needed for the "hand-written fakes for any Android-specific storage dependency" the Verify line
  anticipated, since there's no Android-specific dependency to fake; `DraftStorageTest` exercises
  the real class against a plain JVM temp directory.
  **Note:** `libs.versions.toml` gained `kotlinx-serialization-json` (`1.11.0`, matching
  wallosmobile's/Taiga's own pinned version for the same Kotlin version); root `build.gradle.kts`
  gained `alias(libs.plugins.kotlin.serialization) apply false`, same "avoid the plugin loading
  twice across subproject classloaders" reasoning M0.2 already established for the other `apply
  false` plugins — `KmpSerializationConventionPlugin` (written, unused, back in M0.1) needed this
  to actually apply for the first time.
  **Note:** `WayprintLayout` isn't persisted directly — `DraftMetadata` only stores each label's
  `x`/`y` (`LabelPosition`, a type `core:storage` owns, not `feature:wayprint:domain`'s
  `PlacedLabel`/`TextAnchor`) — keeps `core:storage` a low-level module with no dependency on the
  higher-level `feature:wayprint:domain`, matching `core:gpx`'s own layering. On restore,
  `WayprintViewModel` reruns `buildWayprintLayout` against the stored bytes (recomputing anchors/
  placement/bounding boxes fresh) then reapplies each stored position via `movedTo` — cheaper than
  serializing bounding boxes too, and self-healing if placement logic ever changes.
  **Note:** `WayprintViewModel` gained a private `draftGpxBytes: ByteArray?` field (not part of
  `WayprintUiState`, which stays UI-relevant state only — a `ByteArray` field on a `data class`
  would also hit Kotlin's data-class-array-equals gotcha for no benefit here). Persisted after
  every state-changing call (`onLabelDragEnd`, `onColorSchemeSelected`, `undo`, and a successful
  `loadFromUri`) via a private `persistDraft()` that fire-and-forgets a `viewModelScope.launch
  (Dispatchers.IO)` — including after `undo()`, so a kill right after undoing doesn't resurrect the
  undone edit, slightly beyond this step's own "on each undo-stack push" phrasing but consistent
  with its "saved on every edit" framing.
  **Note:** emulator verify's drag step needed the same temporary-`Log.d`-in-the-hit-test-path
  technique `docs/EMULATOR_TESTING.md` already documents for M7.2 — confirmed again here that
  `adb shell input swipe`'s synthesized touch-slop consumption makes the recognized
  `onDragStart` position land somewhere other than the raw down point, unpredictably, so hitting
  the label's small bounding box took a few log-guided iterations rather than one analytic
  calculation. Generalized into `agentic-grappim`'s `emulator-testing` skill (left uncommitted for
  review) rather than only noted here, since it applies to any Compose `detectDragGestures` target,
  not just this app's labels.
  **Verify:** `core:storage:testAndroidHostTest` (`DraftStorageTest`: null-when-nothing-saved,
  round-trip round-trips gpx bytes + metadata exactly, a second save overwrites rather than
  appends) passes; `detekt`/`ktlintCheck` clean on `core:storage` and `feature:wayprint:ui` —
  confirmed. `./gradlew build` (same pre-existing M0.3/M0.4 F-Droid-signing exclusions) passes
  project-wide. Emulator check (`Medium_Phone_API_36.1`, `gplay` debug build,
  `feature:wayprint:domain`'s `04 Riesa - Meissen.gpx` fixture): imported the GPX, selected the
  4th (purple) color scheme, dragged the "26.2 km" distance label to a new position, force-stopped
  the app (`adb shell am force-stop`, not just backgrounding), relaunched — the route loaded
  automatically with no Import-GPX prompt, the purple scheme was still selected, and the label was
  at the exact same dragged position — confirmed via before/after screenshots. Also confirmed the
  scheme-only case restores correctly on its own (reinstalling the APK mid-flow, before ever
  dragging, still force-stopped+relaunched with the purple scheme intact).

## M8 — start over

A "Start over" action that clears the persisted draft and returns to the import screen. Second
growth-roadmap/backlog item promoted to a milestone (2026-09-03, user-reported gap: M7.4's
persisted draft means a previously-opened track reappears after every relaunch with no exit from
it — the Import-GPX button only shows when `WayprintUiState.layout == null`). Broken down below
into `DraftStorage.clear()` and the ViewModel/UI wiring (confirmation dialog + top-bar action).

Shared context for all of M8 (re-derive nothing below from scratch):
- User decision (2026-09-03, this session): starting over asks for confirmation first (a dialog:
  "Start over? This discards your current route." Cancel / Start over) rather than discarding
  immediately on tap — there's no undo across a draft clear (M7.2's undo stack is in-memory only
  and gets reset along with everything else), so this is a genuinely irreversible action, per
  CLAUDE.md's caution around hard-to-reverse actions.
- Single-draft model still holds — the recent-tracks-list backlog item (multiple saved drafts) is
  separate and unscoped, so "start over" means clearing the one draft `DraftStorage`
  (`core:storage`, M7.4) holds, not archiving it anywhere first.
- `WayprintViewModel`'s empty/import state is exactly `WayprintUiState()` (its default
  constructor) — `loadFromUri`'s error path already resets to a fresh `WayprintUiState(error =
  ...)`, so "return to the import screen" is just assigning a fresh default `WayprintUiState()`
  and clearing `draftGpxBytes`, no new UI state needed in `WayprintScreen` (the existing `else ->
  Button("Import GPX")` branch already covers `layout == null`).
- `WayprintScreen`'s `CenterAlignedTopAppBar` has no `actions` today — that's the natural home for
  a "Start over" icon action, gated on `uiState.layout != null` (mirrors how Undo/swatches/Export
  are already gated the same way, just in the `Box` below rather than the app bar).

- [ ] **M8.1** — `core:storage`: `DraftStorage.clear()` — deletes `gpxFile`/`metadataFile` if
  present, a no-op if neither exists. Unit tests: save-then-clear-then-load returns `null`;
  calling `clear()` on a directory with nothing saved doesn't throw.
  **Verify:** `./gradlew :core:storage:testAndroidHostTest`, `detekt`, `ktlintCheck` pass.

- [ ] **M8.2** — `feature:wayprint:ui`: `WayprintViewModel.startOver()` — calls
  `draftStorage.clear()`, resets `draftGpxBytes = null` and `_uiState.value = WayprintUiState()`.
  `WayprintScreen`: a "Start over" icon action in the top app bar (visible only when
  `uiState.layout != null`) opens an `AlertDialog` (per the shared-context decision above);
  confirming calls `viewModel.startOver()`, cancelling dismisses with no change.
  **Verify:** `./gradlew :feature:wayprint:ui:testAndroidHostTest`, `detekt`, `ktlintCheck` pass;
  `./gradlew build` (same pre-existing F-Droid-signing exclusions) passes project-wide. Emulator
  check (`emulator-testing` skill): import a GPX, drag a label and/or pick a color scheme, tap
  "Start over", cancel — nothing changes; tap it again and confirm — the app returns to the
  Import-GPX screen; force-stop and relaunch — no draft is restored (Import-GPX shows immediately,
  not the previously-loaded route).

## Backlog (growth roadmap, not milestones yet)

- More input sources: Health Connect / Strava OAuth import; on-device live recording (its own
  milestone-scale scope jump when it happens, not an incremental add-on).
- Multiple layout templates (poster, square post, story) once the story layout is proven —
  includes aspect-ratio picking, deferred out of M7.
- iOS/Desktop targets for `core:gpx` and the Compose `Canvas` renderer.
- A recent-tracks list (2026-09-03, user-reported): implies moving past M7.4's single-draft
  model to multiple saved routes, which in turn implies real navigation — more than one screen
  (a home/recents list, the canvas/edit screen, maybe settings) needing `core:navigation`
  (deferred since M0, §4 — "add when there's a second screen worth routing between"; this would
  be that trigger). Open question, not designed yet: what a "recent" even is here (every past
  import? only explicitly saved ones?) and what the navigation shape should look like — a
  step-break-down of its own once picked up, not a small addition.
- Combining several GPX tracks into one image (2026-09-03, user-reported): open design
  question, not scoped. `dayPalette()` (`core:gpx`, ported M1.3) already exists for per-track/
  per-day coloring and has never had a caller — this is the feature that would finally use it.
  Unresolved: how multiple tracks share one projection/scale (`fitProjection` today fits a
  single track's own bounding box), whether it's one combined distance label or one per track,
  and how label collision-avoidance behaves with 3x as many labels competing for space.
