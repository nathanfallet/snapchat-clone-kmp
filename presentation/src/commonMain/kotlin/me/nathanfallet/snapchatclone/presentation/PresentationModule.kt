package me.nathanfallet.snapchatclone.presentation

import me.nathanfallet.snapchatclone.presentation.camera.CameraViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.TimeSource

/**
 * The Koin module of the presentation layer: its ViewModels.
 */
val presentationModule = module {
    viewModel { CameraViewModel(get(), get(), Clock.System, TimeSource.Monotonic) }
}
