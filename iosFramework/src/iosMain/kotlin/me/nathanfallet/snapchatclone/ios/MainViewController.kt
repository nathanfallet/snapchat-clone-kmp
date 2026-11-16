package me.nathanfallet.snapchatclone.ios

import androidx.compose.ui.window.ComposeUIViewController
import me.nathanfallet.snapchatclone.data.dataModule
import me.nathanfallet.snapchatclone.domain.domainModule
import me.nathanfallet.snapchatclone.presentation.App
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

/**
 * The entry point of the iOS app, used by the Xcode project in iosApp. It assembles the layers with Koin.
 */
@Suppress("FunctionNaming")
fun MainViewController(): UIViewController {
    if (KoinPlatform.getKoinOrNull() == null) {
        startKoin { modules(domainModule, dataModule) }
    }
    return ComposeUIViewController { App() }
}
