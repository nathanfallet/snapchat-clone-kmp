package me.nathanfallet.snapchatclone.data

import okio.FileSystem
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * On iOS, memories are stored in the app's Application Support directory, private to the app (unlike Documents, it
 * isn't shown in the Files app).
 */
internal actual val platformDataModule: Module = module {
    single<FileSystem> { FileSystem.SYSTEM }
    single {
        val applicationSupport = NSSearchPathForDirectoriesInDomains(
            directory = NSApplicationSupportDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        ).first() as String
        AppDirectories(memories = "$applicationSupport/memories")
    }
}
