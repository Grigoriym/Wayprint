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
