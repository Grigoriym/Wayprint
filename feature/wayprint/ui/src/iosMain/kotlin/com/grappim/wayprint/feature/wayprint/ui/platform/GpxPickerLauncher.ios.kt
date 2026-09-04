package com.grappim.wayprint.feature.wayprint.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.darwin.NSObject

/**
 * Presents `UIDocumentPickerViewController` (import mode) from the current window's top-most
 * [UIViewController], filtered to GPX (no registered system UTI, so `typeWithFilenameExtension`
 * is used with a fallback to any file). [GpxDocumentPickerDelegate] is kept alive by [remember]
 * for as long as this composable is in the tree — `UIDocumentPickerViewController.delegate` is a
 * weak reference, so nothing else holds it.
 */
@Composable
actual fun rememberGpxPickerLauncher(onPick: (PlatformFileHandle) -> Unit): () -> Unit {
    val currentOnPick = rememberUpdatedState(onPick)
    val delegate = remember {
        GpxDocumentPickerDelegate { url -> currentOnPick.value(PlatformFileHandle(url)) }
    }
    return remember {
        {
            val contentTypes = listOfNotNull(UTType.typeWithFilenameExtension("gpx")).ifEmpty { listOf(UTTypeItem) }
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = contentTypes)
            picker.delegate = delegate
            topMostViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class GpxDocumentPickerDelegate(private val onPick: (NSURL) -> Unit) :
    NSObject(),
    UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL: NSURL) {
        onPick(didPickDocumentAtURL)
    }

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.let(onPick)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) = Unit
}

/** The frontmost presented [UIViewController] of the foreground-active scene's key window. */
internal fun topMostViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?.keyWindow
    var top = keyWindow?.rootViewController
    while (top?.presentedViewController != null) {
        top = top.presentedViewController
    }
    return top
}
