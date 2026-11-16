package me.nathanfallet.snapchatclone.domain.usecases

/**
 * Deletes a memory and its files.
 */
interface DeleteMemoryUseCase {
    /** Deletes the memory with the given [id]. */
    suspend operator fun invoke(id: String)
}
