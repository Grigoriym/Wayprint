package com.grappim.wayprint.composeapp

// M4.3 provisional demo route — no GPX import UI exists yet (M5's scope). A short, hand-written
// GPX string stands in for real file-picker/share-intent input until M5 replaces this call site.
internal const val DEMO_GPX = """<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="wayprint-demo" xmlns="http://www.topografix.com/GPX/1/1">
  <trk>
    <name>Demo route</name>
    <trkseg>
      <trkpt lat="51.050400" lon="13.737300"><ele>115</ele></trkpt>
      <trkpt lat="51.052100" lon="13.739800"><ele>118</ele></trkpt>
      <trkpt lat="51.053800" lon="13.738200"><ele>121</ele></trkpt>
      <trkpt lat="51.055600" lon="13.741100"><ele>119</ele></trkpt>
      <trkpt lat="51.057200" lon="13.743900"><ele>123</ele></trkpt>
      <trkpt lat="51.058900" lon="13.742300"><ele>126</ele></trkpt>
    </trkseg>
  </trk>
</gpx>
"""
