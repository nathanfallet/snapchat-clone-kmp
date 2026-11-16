package me.nathanfallet.snapchatclone.data.repositories

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import me.nathanfallet.snapchatclone.data.models.MemoriesIndex
import me.nathanfallet.snapchatclone.data.models.MemoryEntity
import me.nathanfallet.snapchatclone.data.models.toMemory
import me.nathanfallet.snapchatclone.domain.models.MediaSource
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import me.nathanfallet.snapchatclone.domain.repositories.MemoriesRepository
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import kotlin.uuid.Uuid

/**
 * Stores memories as files in [directory] (e.g. `<app files>/memories`), next to a JSON index (`index.json`).
 *
 * The index is read once and cached; every change rewrites it atomically (written to a temporary file, then moved
 * over the previous one), so a crash never leaves a half-written index.
 *
 * @param fileSystem The file system: the real one in the app, a fake one in tests.
 * @param directory The private directory of the memories.
 * @param ioDispatcher The dispatcher running file operations.
 * @param generateId Generates the identifier of a new memory.
 */
class MemoriesRepositoryImpl(
    private val fileSystem: FileSystem,
    private val directory: Path,
    private val ioDispatcher: CoroutineDispatcher,
    private val generateId: () -> String = { Uuid.random().toString() },
) : MemoriesRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private val index = MutableStateFlow<MemoriesIndex?>(null)

    private val indexPath get() = directory / INDEX_FILE

    override val memories: Flow<List<Memory>> = flow {
        mutex.withLock { loadIndex() }
        emitAll(index.filterNotNull().map { it.memories.mapNotNull { entity -> entity.toMemory(directory) } })
    }

    override suspend fun saveMemory(draft: MemoryDraft): Memory = mutex.withLock {
        withContext(ioDispatcher) {
            val current = loadIndex()
            val id = generateId()
            fileSystem.createDirectories(directory)
            val mediaFile = "$id.${draft.extension}"
            when (val media = draft.media) {
                is MediaSource.Bytes -> fileSystem.write(directory / mediaFile) { write(media.bytes) }
                is MediaSource.File -> moveFile(media.path.toPath(), directory / mediaFile)
            }
            val thumbnailFile = draft.thumbnail?.let { bytes ->
                "${id}_thumbnail.jpg".also { name -> fileSystem.write(directory / name) { write(bytes) } }
            }
            val entity = MemoryEntity(
                id = id,
                type = draft.type.name,
                capturedAt = draft.capturedAt.toEpochMilliseconds(),
                mediaFile = mediaFile,
                thumbnailFile = thumbnailFile,
                filter = draft.filter.name,
                latitude = draft.location?.latitude,
                longitude = draft.location?.longitude,
                durationMillis = draft.duration?.inWholeMilliseconds,
            )
            writeIndex(current.copy(memories = current.memories + entity))
            checkNotNull(entity.toMemory(directory))
        }
    }

    override suspend fun deleteMemory(id: String) {
        mutex.withLock {
            withContext(ioDispatcher) {
                val current = loadIndex()
                val entity = current.memories.firstOrNull { it.id == id } ?: return@withContext
                writeIndex(current.copy(memories = current.memories - entity))
                fileSystem.delete(directory / entity.mediaFile, mustExist = false)
                entity.thumbnailFile?.let { fileSystem.delete(directory / it, mustExist = false) }
            }
        }
    }

    /** Reads the index if it isn't cached yet. Must be called with the [mutex] held. */
    private suspend fun loadIndex(): MemoriesIndex = index.value ?: withContext(ioDispatcher) {
        readIndex().also { index.value = it }
    }

    private fun readIndex(): MemoriesIndex {
        if (!fileSystem.exists(indexPath)) return MemoriesIndex()
        val content = fileSystem.read(indexPath) { readUtf8() }
        return try {
            val stored = json.decodeFromString<MemoriesIndex>(content)
            // Entries whose media file was removed (e.g. by a backup restore) can't be shown.
            stored.copy(memories = stored.memories.filter { fileSystem.exists(directory / it.mediaFile) })
        } catch (e: SerializationException) {
            // Keep the unreadable index aside instead of overwriting it on the next save, and start over.
            println("Unreadable memories index, moved to $CORRUPTED_INDEX_FILE: ${e.message}")
            fileSystem.atomicMove(indexPath, directory / CORRUPTED_INDEX_FILE)
            MemoriesIndex()
        }
    }

    private fun writeIndex(newIndex: MemoriesIndex) {
        fileSystem.createDirectories(directory)
        val temporary = directory / "$INDEX_FILE.tmp"
        fileSystem.write(temporary) { writeUtf8(json.encodeToString(newIndex)) }
        fileSystem.atomicMove(temporary, indexPath)
        index.value = newIndex
    }

    /** Moves [source] to [target], copying it when it is on another volume (e.g. the system's temporary directory). */
    private fun moveFile(source: Path, target: Path) {
        try {
            fileSystem.atomicMove(source, target)
        } catch (e: IOException) {
            println("Copying $source instead of moving it: ${e.message}")
            fileSystem.copy(source, target)
            fileSystem.delete(source, mustExist = false)
        }
    }

    private val MemoryDraft.extension: String
        get() = when (val media = media) {
            is MediaSource.Bytes -> media.extension
            is MediaSource.File -> media.path.substringAfterLast('/').substringAfterLast('.', "")
                .lowercase().ifEmpty { if (type == MemoryType.VIDEO) "mp4" else "jpg" }
        }

    private companion object {
        const val INDEX_FILE = "index.json"
        const val CORRUPTED_INDEX_FILE = "index.corrupted.json"
    }
}
