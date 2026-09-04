package com.grappim.wayprint.feature.wayprint.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * iOS has no runtime permission request for adding to the photo library — the system prompts
 * automatically the first time `PHPhotoLibrary` is written to ([ImageExporter.saveToGallery]'s
 * `actual`) — so [save] runs straight through, same as Desktop's `actual`.
 */
@Composable
actual fun rememberGatedSaveAction(save: (ImageBitmap) -> Unit): (ImageBitmap) -> Unit = save
