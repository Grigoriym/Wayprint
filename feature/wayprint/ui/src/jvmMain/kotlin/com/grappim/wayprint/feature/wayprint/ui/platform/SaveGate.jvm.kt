package com.grappim.wayprint.feature.wayprint.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** Not wired into any Gradle target yet — see `ImageExporter.jvm.kt`'s doc. */
@Composable
actual fun rememberGatedSaveAction(save: (ImageBitmap) -> Unit): (ImageBitmap) -> Unit = save
