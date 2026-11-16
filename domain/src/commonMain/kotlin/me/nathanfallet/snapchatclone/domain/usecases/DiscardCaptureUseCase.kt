package me.nathanfallet.snapchatclone.domain.usecases

import me.nathanfallet.snapchatclone.domain.models.MemoryDraft

/**
 * Discards a snap the user did not save, deleting the temporary file the camera wrote for it, if any.
 */
interface DiscardCaptureUseCase {
    /** Discards the [draft]. */
    suspend operator fun invoke(draft: MemoryDraft)

    /** Deletes the temporary capture file at [path] (e.g. a photo once its filtered copy was made). */
    suspend operator fun invoke(path: String)
}
