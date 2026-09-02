# Revisit

Real problems noticed outside the current task, logged here instead of fixed inline so the diff
stays reviewable. Not chat — this is the persistent record.

- M7.2 drag hit-testing (`WayprintCanvas.kt`'s `detectDragGestures`) uses `PlacedLabel.boundingBox`
  as-is, per the checklist's own spec — but that box is sized from `LabelPlacement.kt`'s
  `CHAR_WIDTH`/`TEXT_HEIGHT` placement-estimate constants (7×14 canvas units), not from the label's
  actual rendered size (`WayprintCanvas.kt`'s `LABEL_TEXT_SIZE = 28f`). Confirmed on-device
  (`Medium_Phone_API_36.1`, scale≈1.0): "Start"'s real hit box is only ~35×14 device px against a
  visually much larger 28sp glyph — a real user's finger will frequently miss the label they're
  trying to grab. Worth a small fixed touch-padding around the box for hit-testing (not for
  collision placement, which should stay as-is) before M7 ships.
