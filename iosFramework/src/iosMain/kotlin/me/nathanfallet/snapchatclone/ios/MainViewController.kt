package me.nathanfallet.snapchatclone.ios

import androidx.compose.ui.window.ComposeUIViewController
import me.nathanfallet.snapchatclone.presentation.App
import platform.UIKit.UIViewController

/**
 * The entry point of the iOS app, used by the Xcode project in iosApp.
 */
@Suppress("FunctionNaming")
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
