package me.nathanfallet.snapchatclone.domain.usecases

import me.nathanfallet.snapchatclone.domain.repositories.MemoriesRepository

/**
 * Deletes memories from the [repository].
 */
class DeleteMemoryUseCaseImpl(private val repository: MemoriesRepository) : DeleteMemoryUseCase {
    override suspend fun invoke(id: String): Unit = repository.deleteMemory(id)
}
