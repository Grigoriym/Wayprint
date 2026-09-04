package com.grappim.wayprint.feature.wayprint.ui.platform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberGpxPickerLauncher(onPick: (PlatformFileHandle) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) onPick(PlatformFileHandle(uri, context))
    }
    return { launcher.launch("*/*") }
}
