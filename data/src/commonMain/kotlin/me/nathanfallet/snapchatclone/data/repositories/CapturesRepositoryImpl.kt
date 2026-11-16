package me.nathanfallet.snapchatclone.data.repositories

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.nathanfallet.snapchatclone.domain.repositories.CapturesRepository
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Reads and deletes the temporary files the camera writes, with the given [fileSystem].
 */
class CapturesRepositoryImpl(
    private val fileSystem: FileSystem,
    private val ioDispatcher: CoroutineDispatcher,
) : CapturesRepository {
    override suspend fun readCapture(path: String): ByteArray = withContext(ioDispatcher) {
        fileSystem.read(path.toPath()) { readByteArray() }
    }

    override suspend fun deleteCapture(path: String): Unit = withContext(ioDispatcher) {
        fileSystem.delete(path.toPath(), mustExist = false)
    }
}
