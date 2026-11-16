package me.nathanfallet.snapchatclone.presentation

import androidx.compose.runtime.Composable
import me.nathanfallet.snapchatclone.presentation.camera.CameraScreen
import me.nathanfallet.snapchatclone.presentation.camera.CameraViewModel
import me.nathanfallet.snapchatclone.presentation.theme.SnapCloneTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * The root of the app, shared by Android and iOS. Koin must be started with the layers' modules (see the apps).
 */
@Composable
fun App() {
    SnapCloneTheme {
        val cameraViewModel = koinViewModel<CameraViewModel>()
        CameraScreen(viewModel = cameraViewModel, isVisible = true, onOpenMemories = {})
    }
}
