package me.nathanfallet.snapchatclone.data

import org.koin.android.ext.koin.androidContext
import okio.FileSystem
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

/** On Android, memories are stored in the app's internal storage (`Context.filesDir`), private to the app. */
internal actual val platformDataModule: Module = module {
    single<FileSystem> { FileSystem.SYSTEM }
    single { AppDirectories(memories = File(androidContext().filesDir, "memories").absolutePath) }
}
