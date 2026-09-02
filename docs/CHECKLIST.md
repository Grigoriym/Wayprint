# Wayprint checklist

**Current step:** M0.5

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

- [ ] **M0.5** — Symlink agentic-grappim skills into `wayprint/.claude/skills/`: `finalize`,
  `update-gradle-wrapper`, `emulator-testing`, `kover-coverage-sweep`, `compose-stability-audit`,
  `masvs-review` (relative symlinks, `../../../agentic-grappim/skills/<name>` — never copied,
  per that repo's README). Then, with the user's explicit confirmation in that session, add
  Wayprint to the project list in `agentic-grappim/README.md`.
  **Verify:** `ls -la .claude/skills/` shows each as a symlink resolving into
  `../agentic-grappim/skills/`; the skills are invokable (e.g. `/finalize` recognized).

- [ ] **M0.6** — Merge `agentic-grappim/templates/CLAUDE.md.template`'s structure (working
  agreements, settled-decisions table, reference-projects section) into Wayprint's `CLAUDE.md`,
  keeping all existing content (Concept/Origin/MVP scope/Architecture/Naming/Next steps)
  untouched — add the template's sections around it, don't replace anything.
  **Verify:** `CLAUDE.md` still reads correctly top-to-bottom as a single coherent doc; nothing
  from the original content was lost, diffed against git history for this file.

## M1 — `core:gpx`

Port `gpx_route_art.py`'s parsing, RDP simplification, equirectangular projection, and
`day_palette()` to Kotlin, with unit tests proving numeric parity against the Python reference
on the Elbe route's GPX file. Not yet broken into steps — do that at the start of M1, once M0 is
done and the exact Python reference has been re-read fresh (its tolerance/palette values are the
known-good starting point per root `CLAUDE.md`).

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
