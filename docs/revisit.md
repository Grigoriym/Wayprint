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
