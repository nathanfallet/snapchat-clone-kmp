package me.nathanfallet.snapchatclone.domain.usecases

import kotlinx.coroutines.flow.Flow
import me.nathanfallet.snapchatclone.domain.models.Memory

/**
 * Observes the saved memories, newest first.
 */
interface GetMemoriesUseCase {
    /** Returns the saved memories, newest first, updated whenever one is saved or deleted. */
    operator fun invoke(): Flow<List<Memory>>
}
