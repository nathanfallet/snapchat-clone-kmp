package me.nathanfallet.snapchatclone.domain.repositories

import kotlinx.coroutines.flow.Flow
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft

/**
 * Stores the [Memory]s of the user on the device.
 */
interface MemoriesRepository {
    /** The saved memories, in no particular order, updated whenever one is saved or deleted. */
    val memories: Flow<List<Memory>>

    /**
     * Saves the given [draft]: its media is written (or moved, if it is a temporary file) to the memories' storage.
     *
     * @return The saved [Memory].
     */
    suspend fun saveMemory(draft: MemoryDraft): Memory

    /** Deletes the memory with the given [id] and its files. Does nothing if it does not exist. */
    suspend fun deleteMemory(id: String)
}
