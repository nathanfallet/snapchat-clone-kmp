package me.nathanfallet.snapchatclone.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.nathanfallet.snapchatclone.data.repositories.CapturesRepositoryImpl
import me.nathanfallet.snapchatclone.data.repositories.MemoriesRepositoryImpl
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.GeoPoint
import me.nathanfallet.snapchatclone.domain.models.MediaSource
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class MemoriesRepositoryImplTest {
    private val fileSystem = FakeFileSystem()
    private val directory = "/app/files/memories".toPath()
    private var nextId = 0

    @AfterTest
    fun tearDown() = fileSystem.checkNoOpenFiles()

    private fun TestScope.repository() = MemoriesRepositoryImpl(
        fileSystem = fileSystem,
        directory = directory,
        ioDispatcher = StandardTestDispatcher(testScheduler),
        generateId = { "id${++nextId}" },
    )

    private fun photoDraft(bytes: ByteArray = byteArrayOf(1, 2, 3)) = MemoryDraft(
        type = MemoryType.PHOTO,
        media = MediaSource.Bytes(bytes, "jpg"),
        thumbnail = null,
        capturedAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
        filter = Filter.VINTAGE,
        location = GeoPoint(48.8566, 2.3522),
        duration = null,
    )

    @Test
    fun savesAPhotoFromBytes() = runTest {
        val memory = repository().saveMemory(photoDraft())
        assertEquals("id1", memory.id)
        assertEquals(MemoryType.PHOTO, memory.type)
        assertEquals("/app/files/memories/id1.jpg", memory.mediaPath)
        assertEquals(Filter.VINTAGE, memory.filter)
        assertEquals(GeoPoint(48.8566, 2.3522), memory.location)
        assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), memory.capturedAt)
        assertNull(memory.duration)
        assertContentEquals(byteArrayOf(1, 2, 3), fileSystem.read(directory / "id1.jpg") { readByteArray() })
    }

    @Test
    fun savesAVideoByMovingTheRecordedFileWithItsThumbnail() = runTest {
        val recorded = "/tmp/VisionCamera_123.MOV".toPath()
        fileSystem.createDirectories(recorded.parent!!)
        fileSystem.write(recorded) { write(byteArrayOf(9, 9)) }
        val draft = MemoryDraft(
            type = MemoryType.VIDEO,
            media = MediaSource.File(recorded.toString()),
            thumbnail = byteArrayOf(7),
            capturedAt = Instant.fromEpochMilliseconds(5),
            filter = Filter.NORMAL,
            location = null,
            duration = 12.seconds,
        )
        val memory = repository().saveMemory(draft)
        assertEquals("/app/files/memories/id1.mov", memory.mediaPath)
        assertEquals("/app/files/memories/id1_thumbnail.jpg", memory.thumbnailPath)
        assertEquals(12.seconds, memory.duration)
        assertFalse(fileSystem.exists(recorded))
        assertContentEquals(byteArrayOf(9, 9), fileSystem.read(directory / "id1.mov") { readByteArray() })
        assertContentEquals(byteArrayOf(7), fileSystem.read(directory / "id1_thumbnail.jpg") { readByteArray() })
    }

    @Test
    fun memoriesFlowFollowsSavesAndDeletes() = runTest {
        val repository = repository()
        assertEquals(emptyList(), repository.memories.first())
        repository.saveMemory(photoDraft())
        repository.saveMemory(photoDraft())
        assertEquals(listOf("id1", "id2"), repository.memories.first().map { it.id })
        repository.deleteMemory("id1")
        assertEquals(listOf("id2"), repository.memories.first().map { it.id })
    }

    @Test
    fun deleteRemovesTheFiles() = runTest {
        val repository = repository()
        repository.saveMemory(photoDraft())
        repository.deleteMemory("id1")
        assertFalse(fileSystem.exists(directory / "id1.jpg"))
        repository.deleteMemory("unknown")
    }

    @Test
    fun theIndexIsPersistedAcrossInstances() = runTest {
        repository().saveMemory(photoDraft())
        val reloaded = repository().memories.first()
        assertEquals(listOf("id1"), reloaded.map { it.id })
        assertEquals(GeoPoint(48.8566, 2.3522), reloaded.single().location)
        assertTrue(fileSystem.exists(directory / "index.json"))
        assertFalse(fileSystem.exists(directory / "index.json.tmp"))
    }

    @Test
    fun memoriesWhoseMediaIsMissingAreHidden() = runTest {
        repository().saveMemory(photoDraft())
        repository().saveMemory(photoDraft())
        fileSystem.delete(directory / "id1.jpg")
        assertEquals(listOf("id2"), repository().memories.first().map { it.id })
    }

    @Test
    fun aCorruptedIndexIsKeptAsideInsteadOfOverwritten() = runTest {
        fileSystem.createDirectories(directory)
        fileSystem.write(directory / "index.json") { writeUtf8("{ not json") }
        val repository = repository()
        assertEquals(emptyList(), repository.memories.first())
        assertEquals("{ not json", fileSystem.read(directory / "index.corrupted.json") { readUtf8() })
        repository.saveMemory(photoDraft())
        assertEquals(listOf("id1"), repository.memories.first().map { it.id })
    }

    @Test
    fun unknownEntriesAreSkipped() = runTest {
        fileSystem.createDirectories(directory)
        fileSystem.write(directory / "a.jpg") { writeUtf8("a") }
        fileSystem.write(directory / "b.gif") { writeUtf8("b") }
        fileSystem.write(directory / "index.json") {
            writeUtf8(
                """
                {"version":2,"memories":[
                  {"id":"a","type":"PHOTO","capturedAt":1,"mediaFile":"a.jpg","filter":"NEW_FILTER","extra":true},
                  {"id":"b","type":"HOLOGRAM","capturedAt":2,"mediaFile":"b.gif","filter":"NORMAL"}
                ]}
                """.trimIndent(),
            )
        }
        val memories = repository().memories.first()
        assertEquals(listOf("a"), memories.map { it.id })
        assertEquals(Filter.NORMAL, memories.single().filter)
    }

    @Test
    fun capturesRepositoryReadsAndDeletesTemporaryFiles() = runTest {
        val path = "/tmp/photo.jpg".toPath()
        fileSystem.createDirectories(path.parent!!)
        fileSystem.write(path) { writeUtf8("photo") }
        val repository = CapturesRepositoryImpl(fileSystem, StandardTestDispatcher(testScheduler))
        assertEquals("photo", repository.readCapture(path.toString()).decodeToString())
        repository.deleteCapture(path.toString())
        assertFalse(fileSystem.exists(path))
        repository.deleteCapture(path.toString())
    }
}
