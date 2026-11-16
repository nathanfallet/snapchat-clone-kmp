package me.nathanfallet.snapchatclone.domain.usecases

import me.nathanfallet.snapchatclone.domain.models.MediaSource
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.repositories.CapturesRepository

/**
 * Discards captures with the [repository]: in-memory media need nothing, temporary files are deleted.
 */
class DiscardCaptureUseCaseImpl(private val repository: CapturesRepository) : DiscardCaptureUseCase {
    override suspend fun invoke(draft: MemoryDraft) {
        val media = draft.media
        if (media is MediaSource.File) repository.deleteCapture(media.path)
    }

    override suspend fun invoke(path: String): Unit = repository.deleteCapture(path)
}
