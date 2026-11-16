package me.nathanfallet.snapchatclone.domain

import me.nathanfallet.snapchatclone.domain.usecases.DeleteMemoryUseCase
import me.nathanfallet.snapchatclone.domain.usecases.DeleteMemoryUseCaseImpl
import me.nathanfallet.snapchatclone.domain.usecases.DiscardCaptureUseCase
import me.nathanfallet.snapchatclone.domain.usecases.DiscardCaptureUseCaseImpl
import me.nathanfallet.snapchatclone.domain.usecases.GetMemoriesUseCase
import me.nathanfallet.snapchatclone.domain.usecases.GetMemoriesUseCaseImpl
import me.nathanfallet.snapchatclone.domain.usecases.ReadCaptureUseCase
import me.nathanfallet.snapchatclone.domain.usecases.ReadCaptureUseCaseImpl
import me.nathanfallet.snapchatclone.domain.usecases.SaveMemoryUseCase
import me.nathanfallet.snapchatclone.domain.usecases.SaveMemoryUseCaseImpl
import org.koin.dsl.module

/**
 * The Koin module of the domain layer: its use cases. The repositories they need come from the data layer.
 */
val domainModule = module {
    factory<GetMemoriesUseCase> { GetMemoriesUseCaseImpl(get()) }
    factory<SaveMemoryUseCase> { SaveMemoryUseCaseImpl(get()) }
    factory<DeleteMemoryUseCase> { DeleteMemoryUseCaseImpl(get()) }
    factory<ReadCaptureUseCase> { ReadCaptureUseCaseImpl(get()) }
    factory<DiscardCaptureUseCase> { DiscardCaptureUseCaseImpl(get()) }
}
