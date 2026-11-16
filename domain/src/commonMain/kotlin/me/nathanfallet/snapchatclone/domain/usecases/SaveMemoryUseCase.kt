package me.nathanfallet.snapchatclone.domain.usecases

import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft

/**
 * Saves a captured snap to the Memories.
 */
interface SaveMemoryUseCase {
    /** Saves the [draft], and returns the saved [Memory]. */
    suspend operator fun invoke(draft: MemoryDraft): Memory
}
