package me.nathanfallet.snapchatclone.presentation

import kotlinx.coroutines.Dispatchers
import me.nathanfallet.snapchatclone.presentation.camera.CameraViewModel
import me.nathanfallet.snapchatclone.presentation.filters.SkiaPhotoFilterRenderer
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.TimeSource

/**
 * The Koin module of the presentation layer: its ViewModels.
 */
val presentationModule = module {
    viewModel {
        CameraViewModel(
            saveMemory = get(),
            readCapture = get(),
            discardCapture = get(),
            photoFilterRenderer = SkiaPhotoFilterRenderer(Dispatchers.Default),
            clock = Clock.System,
            timeSource = TimeSource.Monotonic,
        )
    }
}
