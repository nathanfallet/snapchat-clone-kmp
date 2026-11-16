package me.nathanfallet.snapchatclone.domain.usecases

import me.nathanfallet.snapchatclone.domain.models.MediaSource
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import me.nathanfallet.snapchatclone.domain.repositories.MemoriesRepository

/**
 * Saves snaps with the [repository], after checking they are consistent: a video has a duration and comes from a
 * file, a photo has no duration.
 */
class SaveMemoryUseCaseImpl(private val repository: MemoriesRepository) : SaveMemoryUseCase {
    override suspend fun invoke(draft: MemoryDraft): Memory {
        when (draft.type) {
            MemoryType.PHOTO -> require(draft.duration == null) { "A photo has no duration" }
            MemoryType.VIDEO -> {
                require(draft.duration != null) { "A video needs a duration" }
                require(draft.media is MediaSource.File) { "A video is saved from the file the camera recorded" }
            }
        }
        if (draft.media is MediaSource.Bytes) require(draft.media.bytes.isNotEmpty()) { "The media is empty" }
        return repository.saveMemory(draft)
    }
}
