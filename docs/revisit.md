# Revisit

Real problems noticed outside the current task, logged here instead of fixed inline so the diff
stays reviewable. Not chat — this is the persistent record.

- `androidApp/src/main/kotlin/com/grappim/wayprint/MainActivity.kt:11,15,34` — share/view-intent
  handling (`handleIntent` → `viewModel.loadFromUri(uri)`) still targets the pre-M9
  `WayprintViewModel` API directly: after M9.3 that class moved to
  `com.grappim.wayprint.feature.wayprint.ui.edit.WayprintViewModel`, dropped `loadFromUri`
  (import now lives on `RecentsViewModel.importGpx`, M9.4), and needs a `trackId` via
  `parametersOf` that a bare `by viewModel()` injection can't supply. `MainActivity` will not
  compile once M9.3 lands. Neither M9.4 nor M9.5 (docs/CHECKLIST.md) names fixing this
  explicitly — M9.5 "wire it together" covers `composeApp`'s `WayprintScreen()` call site but not
  `androidApp`'s share-intent entry point. Whoever does M9.5 (or a follow-up) should route a
  share/view intent's `Uri` into the Recents screen's import flow (e.g. via a pending-uri passed
  through `WayprintAppContent`/navigation, or `RecentsViewModel` picking it up on start)
  instead of calling a now-nonexistent `WayprintViewModel.loadFromUri`.
