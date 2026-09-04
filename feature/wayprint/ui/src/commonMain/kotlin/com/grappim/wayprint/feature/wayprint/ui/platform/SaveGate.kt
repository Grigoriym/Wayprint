package com.grappim.wayprint.feature.wayprint.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Wraps [save] with whatever the platform needs before it's safe to call — Android's pre-Q
 * `WRITE_EXTERNAL_STORAGE` runtime permission request; nothing on Desktop, which has no such
 * permission model, so [save] runs straight through.
 */
@Composable
expect fun rememberGatedSaveAction(save: (ImageBitmap) -> Unit): (ImageBitmap) -> Unit
