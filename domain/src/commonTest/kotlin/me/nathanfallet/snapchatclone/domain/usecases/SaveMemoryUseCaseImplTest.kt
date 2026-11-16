package me.nathanfallet.snapchatclone.domain.usecases

import kotlinx.coroutines.test.runTest
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.MediaSource
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SaveMemoryUseCaseImplTest {
    private fun draft(type: MemoryType, media: MediaSource, duration: Duration?) = MemoryDraft(
        type = type,
        media = media,
        thumbnail = null,
        capturedAt = Instant.fromEpochMilliseconds(42),
        filter = Filter.WARM,
        location = null,
        duration = duration,
    )

    @Test
    fun savesAPhoto() = runTest {
        val repository = FakeMemoriesRepository()
        val draft = draft(MemoryType.PHOTO, MediaSource.Bytes(byteArrayOf(1, 2), "jpg"), null)
        val memory = SaveMemoryUseCaseImpl(repository)(draft)
        assertEquals(listOf(draft), repository.savedDrafts)
        assertEquals(MemoryType.PHOTO, memory.type)
    }

    @Test
    fun savesAVideo() = runTest {
        val repository = FakeMemoriesRepository()
        SaveMemoryUseCaseImpl(repository)(draft(MemoryType.VIDEO, MediaSource.File("/tmp/video.mp4"), 3.seconds))
        assertEquals(1, repository.savedDrafts.size)
    }

    @Test
    fun rejectsInconsistentDrafts() = runTest {
        val repository = FakeMemoriesRepository()
        val useCase = SaveMemoryUseCaseImpl(repository)
        assertFailsWith<IllegalArgumentException> {
            useCase(draft(MemoryType.VIDEO, MediaSource.File("/tmp/video.mp4"), null))
        }
        assertFailsWith<IllegalArgumentException> {
            useCase(draft(MemoryType.VIDEO, MediaSource.Bytes(byteArrayOf(1), "mp4"), 1.seconds))
        }
        assertFailsWith<IllegalArgumentException> {
            useCase(draft(MemoryType.PHOTO, MediaSource.Bytes(byteArrayOf(1), "jpg"), 1.seconds))
        }
        assertFailsWith<IllegalArgumentException> {
            useCase(draft(MemoryType.PHOTO, MediaSource.Bytes(byteArrayOf(), "jpg"), null))
        }
        assertTrue(repository.savedDrafts.isEmpty())
    }
}
