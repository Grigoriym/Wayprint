package com.grappim.wayprint.feature.wayprint.domain

import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.writeString

/**
 * The `04 Riesa - Meissen.gpx` fixture used by [StoryPresetTest], [WayprintLayoutTest], and
 * [WayprintRouteTest], inlined as a string constant rather than loaded via classpath resource
 * lookup (`javaClass.getResourceAsStream`), which has no Kotlin/Native equivalent (see
 * docs/revisit.md).
 */
private const val RIESA_MEISSEN_GPX = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
<gpx version="1.1" creator="OsmAndRouterV2" xmlns="http://www.topografix.com/GPX/1/1" xmlns:osmand="https://osmand.net/docs/technical/osmand-file-formats/osmand-gpx" xmlns:gpxtpx="https://www8.garmin.com/xmlschemas/TrackPointExtensionv1.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 https://www.topografix.com/GPX/1/1/gpx.xsd">
  <metadata>
    <name>04 Riesa - Meissen</name>
    <link href="guibo.travel">
      <text>Route von guibo.travel</text>
    </link>
    <time>2026-08-09T11:31:17Z</time>
    <bounds minlat="51.161584" minlon="13.307704" maxlat="51.308247" maxlon="13.47739" />
  </metadata>
  <trk>
    <name>7 - elberadweg_meissen_riesa</name>
    <trkseg>
      <trkpt lat="51.305712" lon="13.307704">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.305504" lon="13.308477">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.3055" lon="13.30848">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.305499" lon="13.308479">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.305348" lon="13.309019">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.305348" lon="13.309019">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.305346" lon="13.309028">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.30558" lon="13.309217">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.306063" lon="13.309532">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.306063" lon="13.309529">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.306063" lon="13.309529">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.306032" lon="13.310046">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.305999" lon="13.310145">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.305994" lon="13.310278">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.30601" lon="13.31028">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.30601" lon="13.31028">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.306034" lon="13.310483">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.306128" lon="13.310929">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.306158" lon="13.312538">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.306165" lon="13.313236">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.306185" lon="13.314024">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.306215" lon="13.315784">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.306225" lon="13.317023">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.306242" lon="13.317527">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.306265" lon="13.318004">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.306279" lon="13.318208">
        <ele>93</ele>
      </trkpt>
      <trkpt lat="51.306295" lon="13.319726">
        <ele>92</ele>
      </trkpt>
      <trkpt lat="51.306272" lon="13.321255">
        <ele>92</ele>
      </trkpt>
      <trkpt lat="51.306359" lon="13.32228">
        <ele>91</ele>
      </trkpt>
      <trkpt lat="51.306389" lon="13.323144">
        <ele>91</ele>
      </trkpt>
      <trkpt lat="51.306352" lon="13.323594">
        <ele>92</ele>
      </trkpt>
      <trkpt lat="51.306419" lon="13.32434">
        <ele>93</ele>
      </trkpt>
      <trkpt lat="51.306513" lon="13.324844">
        <ele>93</ele>
      </trkpt>
      <trkpt lat="51.306553" lon="13.325547">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.30655" lon="13.326276">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.306634" lon="13.327762">
        <ele>93</ele>
      </trkpt>
      <trkpt lat="51.306761" lon="13.328664">
        <ele>92</ele>
      </trkpt>
      <trkpt lat="51.306839" lon="13.329168">
        <ele>91</ele>
      </trkpt>
      <trkpt lat="51.306902" lon="13.329801">
        <ele>93</ele>
      </trkpt>
      <trkpt lat="51.307" lon="13.330461">
        <ele>93</ele>
      </trkpt>
      <trkpt lat="51.307124" lon="13.331142">
        <ele>93</ele>
      </trkpt>
      <trkpt lat="51.307332" lon="13.332166">
        <ele>93</ele>
      </trkpt>
      <trkpt lat="51.307291" lon="13.332381">
        <ele>93</ele>
      </trkpt>
      <trkpt lat="51.307224" lon="13.332607">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.307244" lon="13.333277">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.307402" lon="13.334462">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.307459" lon="13.335036">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.30766" lon="13.337429">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.307908" lon="13.340219">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.30822" lon="13.343856">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.308213" lon="13.344542">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.308234" lon="13.344698">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.308247" lon="13.345079">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.308156" lon="13.345733">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.307959" lon="13.346489">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.307781" lon="13.346876">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.307653" lon="13.346983">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.307214" lon="13.346811">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.307016" lon="13.346699">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.306627" lon="13.346441">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.306269" lon="13.347021">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.306218" lon="13.347155">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.306124" lon="13.34724">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.305933" lon="13.347305">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.305866" lon="13.347359">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.305809" lon="13.347477">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.305789" lon="13.347654">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.305789" lon="13.3479">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.305769" lon="13.34804">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.305742" lon="13.348126">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.305262" lon="13.349343">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.305021" lon="13.349853">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.303931" lon="13.352133">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.303495" lon="13.353222">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.303153" lon="13.354418">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.303042" lon="13.354917">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.302851" lon="13.356328">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.302777" lon="13.356752">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.302603" lon="13.357309">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.302405" lon="13.357889">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.302019" lon="13.358913">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.30168" lon="13.359756">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.301466" lon="13.360356">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.301355" lon="13.360625">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.301318" lon="13.360678">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.301268" lon="13.360678">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.300496" lon="13.35996">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.299658" lon="13.359187">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.298886" lon="13.361344">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.298148" lon="13.363425">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.297162" lon="13.366054">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.297119" lon="13.366311">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.296948" lon="13.367062">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.296156" lon="13.36976">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.295398" lon="13.371488">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.29455" lon="13.373397">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.29402" lon="13.37484">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.293594" lon="13.376031">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.293782" lon="13.376273">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.29514" lon="13.378027">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.295485" lon="13.37837">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.295629" lon="13.378526">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.295089" lon="13.380886">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.295029" lon="13.381079">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.294928" lon="13.38117">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.294556" lon="13.381428">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.29352" lon="13.382136">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.293366" lon="13.382216">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.293265" lon="13.382211">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.293151" lon="13.382174">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.293046" lon="13.382168">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.292413" lon="13.382544">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.292127" lon="13.38286">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.291617" lon="13.383391">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.290396" lon="13.384646">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.289913" lon="13.38514">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.28944" lon="13.385628">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.288903" lon="13.386202">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.28793" lon="13.387731">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.286578" lon="13.38985">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.286203" lon="13.390751">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.28587" lon="13.391695">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.285766" lon="13.391942">
        <ele>94</ele>
      </trkpt>
      <trkpt lat="51.284877" lon="13.389914">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.284213" lon="13.388412">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.284009" lon="13.38793">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.283911" lon="13.387774">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.283807" lon="13.387688">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.283233" lon="13.38727">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.282887" lon="13.386964">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.282673" lon="13.38676">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.282502" lon="13.386621">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.282052" lon="13.386293">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.282025" lon="13.38624">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.282005" lon="13.386154">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.281888" lon="13.386245">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.281549" lon="13.386363">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.281455" lon="13.386422">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.281207" lon="13.386642">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.280844" lon="13.386921">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.280489" lon="13.3872">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.280237" lon="13.387425">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.279683" lon="13.387999">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.279294" lon="13.388412">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.278976" lon="13.388713">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.278673" lon="13.388949">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.277247" lon="13.390059">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.27669" lon="13.390494">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.276045" lon="13.391008">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.275945" lon="13.391062">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.275952" lon="13.391148">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.275928" lon="13.391266">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.27576" lon="13.39183">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.275583" lon="13.392425">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.275351" lon="13.393101">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.275096" lon="13.393712">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.274891" lon="13.394163">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.274724" lon="13.39442">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.274529" lon="13.394694">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.274247" lon="13.395043">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.274129" lon="13.395215">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.274029" lon="13.395413">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.273928" lon="13.395703">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.273824" lon="13.395987">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.273737" lon="13.396159">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.273657" lon="13.396234">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.27332" lon="13.396464">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.272153" lon="13.397307">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.272109" lon="13.397371">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.271398" lon="13.398068">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.271237" lon="13.398256">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.271146" lon="13.398401">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.270962" lon="13.398835">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.270881" lon="13.398905">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.270955" lon="13.40037">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.270975" lon="13.400793">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.270908" lon="13.400815">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.270874" lon="13.400868">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.270814" lon="13.400906">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.270652" lon="13.400906">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.270001" lon="13.400815">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.269471" lon="13.400606">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.268934" lon="13.40052">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.268786" lon="13.400514">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.268394" lon="13.400531">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.267988" lon="13.400579">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.267722" lon="13.40059">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.267561" lon="13.40059">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.267166" lon="13.400413">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.26681" lon="13.40023">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.266561" lon="13.400203">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.266212" lon="13.400262">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.265937" lon="13.400332">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.265538" lon="13.400461">
        <ele>95</ele>
      </trkpt>
      <trkpt lat="51.265333" lon="13.398921">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.265027" lon="13.398583">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.264675" lon="13.398122">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.264238" lon="13.397645">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.263789" lon="13.397489">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.263225" lon="13.39736">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.263094" lon="13.39728">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.262923" lon="13.397441">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.262762" lon="13.397902">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.262698" lon="13.39817">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.262664" lon="13.398401">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.262661" lon="13.398535">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.262637" lon="13.398669">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.262557" lon="13.398798">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.262225" lon="13.39898">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.261915" lon="13.399061">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.261741" lon="13.39913">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.2616" lon="13.399216">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.261422" lon="13.399361">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.261003" lon="13.399678">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.260566" lon="13.399914">
        <ele>111</ele>
      </trkpt>
      <trkpt lat="51.260291" lon="13.39993">
        <ele>113</ele>
      </trkpt>
      <trkpt lat="51.260153" lon="13.399978">
        <ele>114</ele>
      </trkpt>
      <trkpt lat="51.259274" lon="13.400321">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.258988" lon="13.400294">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.258082" lon="13.399989">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.257605" lon="13.399908">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.257316" lon="13.399887">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.257122" lon="13.399973">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.256467" lon="13.400101">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.255231" lon="13.400364">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.253932" lon="13.400713">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.253335" lon="13.400788">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.252905" lon="13.400734">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.252065" lon="13.400734">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.25172" lon="13.400622">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.251233" lon="13.400364">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.251125" lon="13.400262">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.251052" lon="13.400118">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.250837" lon="13.400257">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.25047" lon="13.400284">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.249262" lon="13.400439">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.24907" lon="13.400514">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.248473" lon="13.400675">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.247909" lon="13.400949">
        <ele>111</ele>
      </trkpt>
      <trkpt lat="51.24756" lon="13.401142">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.247123" lon="13.401308">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.246881" lon="13.401453">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.245599" lon="13.402639">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.245236" lon="13.402923">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.24445" lon="13.403422">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.244373" lon="13.403508">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.244242" lon="13.403615">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.244111" lon="13.40368">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.243409" lon="13.404168">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.243023" lon="13.404645">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.242533" lon="13.405267">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.242328" lon="13.405605">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.242247" lon="13.405696">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.24213" lon="13.405798">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.241999" lon="13.405863">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.241861" lon="13.405965">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.241761" lon="13.406067">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.241713" lon="13.406147">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.241465" lon="13.406518">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.241172" lon="13.40678">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.240937" lon="13.407016">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.240682" lon="13.407381">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.240261" lon="13.408085">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.239779" lon="13.408888">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.239973" lon="13.409296">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.239755" lon="13.409731">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.23959" lon="13.410026">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.239325" lon="13.410444">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.239107" lon="13.410744">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.238932" lon="13.410964">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.238758" lon="13.411147">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.238344" lon="13.411442">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.237723" lon="13.411908">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.237418" lon="13.412107">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.237186" lon="13.412241">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.237095" lon="13.412332">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.236843" lon="13.412708">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.236739" lon="13.412826">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.236645" lon="13.412853">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.236007" lon="13.41274">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.235688" lon="13.412574">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.235547" lon="13.412606">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.234885" lon="13.413024">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.234089" lon="13.413502">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.233746" lon="13.413625">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.233545" lon="13.413679">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.233397" lon="13.413673">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.233196" lon="13.41363">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.232984" lon="13.413534">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.232345" lon="13.413019">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.231818" lon="13.412686">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.23161" lon="13.41186">
        <ele>111</ele>
      </trkpt>
      <trkpt lat="51.231566" lon="13.411828">
        <ele>111</ele>
      </trkpt>
      <trkpt lat="51.231519" lon="13.411774">
        <ele>111</ele>
      </trkpt>
      <trkpt lat="51.231378" lon="13.411501">
        <ele>111</ele>
      </trkpt>
      <trkpt lat="51.231304" lon="13.411329">
        <ele>112</ele>
      </trkpt>
      <trkpt lat="51.231049" lon="13.410707">
        <ele>112</ele>
      </trkpt>
      <trkpt lat="51.230821" lon="13.410256">
        <ele>113</ele>
      </trkpt>
      <trkpt lat="51.230596" lon="13.409875">
        <ele>115</ele>
      </trkpt>
      <trkpt lat="51.230337" lon="13.409521">
        <ele>117</ele>
      </trkpt>
      <trkpt lat="51.230025" lon="13.409124">
        <ele>120</ele>
      </trkpt>
      <trkpt lat="51.229645" lon="13.408733">
        <ele>122</ele>
      </trkpt>
      <trkpt lat="51.229175" lon="13.408062">
        <ele>124</ele>
      </trkpt>
      <trkpt lat="51.229054" lon="13.407864">
        <ele>124</ele>
      </trkpt>
      <trkpt lat="51.228839" lon="13.407515">
        <ele>124</ele>
      </trkpt>
      <trkpt lat="51.228654" lon="13.407274">
        <ele>123</ele>
      </trkpt>
      <trkpt lat="51.228305" lon="13.406855">
        <ele>120</ele>
      </trkpt>
      <trkpt lat="51.228204" lon="13.406678">
        <ele>119</ele>
      </trkpt>
      <trkpt lat="51.22773" lon="13.405552">
        <ele>111</ele>
      </trkpt>
      <trkpt lat="51.227112" lon="13.404162">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.226964" lon="13.404334">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.226837" lon="13.404532">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.226628" lon="13.404715">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.226541" lon="13.404908">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.226484" lon="13.405187">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.226407" lon="13.405326">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.226245" lon="13.405466">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.226118" lon="13.405605">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.225977" lon="13.405756">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.225849" lon="13.405884">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.225691" lon="13.405906">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.225604" lon="13.405884">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.225507" lon="13.405815">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.225453" lon="13.405756">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.225352" lon="13.405503">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.225218" lon="13.405294">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.22516" lon="13.405273">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.225106" lon="13.405214">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.22473" lon="13.404527">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.224414" lon="13.404012">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.224058" lon="13.403513">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.223424" lon="13.402768">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.223262" lon="13.402617">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.222301" lon="13.401716">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.221979" lon="13.401469">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.221656" lon="13.401249">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.221434" lon="13.40111">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.220834" lon="13.400726">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.220561" lon="13.400557">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.2204" lon="13.400498">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.220231" lon="13.400498">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.22001" lon="13.400557">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.219778" lon="13.400681">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.219711" lon="13.400702">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.219641" lon="13.400686">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.219594" lon="13.400691">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.21953" lon="13.400756">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.2195" lon="13.40082">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.219479" lon="13.401067">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.219448" lon="13.401142">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.219385" lon="13.401228">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.218897" lon="13.401722">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.21874" lon="13.401861">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.218716" lon="13.401968">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.218689" lon="13.403132">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.218676" lon="13.403245">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.218625" lon="13.403288">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.217728" lon="13.403363">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.217298" lon="13.403347">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.216868" lon="13.4034">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.216146" lon="13.403374">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.215323" lon="13.403449">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.214926" lon="13.403492">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.213733" lon="13.403669">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.21298" lon="13.403588">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.212524" lon="13.40354">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.21219" lon="13.403454">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.211546" lon="13.403459">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.211357" lon="13.403476">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.211256" lon="13.403454">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.211048" lon="13.403577">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.210968" lon="13.403669">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.210904" lon="13.403706">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.210587" lon="13.403776">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.210067" lon="13.403878">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.209614" lon="13.404015">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.209351" lon="13.404066">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.209011" lon="13.404071">
        <ele>96</ele>
      </trkpt>
      <trkpt lat="51.208773" lon="13.404087">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.208544" lon="13.404017">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.208514" lon="13.403889">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.20848" lon="13.403658">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.208054" lon="13.403797">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.207661" lon="13.404007">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.207294" lon="13.404114">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.206582" lon="13.404077">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.206259" lon="13.404087">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.205735" lon="13.404184">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.20552" lon="13.4042">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.205338" lon="13.404184">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.204484" lon="13.403937">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.203792" lon="13.403964">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.203025" lon="13.404141">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.20279" lon="13.404248">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.202538" lon="13.404473">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.202354" lon="13.404554">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.202216" lon="13.404565">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.20152" lon="13.404436">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.200955" lon="13.404484">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.200884" lon="13.40472">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.200737" lon="13.404919">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.200545" lon="13.405037">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.200031" lon="13.405149">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.200034" lon="13.405552">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.200007" lon="13.405766">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.1999" lon="13.406088">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.199819" lon="13.406126">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.199759" lon="13.406201">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.199688" lon="13.406185">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.199369" lon="13.406185">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.199312" lon="13.406292">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.199257" lon="13.406496">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.19911" lon="13.406689">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.198935" lon="13.406759">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.198844" lon="13.406721">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.198666" lon="13.406501">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.198495" lon="13.406324">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.198424" lon="13.406442">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.19835" lon="13.406507">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.197897" lon="13.406501">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.197837" lon="13.406565">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.197676" lon="13.406935">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.197595" lon="13.407086">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.197561" lon="13.407129">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.197328" lon="13.407113">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.197379" lon="13.407553">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.197433" lon="13.407837">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.197493" lon="13.408186">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.197533" lon="13.40855">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.19754" lon="13.408872">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.19753" lon="13.409103">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.1975" lon="13.409323">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.197459" lon="13.409543">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.197405" lon="13.409747">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.197315" lon="13.410004">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.197042" lon="13.410643">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.196912" lon="13.4109">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.196598" lon="13.411565">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.196565" lon="13.411635">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.196174" lon="13.412459">
        <ele>107</ele>
      </trkpt>
      <trkpt lat="51.195842" lon="13.413233">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.195718" lon="13.413475">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.195543" lon="13.413641">
        <ele>111</ele>
      </trkpt>
      <trkpt lat="51.19551" lon="13.413743">
        <ele>111</ele>
      </trkpt>
      <trkpt lat="51.195523" lon="13.414247">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.19553" lon="13.414365">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.195577" lon="13.414548">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.195577" lon="13.414623">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.195516" lon="13.414736">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.195224" lon="13.415138">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.195005" lon="13.415454">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.194589" lon="13.416028">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.194239" lon="13.416543">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.194087" lon="13.416699">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.193573" lon="13.417219">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.193432" lon="13.417471">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.192766" lon="13.418228">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.192205" lon="13.419016">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.191997" lon="13.419354">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.191923" lon="13.419499">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.191757" lon="13.419816">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.191452" lon="13.420282">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.191361" lon="13.420433">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.191092" lon="13.421028">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.191035" lon="13.421146">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.190927" lon="13.421269">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.19085" lon="13.421436">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.190732" lon="13.421768">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.190564" lon="13.422358">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.19039" lon="13.422916">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.190295" lon="13.423222">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.190228" lon="13.423324">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.190094" lon="13.423506">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.190043" lon="13.423608">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.189902" lon="13.423968">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.189458" lon="13.425346">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.189304" lon="13.425942">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.189216" lon="13.42636">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.189015" lon="13.427546">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.188873" lon="13.428098">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.188426" lon="13.42982">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.188251" lon="13.430781">
        <ele>112</ele>
      </trkpt>
      <trkpt lat="51.188093" lon="13.43188">
        <ele>113</ele>
      </trkpt>
      <trkpt lat="51.188039" lon="13.432631">
        <ele>112</ele>
      </trkpt>
      <trkpt lat="51.187992" lon="13.433554">
        <ele>109</ele>
      </trkpt>
      <trkpt lat="51.187972" lon="13.433752">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.187871" lon="13.434214">
        <ele>108</ele>
      </trkpt>
      <trkpt lat="51.187579" lon="13.435346">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.187451" lon="13.435753">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.187104" lon="13.436612">
        <ele>113</ele>
      </trkpt>
      <trkpt lat="51.186466" lon="13.438194">
        <ele>113</ele>
      </trkpt>
      <trkpt lat="51.186345" lon="13.438484">
        <ele>113</ele>
      </trkpt>
      <trkpt lat="51.186042" lon="13.439128">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.185649" lon="13.439938">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.185521" lon="13.440243">
        <ele>113</ele>
      </trkpt>
      <trkpt lat="51.185451" lon="13.440458">
        <ele>114</ele>
      </trkpt>
      <trkpt lat="51.185269" lon="13.441037">
        <ele>115</ele>
      </trkpt>
      <trkpt lat="51.185128" lon="13.44159">
        <ele>110</ele>
      </trkpt>
      <trkpt lat="51.184886" lon="13.442593">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.184866" lon="13.442689">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.184879" lon="13.442748">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.185158" lon="13.443022">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.185215" lon="13.443092">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.185222" lon="13.443172">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.185215" lon="13.44329">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.185225" lon="13.443365">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.185255" lon="13.443435">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.185455" lon="13.443652">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.185655" lon="13.443843">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.185739" lon="13.443913">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.185766" lon="13.444068">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.185759" lon="13.444165">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.185699" lon="13.444299">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.185638" lon="13.444379">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.185454" lon="13.444626">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.1854" lon="13.444744">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.184929" lon="13.446155">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.184593" lon="13.447013">
        <ele>98</ele>
      </trkpt>
      <trkpt lat="51.18428" lon="13.447711">
        <ele>97</ele>
      </trkpt>
      <trkpt lat="51.183769" lon="13.448746">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.183328" lon="13.449465">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.182753" lon="13.450382">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.182505" lon="13.450881">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.182323" lon="13.451208">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.182148" lon="13.451348">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.181993" lon="13.451546">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.181671" lon="13.452142">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.18159" lon="13.452265">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.181493" lon="13.452356">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.181439" lon="13.452426">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.18112" lon="13.453032">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.180998" lon="13.453204">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.180561" lon="13.453869">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.180383" lon="13.4541">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.180114" lon="13.454459">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.179936" lon="13.454631">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.179717" lon="13.454786">
        <ele>106</ele>
      </trkpt>
      <trkpt lat="51.179549" lon="13.454979">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.179337" lon="13.455237">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.179243" lon="13.455323">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.179128" lon="13.455387">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.178527" lon="13.455961">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.178395" lon="13.456165">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.178362" lon="13.456245">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.178056" lon="13.456514">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.177326" lon="13.457055">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.177218" lon="13.457141">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.176825" lon="13.457485">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.176512" lon="13.457817">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.176105" lon="13.45845">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.175998" lon="13.4586">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.175826" lon="13.458692">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.175746" lon="13.458777">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.175268" lon="13.459416">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.174276" lon="13.4608">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.17395" lon="13.461331">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.17368" lon="13.461803">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.173284" lon="13.462592">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.173196" lon="13.462806">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.173008" lon="13.463251">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.172752" lon="13.463938">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.172352" lon="13.465199">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.17215" lon="13.465971">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.172042" lon="13.466502">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.171575" lon="13.468213">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.171377" lon="13.4689">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.171363" lon="13.468997">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.171017" lon="13.469823">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.170943" lon="13.470118">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.170926" lon="13.470268">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.170943" lon="13.470375">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.170946" lon="13.470461">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.170714" lon="13.470735">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.170633" lon="13.470981">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.170617" lon="13.471099">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.17059" lon="13.471303">
        <ele>99</ele>
      </trkpt>
      <trkpt lat="51.170553" lon="13.471453">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.170492" lon="13.471588">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.170428" lon="13.471695">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.170307" lon="13.471797">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.170105" lon="13.471867">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.169832" lon="13.471904">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.169325" lon="13.47191">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.169285" lon="13.471883">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.169204" lon="13.471877">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.16914" lon="13.471958">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.169072" lon="13.472006">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.168218" lon="13.472698">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.16768" lon="13.473234">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.167612" lon="13.473277">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.167336" lon="13.473363">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.166637" lon="13.473674">
        <ele>105</ele>
      </trkpt>
      <trkpt lat="51.165634" lon="13.474061">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.164929" lon="13.47435">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.164787" lon="13.474404">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.16473" lon="13.474431">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.164572" lon="13.474527">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.164464" lon="13.474613">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.16442" lon="13.474683">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.16436" lon="13.47479">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.164037" lon="13.475061">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.163829" lon="13.475209">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.163807" lon="13.47515">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.163243" lon="13.475632">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.163058" lon="13.475981">
        <ele>100</ele>
      </trkpt>
      <trkpt lat="51.162836" lon="13.476201">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.162738" lon="13.476244">
        <ele>101</ele>
      </trkpt>
      <trkpt lat="51.162288" lon="13.4767">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.162058" lon="13.476945">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.162011" lon="13.476982">
        <ele>102</ele>
      </trkpt>
      <trkpt lat="51.161953" lon="13.477036">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.161859" lon="13.4771">
        <ele>103</ele>
      </trkpt>
      <trkpt lat="51.161725" lon="13.477234">
        <ele>104</ele>
      </trkpt>
      <trkpt lat="51.161584" lon="13.47739">
        <ele>105</ele>
      </trkpt>
    </trkseg>
  </trk>
</gpx>"""

fun riesaMeissenFixtureSource(): Source = Buffer().apply { writeString(RIESA_MEISSEN_GPX) }
