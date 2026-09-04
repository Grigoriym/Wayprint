package com.grappim.wayprint.feature.wayprint.ui.platform

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberGatedSaveAction(save: (ImageBitmap) -> Unit): (ImageBitmap) -> Unit {
    val context = LocalContext.current
    var pendingSave by remember { mutableStateOf<ImageBitmap?>(null) }
    val requestSavePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pendingSave?.let(save)
        pendingSave = null
    }
    return { image ->
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingSave = image
            requestSavePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            save(image)
        }
    }
}
