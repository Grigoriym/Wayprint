package com.grappim.wayprint.feature.wayprint.ui.platform

import androidx.compose.runtime.Composable

/**
 * Returns a launch function that opens the platform's file picker filtered to GPX files, calling
 * [onPick] with the result. Android's `actual` uses `ActivityResultContracts.GetContent()`;
 * JVM's uses a Swing/AWT `FileDialog`.
 */
@Composable
expect fun rememberGpxPickerLauncher(onPick: (PlatformFileHandle) -> Unit): () -> Unit
