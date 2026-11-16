package me.nathanfallet.snapchatclone.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.nathanfallet.snapchatclone.presentation.camera.CameraScreen
import me.nathanfallet.snapchatclone.presentation.camera.CameraViewModel
import me.nathanfallet.snapchatclone.presentation.memories.MemoriesScreen
import me.nathanfallet.snapchatclone.presentation.memories.MemoriesViewModel
import me.nathanfallet.snapchatclone.presentation.theme.SnapCloneTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * The root of the app, shared by Android and iOS: the camera, with the Memories sliding up over it. Koin must be
 * started with the layers' modules (see the apps).
 */
@Composable
fun App() {
    SnapCloneTheme {
        var showMemories by rememberSaveable { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxSize()) {
            CameraScreen(
                viewModel = koinViewModel<CameraViewModel>(),
                // The camera stops while the Memories cover it.
                isVisible = !showMemories,
                onOpenMemories = { showMemories = true },
            )
            AnimatedVisibility(
                visible = showMemories,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                MemoriesScreen(viewModel = koinViewModel<MemoriesViewModel>(), onClose = { showMemories = false })
            }
        }
    }
}
