package me.nathanfallet.snapchatclone.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.nathanfallet.snapchatclone.data.repositories.CapturesRepositoryImpl
import me.nathanfallet.snapchatclone.data.repositories.MemoriesRepositoryImpl
import me.nathanfallet.snapchatclone.domain.repositories.CapturesRepository
import me.nathanfallet.snapchatclone.domain.repositories.MemoriesRepository
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The Koin module of the data layer: the repositories' implementations, storing memories in the app's private
 * directory.
 */
val dataModule = module {
    includes(platformDataModule)
    single<MemoriesRepository> {
        MemoriesRepositoryImpl(
            fileSystem = get(),
            directory = get<AppDirectories>().memories.toPath(),
            ioDispatcher = Dispatchers.IO,
        )
    }
    single<CapturesRepository> { CapturesRepositoryImpl(get(), Dispatchers.IO) }
}

/**
 * The directories of the app on the current platform.
 *
 * @property memories The private directory where memories are stored.
 */
data class AppDirectories(val memories: String)

/** The platform part of [dataModule], providing the system's `FileSystem` and the [AppDirectories]. */
internal expect val platformDataModule: Module
