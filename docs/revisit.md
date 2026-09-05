# Revisit

Real problems noticed outside the current task, logged here instead of fixed inline so the diff
stays reviewable. Not chat — this is the persistent record.

- M15.7: `WayprintScreen`'s save snackbar text is the literal string `"Saved to gallery"`
  (`WayprintScreen.kt`, `viewModel.saveConfirmations.collect { snackbarHostState.showSnackbar(...) }`),
  which is accurate for Android (`ImageExporter.saveToGallery`'s real `MediaStore` write) but will
  read wrong once M15.8 makes it reachable on Desktop, where `ImageExporter.saveToGallery`
  actually opens a native "Save As" file dialog — there's no "gallery" there. Left as a literal
  string rather than threading a platform-conditional message through for a UI that doesn't exist
  yet; M15.8 (first milestone that actually runs this on Desktop) is the right place to give it
  real wording, once there's a real window to look at while choosing it. M16.1 adds a third
  platform (iOS) where "gallery" is doubly wrong (Photos, not a gallery) — same fix, same
  deferral point.

- M16.1: `configureComposeStabilityMarker()`'s `compileOnly(compose.runtime)`
  (`build-logic/convention/src/main/kotlin/com/grappim/wayprint/buildlogic/ComposeStabilityMarker.kt:22`)
  now prints a Gradle warning ("Unsupported `compileOnly` Dependencies in Kotlin Targets ... used
  in targets: Kotlin/Native") for every module on every build, once M16.1 added `iosArm64()`/
  `iosSimulatorArm64()` to `configureKmp()` — `compileOnly` isn't supported for Kotlin/Native
  consumers the way it is for JVM/Android. Harmless (`compose.runtime` really is present
  transitively via the Compose Multiplatform plugin on iOS too, so nothing is actually missing at
  compile time — confirmed by every iOS target actually compiling clean), but noisy on every
  build from here on. Fixing it properly means exposing the dependency as `api` instead of
  `compileOnly` on Native — a call about `ComposeStabilityMarker.kt`'s own design (probably fine,
  but not this step's job) rather than something to change as a side effect of M16.1.

- 2026-09-05 (found while investigating the Recents delete-button bug, unrelated): a plain
  `./gradlew build` fails at `:core:gpx:compileTestKotlinIosArm64`/`compileTestKotlinIosSimulatorArm64`
  — `core/gpx/src/commonTest/kotlin/.../GpxParserTest.kt`, `RdpTest.kt`, `RouteArtTest.kt` all use
  JVM-only APIs (`javaClass`, `File(...).use { }`, a Java-`File`-typed helper's `.size`/`.lat`/
  `.lon`/`.ele`) that don't exist on Kotlin/Native, and `RouteArtTest.kt:11` has a test function
  name containing `()`, illegal on Native. Confirmed pre-existing on `master` before any change
  this session (verified via `git stash`) — M15/M16's own Verify lines only ever ran
  `compileKotlinIosArm64`/`compileKotlinIosSimulatorArm64` (main source sets), never the
  `commonTest` compile for iOS, so this gap was never actually exercised until now. Whoever picks
  this up next: the fix is almost certainly the same shape M15.1/M15.2 already used for
  `core:gpx`'s main sources (hand-rolled non-JVM equivalents), just extended to these three test
  files.

- 2026-09-05 (same discovery, same day): `:core:storage:compileTestKotlinIosArm64`/
  `compileTestKotlinIosSimulatorArm64` also fail on `master` before any change this session
  (verified via `git stash`) — `core/storage/src/commonTest/kotlin/.../TracksStorageTest.kt` uses
  `String.toByteArray()` (`kotlin.text`, JVM/Android-only, no Kotlin/Native overload) throughout.
  Same root cause and same fix shape as the `core:gpx` entry directly above — every `commonTest`
  file across every KMP module apparently was never actually compile-checked against the iOS test
  source sets, only `compileKotlinIosXxx` (main) per M16's own Verify lines. Worth checking whether
  `feature:wayprint:domain`/`feature:wayprint:ui`/`composeApp`'s `commonTest` files have the same
  gap before assuming it's confined to these two.
