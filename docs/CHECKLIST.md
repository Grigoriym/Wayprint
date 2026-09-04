# Wayprint checklist

**Current step:** M15 in progress (add a Desktop/JVM KMP target — see below); M15.1-M15.7 done,
next is M15.8. M0–M14 (the full MVP roadmap, the second layout template, the edit-toolbar decluttering,
and moving Android-only code out of `commonMain`) are complete and archived to
`docs/CHECKLIST_ARCHIVE.md`. M15 is fully buildable/runnable/verifiable on this dev machine; M16
adds iOS after M15 lands, but its verification is capped — this machine is Linux, so an iOS app
can't actually be built or run here, only compiled. Release CI/publish was explicitly deferred by
the user (keystores ready since M6.3) — pick that back up only when they say the app is ready to
ship.

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

## M15 — Add a Desktop (JVM) KMP target

Scoped 2026-09-04, following up on M14: moving the 8 Android-importing files to `androidMain`
made the build honest, but did nothing to make Wayprint portable — it just legalized the status
quo. This milestone does the real work for one second target. **Desktop, not iOS, goes first**:
this dev machine is Linux, so a Desktop (JVM) app is fully buildable, runnable, and verifiable
here the same way every other milestone has been, where an iOS app cannot be — Kotlin/Native can
compile the klib on Linux, but there's no way to produce or run a real `.app`/simulator build
without a Mac + Xcode. M16 (below) adds iOS once this one lands, reusing the interfaces this one
designs rather than guessing them speculatively for three platforms at once.

Reference precedent: `../TaigaMobileNova` already ships a real desktop target — its
`build-logic/.../KmpConfiguration.kt` (the doc comment Wayprint's own `KmpConfiguration.kt` was
copied from) declares `jvm()`, and `composeApp/src/jvmMain/.../TaigaMobileDesktop.kt` +
`composeApp/build.gradle.kts`'s `compose.desktop { application { ... } }` block wire a real
runnable window — no separate `desktopApp` module, `jvmMain` just lives alongside `androidMain`
inside the existing `composeApp`. Taiga's `core:storage` also already solved "no `java.io.File`
on Kotlin/Native" once, with `kotlinx-io-core` (`Path`/`SystemFileSystem`, JetBrains' own KMP I/O
library) — same fix applies here.

**Investigation finding — the non-portable surface is bigger than the 8 files M14 moved.** Those
8 had `android.*` imports; a separate, previously-unscoped set of files has plain JVM-standard
imports that don't exist on Kotlin/Native either (not "Android-only", just never ported off the
JVM stdlib since there was nowhere else to run them):

- `core:gpx`: `Projection.kt`/`DayPalette.kt` use `java.math.BigDecimal`/`RoundingMode` for
  `HALF_EVEN` rounding (ported from the Python reference's `round()` — exact behavior matters,
  the existing golden-value tests pin it). `GpxParser.kt` uses `javax.xml.parsers.DocumentBuilderFactory`
  for GPX's XML — no DOM parser exists on Kotlin/Native. `RouteArt.kt`/`GpxParser.kt` take a
  `java.io.InputStream`.
- `feature:wayprint:domain`: `WayprintLayout.kt`/`WayprintRoute.kt` also take `InputStream`;
  `WayprintLayout.kt` uses `String.format(Locale.ROOT, "%.1f km", ...)` for the distance label.
- `core:storage`: `TracksStorage.kt` is built entirely on `java.io.File` (per CLAUDE.md's settled
  decision to keep it `File`-based rather than `Context`/DataStore-based — that decision stands,
  only the concrete type changes to something portable).
- `feature:wayprint:ui`'s `WayprintCanvas.kt` (one of M14's 8): two of its four `android.*` uses
  (`Bitmap`, `android.graphics.Canvas`) have a drop-in Compose Multiplatform replacement
  (`ImageBitmap`/`Canvas(imageBitmap)`, already portable, not new abstraction) — no `expect`/
  `actual` needed for those two. The other two (`Paint`-based label-halo text via `nativeCanvas`)
  need Compose Multiplatform's `TextMeasurer`/`Paragraph` API instead — `IMPLEMENTATION_PLAN.md`'s
  M4 note already flagged this exact spot as "revisit when/if an iOS/Desktop target is added."
- The other 6 of M14's 8 files (`WayprintViewModel`, `RecentsViewModel`, `WayprintScreen`,
  `RecentsScreen`, `WayprintAppContent`, `WayprintNavHost`, `WayprintEntryProvider`) touch
  `Uri`/`ContentResolver`/`MediaStore`/`FileProvider`/share `Intent`/the pre-Android-Q storage
  permission check/`rememberLauncherForActivityResult` — all genuinely platform-specific, no
  portable equivalent, real `expect`/`actual` needed. Desktop's own actuals: a Swing/AWT
  `JFileChooser`/`FileDialog` for picking + a native "Save As" dialog for export. **Open design
  question, not pre-decided here:** Desktop has no share-sheet equivalent — M15.7 below needs to
  decide what "Share" means on Desktop (hide the action entirely? "Save As" under a different
  label? something else) before implementing its actual.

Sequencing rule for every step below: **land the portability fix while still Android-only-target,
then flip the target switch last (M15.8)**, so every intermediate step keeps `./gradlew build`
green on the one target that exists today — never leave the tree mid-migration with the new
target half-wired and everything red, since each step still needs its own passing Verify line.

- [x] **M15.1** — `core:gpx`: replace `BigDecimal`/`RoundingMode` in `roundToOneDecimal`
  (`Projection.kt`) and `roundHalfEvenToInt` (`DayPalette.kt`) with a hand-written, portable
  `HALF_EVEN` rounding helper (no new dependency — this is arithmetic, not I/O).
  **Verify:** `./gradlew :core:gpx:testAndroidHostTest`, `detekt`, `ktlintCheck` pass — the
  existing golden-value tests (ported from the Python reference) must still pass bit-for-bit,
  proving the replacement rounds identically at the boundary cases `BigDecimal.HALF_EVEN` cares
  about (exact `.5` ties).
  Note: added a shared internal `roundHalfEven(value, scale)` in a new `Rounding.kt` (multiply by
  `10^scale`, round half-to-even, divide back — exact for every tie these inputs produce, since
  IEEE 754 multiplication by a power of ten that stays within the mantissa is exact), used by both
  call sites. Added `RoundingTest.kt` covering exact `.5` ties at scale 0 and 1 (e.g. `2.5`→`2`,
  `1.25`→`1.2`), since none of the existing golden-value fixtures happened to land on an exact tie.

- [x] **M15.2** — `core:gpx`: replace `java.io.InputStream`/`java.io.StringReader` (`RouteArt.kt`,
  `GpxParser.kt`) with `kotlinx-io-core`'s `Source`/`Buffer` (add the dependency to
  `gradle/libs.versions.toml` + `core:gpx`'s `commonMain.dependencies`), and replace
  `javax.xml.parsers.DocumentBuilderFactory`'s DOM walk with a portable GPX parse. **Open design
  question for this step, not pre-decided:** a real KMP XML library (e.g. `io.github.pdvrieze:xmlutil`)
  vs. a hand-rolled parser for GPX's narrow needed subset (`<trkpt lat lon><ele>`) — decide by
  reading `GpxParser.kt`'s actual DOM usage first; don't default to the heavier dependency without
  checking whether the hand-rolled path is simpler, per CLAUDE.md's "simplicity first".
  **Verify:** `./gradlew :core:gpx:testAndroidHostTest`, `detekt`, `ktlintCheck` pass — parses the
  existing GPX fixture(s) identically to before (byte-for-byte same `WayprintRoute`/`TrackPoint`
  output).
  Note: went hand-rolled — the DOM usage was only ever "first `<trk>`, its `<trkpt lat lon>`
  descendants, each one's first `<ele>` descendant", which `xmlutil` would still require a
  hand-written walk on top of, at the cost of a real dependency and its own API to learn. The
  scanner tracks nesting with a bare `Int` depth counter (no name stack needed — well-formed XML
  guarantees the tag closing at a given depth is the one that opened it there), matches elements
  by local name only (namespace-blind, unlike the old namespace-aware DOM builder — a non-issue
  for real GPX exports, which don't mix namespaces on `trk`/`trkpt`/`ele`), and is XXE-safe by
  construction (comments/CDATA/PIs/DOCTYPEs are skipped verbatim, never interpreted) rather than
  needing the old entity-resolver workaround. `parseTrack`'s signature changed to `Source`, but
  `WayprintRoute.kt` (`feature:wayprint:domain`, still `InputStream` until M15.3) only needed a
  `.asSource().buffered()` adapter at its 2 call sites to keep `./gradlew build` green — its own
  public `InputStream` param is M15.3's job, not this step's.

- [x] **M15.3** — `feature:wayprint:domain`: replace `WayprintLayout.kt`/`WayprintRoute.kt`'s
  `InputStream` param with the `kotlinx-io` type M15.2 settled on, and `String.format(Locale.ROOT,
  "%.1f km", ...)` with a hand-written one-decimal formatter (no `java.util.Locale` on
  Kotlin/Native).
  **Verify:** `./gradlew :feature:wayprint:domain:testAndroidHostTest`, `detekt`, `ktlintCheck`
  pass; the `"%.1f km"` label text is byte-identical for existing test fixtures (negative/zero/
  large distances, not just the common case).
  Note: both functions now take `kotlinx.io.Source` directly (M15.2 already adapted internally
  via `.asSource().buffered()`; that adaptation now lives at the callers instead). New
  `formatOneDecimalKm` in `WayprintLayout.kt` rounds the magnitude half-up-away-from-zero (`diff
  >= 0.5` after `floor`, matching Java's `%f` `RoundingMode.HALF_UP` semantics — verified against
  a real JVM `String.format` run, not assumed) then reattaches the original sign, since Java's
  formatter prints e.g. `"-0.0 km"` for a small negative value that rounds to zero magnitude;
  `DefaultLabelRequestsTest.kt` pins this against 5 cases including that exact edge and a large
  value. `feature:wayprint:ui`'s two `ByteArrayInputStream` call sites (`WayprintViewModel`,
  `RecentsViewModel`) now wrap with `.asSource().buffered()` before calling into domain, needing a
  new `implementation(libs.kotlinx.io.core)` in that module's `build.gradle.kts` (previously only
  `core:gpx`/`feature:wayprint:domain` depended on it directly).

- [x] **M15.4** — `core:storage`: replace `TracksStorage.kt`'s `java.io.File` with `kotlinx-io`'s
  `Path`/`SystemFileSystem`, mirroring `TaigaMobileNova/core/storage`'s own `File`→`kotlinx-io`
  precedent. The constructor keeps taking a caller-resolved directory (CLAUDE.md's settled
  decision stands — `core:storage` still doesn't know about `Context`), just typed as `Path`
  instead of `File`; `WayprintViewModel`/`RecentsViewModel` (the only callers) update their
  `context.filesDir` call site to wrap it as a `Path`.
  **Verify:** `./gradlew :core:storage:testAndroidHostTest :feature:wayprint:ui:testAndroidHostTest`,
  `detekt`, `ktlintCheck` pass; full `./gradlew build` (cross-module, per M9.5's frictions note).
  Note: this step's claimed Taiga precedent doesn't actually exist — `TaigaMobileNova/core/storage`
  has no raw-blob file storage at all (it's Room DB + DataStore; the one `java.io.File` use,
  `platform/AppDataDir.jvm.kt`, is `jvmMain`-only and never became `kotlinx-io`). Went with
  `Path`/`SystemFileSystem` anyway since it's the only portable option and CLAUDE.md's own
  "Trust their code over their docs" note already anticipates checklist claims drifting from a
  reference project's actual code. `Sink`/`Source` have no `readBytes()`/`writeBytes()`/
  `readText()`/`writeText()` convenience like `java.io.File` did, and `SystemFileSystem` has no
  recursive delete, so `TracksStorage.kt` gained small private helpers
  (`readBytes`/`writeBytes`/`readText`/`writeText`/`deleteRecursively`) built on
  `kotlinx.io.readByteArray`/`writeString`/`Sink.write(ByteArray)` + a manual `list()`-then-`delete()`
  recursion. `TracksStorageTest.kt` (in `commonTest`, so it'll need to compile for every future
  target too) swapped `java.nio.file.Files.createTempDirectory` for
  `Path(SystemTemporaryDirectory, "tracks-storage-test-${Random.nextInt()}")` +
  `SystemFileSystem.createDirectories`, and its two "legacy metadata" tests' raw `File` writes for
  a `SystemFileSystem.sink(path).buffered().use { it.writeString(...) }` helper.

- [x] **M15.5** — `feature:wayprint:ui`: in `WayprintCanvas.kt`, swap `renderWayprintStoryBitmap`/
  `renderCombinedWayprintStoryBitmap`'s `android.graphics.Bitmap`+`android.graphics.Canvas` for
  Compose Multiplatform's `ImageBitmap`+`androidx.compose.ui.graphics.Canvas(imageBitmap)`
  (portable, no `expect`/`actual`, identical output), and replace `drawWayprintLabels`'s/
  `hitTestLabelIndex`'s raw `android.graphics.Paint`/`nativeCanvas.drawText` with Compose
  Multiplatform's `TextMeasurer`/`Paragraph` text API, threading a `TextMeasurer` in as a
  parameter since these are plain (non-`@Composable`) `DrawScope` functions. Once done,
  `WayprintCanvas.kt` has zero `android.*` imports — move it back from `androidMain` to
  `commonMain` (M14's move reversed for this one file only).
  **Verify:** `./gradlew :feature:wayprint:ui:testAndroidHostTest`, `detekt`, `ktlintCheck` pass.
  Emulator check (`emulator-testing` skill): rendered route art (line, markers, label halo/fill
  text) looks pixel-equivalent to before the swap, both on-screen and in the exported
  save/share bitmap.
  Note: every `TextMeasurer.measure()` call pins `density = Density(1f)` (a new `LABEL_TEXT_DENSITY`
  constant) so `LABEL_TEXT_SIZE.sp` reads as literal canvas-space units regardless of the device's
  real display density — matching the old `Paint.textSize` behavior, which was already
  density-invariant since raw `Paint` ignores Compose's ambient density entirely. Label
  halo/fill text is drawn via `androidx.compose.ui.text.drawText(textLayoutResult, ...,
  drawStyle = ...)` on one shared `TextLayoutResult` (halo first with `Stroke`, then fill).
  Hit a real bug caught only by the emulator check, not by unit tests or a build: the second
  (fill) `drawText` call must pass `drawStyle = Fill` **explicitly** — leaving the parameter at
  its `null` default does not reset the style to Fill. `AndroidTextPaint.setDrawStyle(drawStyle)`
  early-returns on a `null` argument rather than defaulting it, and the underlying platform
  `TextPaint` is reused across both `drawText` calls on the same `TextLayoutResult` (by design,
  to let color/style change cheaply between draws without relayout) — so the fill draw silently
  inherited the halo draw's leftover `Stroke(width = 8f)`, painting the fill pass as a second
  8px-wide stroke outline directly on top of the first. At the label's actual 28px canvas-space
  size an 8px round-joined outline is thick enough relative to a normal sans-serif letterform to
  fill in every counter and inter-letter gap, so both labels rendered as solid dark blobs with no
  legible glyph shapes — worth flagging explicitly since this is exactly the kind of
  visually-obvious-but-build/test-invisible regression `docs/EMULATOR_TESTING.md`'s existing
  gotchas warn about, and confirmed by diffing an emulator screenshot against a build from the
  pre-M15.5 commit before concluding it was a real regression rather than a pre-existing look
  nobody had zoomed into.

- [x] **M15.6** — `composeApp`: replace the plumbing-only `android.net.Uri` in
  `WayprintAppContent.kt`/`WayprintNavHost.kt`/`WayprintEntryProvider.kt` with an
  `expect class PlatformFileHandle` (Android `actual` wraps `Uri`; no behavior change on Android).
  These 3 files use `Uri` only as a pass-through type — once swapped, they have zero `android.*`
  imports and move back to `commonMain`.
  **Verify:** `./gradlew build`, `detekt`, `ktlintCheck` pass; full share/import flow still works
  identically on the emulator (M5.2's share-intent path, M13's Save/Share).
  Note: `WayprintEntryProvider.kt`'s `wayprintEntry` is the one place that couldn't just swap its
  `Uri` param and move — it directly calls `RecentsScreen`/`WayprintScreen`, both still
  `androidMain`-only until M15.7, so `commonMain` code can never see them (KMP's source-set
  hierarchy has no path from `commonMain` to a sibling platform source set, regardless of how
  many targets are actually configured). Raised this to the user rather than silently descoping;
  they chose to pull a piece of M15.7 forward now rather than leave `WayprintEntryProvider.kt` in
  `androidMain`: `wayprintEntry` itself became `expect fun ... wayprintEntry(...)` in `commonMain`
  with a new `WayprintEntryProvider.android.kt` `actual` (in `composeApp`'s `androidMain`) holding
  the real `RecentsScreen`/`WayprintScreen` wiring and the `PlatformFileHandle` → `Uri` unwrap
  (`pendingImportUri?.uri`). `MainActivity.kt` (`androidApp`, Android-only forever) now wraps its
  `Uri` as `PlatformFileHandle(uri)` before handing it to `WayprintAppContent`. Verified on
  `Medium_Phone_API_36.1`: both the in-app FAB→SAF-picker→template-dialog→edit-screen flow and a
  real external `ACTION_VIEW` handoff (Files app's "Open with → Wayprint" on a `.gpx`, since a
  synthesized `am start -a VIEW -d content://...` throws `SecurityException` without a real
  grant — same class of issue as the `docs/frictions.md` M5.2 entries) landed on the
  template-pick dialog and imported correctly, matching pre-refactor behavior.

- [x] **M15.7** — `feature:wayprint:ui` + `composeApp`: the real platform split for what's left —
  `WayprintViewModel`/`RecentsViewModel`'s `Context`/`MediaStore`/`FileProvider`/share `Intent`/
  `ContentResolver` reads, and `WayprintScreen`/`RecentsScreen`'s pre-Android-Q permission check
  and `rememberLauncherForActivityResult` picker launcher. Design `expect`/`actual` interfaces for:
  reading a picked file's bytes + display name (`PlatformFileHandle` → `ByteArray`/`String`),
  triggering the file picker (`@Composable expect fun rememberGpxPickerLauncher(...)`), and
  exporting an image (`saveToGallery`/`share`, or Desktop's equivalents — **resolve the open
  "what does Share mean on Desktop" question from this milestone's shared context before writing
  the actual**). Android's `actual` is a refactor of existing working code, not new behavior;
  jvm's `actual` uses Swing/AWT (`JFileChooser`/`FileDialog`, a native save dialog). This is the
  biggest, most design-heavy step here — if it doesn't fit one session, split it at this
  boundary (read/pick vs. save/share) rather than doing it half-done.
  **Verify:** `./gradlew build`, `detekt`, `ktlintCheck` pass; Android emulator check confirms
  zero behavior change (M5/M13's existing Verify points all still hold).
  Note: didn't split at the read/pick vs. save/share boundary — both fit in one session.
  `PlatformFileHandle` (M15.6) moved from `composeApp` down into `feature:wayprint:ui`'s
  `platform` package, since `RecentsViewModel.importGpx` needed it as a parameter type and
  `feature:wayprint:ui` can't depend on `composeApp` (dependency runs the other way);
  `WayprintEntryProvider.kt` collapsed back from `expect`/`actual` to a plain `commonMain`
  function now that both screens it wires are portable, undoing M15.6's forced split. New
  `platform/` types: `PlatformFileHandle` (`readBytes()`/`displayName()`),
  `rememberGpxPickerLauncher` (`@Composable expect fun` returning a launch lambda),
  `ImageExporter` (`saveToGallery`/`share`/`supportsShare`), `rememberGatedSaveAction` (wraps a
  save callback with Android's pre-Q permission dance; JVM's `actual` is just `= save`), and
  `PlatformUiModule` (Koin `@Single fun provideImageExporter`, mirroring `core:storage`'s new
  `PlatformStorageModule` for `TracksStorage`, which needed the same Context→Path treatment
  Context itself was getting here). **Desktop's "what does Share mean" question, resolved**:
  `ImageExporter.supportsShare` is `false` on JVM (no share-sheet equivalent exists), gating the
  Share icon out of `WayprintScreen` entirely rather than faking one; JVM's `saveToGallery`
  remaps to a native AWT `FileDialog` "Save As" (the only real desktop equivalent for both former
  actions) and `share()` is unreachable dead code (`error(...)`) protected by that same UI gate.
  **Scope grew beyond the checklist's literal Context/MediaStore/FileProvider/Intent list**:
  moving `WayprintViewModel`/`RecentsViewModel`/`WayprintScreen`/`RecentsScreen` into
  `commonMain` also exposed `System.currentTimeMillis()`, `java.text.SimpleDateFormat`,
  `java.util.Locale`, and `java.io.ByteArrayInputStream` in code that used to compile only
  because the *sole* KMP target was Android (`commonMain` never gets JVM-stdlib access in
  Kotlin Multiplatform, matching M15.1-M15.4's precedent — a single-target project doesn't
  relax this, confirmed the hard way by a real `compileAndroidMain` failure mid-step). Fixed
  with `kotlin.time.Clock.System.now().toEpochMilliseconds()` (id/timestamp generation, per
  `TaigaMobileNova`'s own precedent), a hand-rolled `formatImportedDate`/`formatDistanceKm` in
  `RecentsViewModel.kt` (kotlinx-datetime's non-deprecated `LocalDate.month`/`.day`, not
  `monthNumber`/`dayOfMonth`), and `kotlinx.io.Buffer().apply { write(gpxBytes) }` in place of
  `ByteArrayInputStream(...).asSource().buffered()` (a plain `Buffer` already *is* a `Source`,
  simpler than the old adapter chain, not just a portability workaround). `TracksStorage` also
  needed a real Koin binding for the first time (previously constructed by hand from
  `context.filesDir` inside each ViewModel) — `core:storage` gained the `wayprint.kmp.di`
  plugin and a `PlatformStorageModule` (`@Single fun providePath`/`provideTracksStorage`,
  JVM's using a ported `TaigaMobileNova/core/storage/platform/AppDataDir.jvm.kt`), explicitly
  `includes`d into `composeApp`'s `AppModule` (a different Gradle module than its
  `@ComponentScan` prefix covers, same reason `WayprintUiModule` needed it).
  **A genuine Koin `verify()` gotcha, worth remembering for any future factory-provided
  concrete-class `@Single`**: `KoinGraphTest` failed with "Missing definition for
  '[field:'directory' - type:'kotlinx.io.files.Path']'" against a `@Single fun
  provideTracksStorage(context: Context): TracksStorage = TracksStorage(Path(...))` factory —
  `verify()` doesn't trace the factory *function's* parameters at all, it reflects on the
  *returned type's own* real constructor (`TracksStorage(directory: Path)`) and demands that
  be independently resolvable, regardless of how the factory actually builds the instance.
  `ImageExporter`'s equivalent factory happened to pass verification only by coincidence — its
  factory parameter and its class's real constructor parameter are both bare `Context`. Fixed
  by splitting into two bindings (`providePath(context): Path` then
  `provideTracksStorage(directory: Path): TracksStorage`) so the concrete class's real
  constructor dependency has a real definition to find — confirmed by matching Taiga's own
  `PlatformStorageModule` shape, where every factory-provided singleton binds to an *interface*
  type instead (interfaces have no constructor for `verify()` to reflect on, sidestepping this
  entirely) — a pattern to prefer over a bare-`Single`-fun-returning-a-concrete-class next time
  one of these needs adding, rather than rediscovering this the hard way again.
  Verified on `Medium_Phone_API_36.1` (pushed a temporary test GPX, fresh `pm clear`): (1) FAB →
  `rememberGpxPickerLauncher` → SAF picker → template-pick dialog → edit screen, labels crisp
  with no M15.5-style regression; (2) Save writes a real `MediaStore` row (confirmed in logcat)
  and shows "Saved to gallery" with zero permission prompt on API 36 (`rememberGatedSaveAction`
  correctly no-ops the pre-Q check); (3) Share opens the real Android share sheet via the
  `FileProvider` URI (the chooser's own preview-loader `SecurityException`/`ImageLoader` warning
  in logcat is the known-benign cosmetic one `docs/EMULATOR_TESTING.md` already documents, not a
  real grant failure); (4) a real external `ACTION_VIEW` handoff (Files app → "Open with
  Wayprint" → **Just once**) still lands on Recents' template-pick dialog and imports correctly.
  `feature/wayprint/ui/src/jvmMain`'s new `platform/` actuals (picker, exporter, save-gate,
  file-handle) and `core/storage/src/jvmMain`'s `PlatformStorageModule`/`AppDataDir.jvm.kt`
  are **not yet compiled or run anywhere** — `jvm()` isn't a registered target until M15.8, per
  this milestone's own sequencing rule, so they're unverified beyond being written until then
  (same caveat M16's iOS steps already carry for their own targets).

- [ ] **M15.8** — Build-logic: add `jvm()` to `configureKmp()` (`KmpConfiguration.kt`), mirroring
  `TaigaMobileNova/build-logic/.../KmpConfiguration.kt`'s `jvm()` line and its doc comment's own
  "goes here, and nowhere else, when those apps arrive." Wire a real runnable Desktop entry point:
  `composeApp/src/jvmMain/.../WayprintDesktop.kt` with `fun main()`, and a trimmed
  `compose.desktop { application { ... } }` block in `composeApp/build.gradle.kts` (mirroring
  `TaigaMobileNova/composeApp/build.gradle.kts`'s shape — skip what Taiga has that Wayprint
  doesn't need: no F-Droid/Play flavor split, no `buildkonfig`). Add the `jetbrains.compose.desktop`
  dependency to `gradle/libs.versions.toml`.
  **Verify:** `./gradlew build` succeeds for the `jvm` target across every module (the whole
  point of M15.1–M15.7 landing first); `detekt`, `ktlintCheck` pass. `./gradlew :composeApp:run`
  launches a real window on this machine — import a GPX, render route art, Save/Share (however
  M15.7 resolved Desktop's "Share") all work end-to-end. This is the one target in this milestone
  fully verifiable start-to-finish on this dev machine.

## M16 — Add an iOS KMP target

Scoped 2026-09-04 alongside M15, but **don't start this until M15 is fully landed and archived**.
Reusing M15's `expect`/`actual` interfaces (proven by two real implementations, Android and JVM)
for a third platform is a much smaller, much less speculative step than designing those
interfaces for three platforms in one shot — this is the whole reason M15/M16 are two milestones
and not one.

**The hard limit that shapes every step here: this dev machine is Linux.** Kotlin/Native can
compile `iosArm64`/`iosSimulatorArm64` klibs on Linux with no Mac — that much is verifiable in
this environment. Producing or running an actual `.app`, an Xcode build, or anything on a
simulator/device requires a Mac with Xcode, which isn't available here. Every step's Verify line
below is capped accordingly, and says so explicitly — **"compiles" is not "works."** Don't let a
future session (or the user) mistake a green M16 checkbox for "the iOS app has been run."

Reference precedent: `../TaigaMobileNova/iosApp` — a real Xcode project (`iosApp.xcodeproj`,
`iOSApp.swift`, `ContentView.swift`, `Info.plist`) consuming `composeApp`'s iOS framework export.

- [ ] **M16.1** — Build-logic: add `iosArm64()`/`iosSimulatorArm64()` to `configureKmp()`
  (`KmpConfiguration.kt`), mirroring `TaigaMobileNova`'s same two lines. Add each M15 `expect`
  interface's iOS `actual` (`PlatformFileHandle` → `NSURL`, file read via `NSFileManager`/`NSData`,
  the picker launcher via a wrapped `UIDocumentPickerViewController`, image export via
  `UIActivityViewController`/`PHPhotoLibrary`).
  **Verify (capped — no Mac in this environment):** `./gradlew :core:gpx:compileIosArm64MainKotlinMetadata`
  (and the equivalent for every other module up through `composeApp`) succeeds on this machine.
  That is the full extent of what's verifiable here — the iOS actuals are **unverified beyond
  compiling** until run on a real Mac + simulator/device.

- [ ] **M16.2** — Add the `iosApp` Xcode project scaffold at the repo root, mirroring
  `TaigaMobileNova/iosApp`'s structure, plus `composeApp`'s iOS framework export config
  (`binaries.framework { ... }` in `composeApp/build.gradle.kts`, per Taiga's own).
  **Verify (capped — no Mac in this environment):** `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
  succeeds on this machine. Actually opening `iosApp.xcodeproj`, building, and running on a
  simulator/device is **out of reach here** — flag this step done-but-unverified-end-to-end in
  its `Note:`, and ask the user to confirm on their own Mac before treating M16 as trustworthy.

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
