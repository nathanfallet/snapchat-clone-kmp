package me.nathanfallet.snapchatclone.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.repositories.MemoriesRepository

/**
 * Observes the memories of the [repository], sorted by capture date, newest first (ties broken by id, so the order
 * is stable).
 */
class GetMemoriesUseCaseImpl(private val repository: MemoriesRepository) : GetMemoriesUseCase {
    override fun invoke(): Flow<List<Memory>> = repository.memories.map { memories ->
        memories.sortedWith(compareByDescending<Memory> { it.capturedAt }.thenByDescending { it.id })
    }
}
