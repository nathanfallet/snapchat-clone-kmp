package me.nathanfallet.snapchatclone.domain.usecases

import kotlinx.coroutines.test.runTest
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.MediaSource
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class CaptureAndDeleteUseCasesTest {
    @Test
    fun deleteRemovesTheMemory() = runTest {
        val repository = FakeMemoriesRepository(listOf(memory("a"), memory("b")))
        DeleteMemoryUseCaseImpl(repository)("a")
        assertEquals(listOf("a"), repository.deletedIds)
        assertEquals(listOf("b"), repository.state.value.map { it.id })
    }

    @Test
    fun discardingAVideoDeletesItsTemporaryFile() = runTest {
        val repository = FakeCapturesRepository()
        val draft = MemoryDraft(
            type = MemoryType.VIDEO,
            media = MediaSource.File("/tmp/video.mp4"),
            thumbnail = null,
            capturedAt = Instant.fromEpochMilliseconds(0),
            filter = Filter.NORMAL,
            location = null,
            duration = 2.seconds,
        )
        DiscardCaptureUseCaseImpl(repository)(draft)
        assertEquals(listOf("/tmp/video.mp4"), repository.deletedPaths)
    }

    @Test
    fun discardingAPhotoInMemoryDeletesNothing() = runTest {
        val repository = FakeCapturesRepository()
        val draft = MemoryDraft(
            type = MemoryType.PHOTO,
            media = MediaSource.Bytes(byteArrayOf(1), "jpg"),
            thumbnail = null,
            capturedAt = Instant.fromEpochMilliseconds(0),
            filter = Filter.NORMAL,
            location = null,
            duration = null,
        )
        DiscardCaptureUseCaseImpl(repository)(draft)
        assertEquals(emptyList(), repository.deletedPaths)
    }

    @Test
    fun readingACaptureReadsItsFile() = runTest {
        val repository = FakeCapturesRepository(mapOf("/tmp/photo.jpg" to byteArrayOf(4, 2)))
        assertEquals(listOf<Byte>(4, 2), ReadCaptureUseCaseImpl(repository)("/tmp/photo.jpg").toList())
    }

    @Test
    fun discardingAPathDeletesIt() = runTest {
        val repository = FakeCapturesRepository()
        DiscardCaptureUseCaseImpl(repository)("/tmp/photo.jpg")
        assertEquals(listOf("/tmp/photo.jpg"), repository.deletedPaths)
    }
}
