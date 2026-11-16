package me.nathanfallet.snapchatclone.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import me.nathanfallet.snapchatclone.presentation.camera.CameraViewModel
import me.nathanfallet.snapchatclone.presentation.filters.SkiaPhotoFilterRenderer
import me.nathanfallet.snapchatclone.presentation.memories.MemoriesViewModel
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
    viewModel {
        MemoriesViewModel(getMemories = get(), deleteMemory = get(), timeZone = TimeZone.currentSystemDefault())
    }
}
