package me.nathanfallet.snapchatclone.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryDraft
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import me.nathanfallet.snapchatclone.domain.repositories.CapturesRepository
import me.nathanfallet.snapchatclone.domain.repositories.MemoriesRepository
import kotlin.time.Instant

/** An in-memory [MemoriesRepository] recording what it was asked. */
class FakeMemoriesRepository(initial: List<Memory> = emptyList()) : MemoriesRepository {
    val state = MutableStateFlow(initial)
    val savedDrafts = mutableListOf<MemoryDraft>()
    val deletedIds = mutableListOf<String>()

    override val memories: Flow<List<Memory>> = state

    override suspend fun saveMemory(draft: MemoryDraft): Memory {
        savedDrafts += draft
        val memory = memory(id = "saved-${savedDrafts.size}", capturedAt = draft.capturedAt, type = draft.type)
        state.value += memory
        return memory
    }

    override suspend fun deleteMemory(id: String) {
        deletedIds += id
        state.value = state.value.filterNot { it.id == id }
    }
}

/** A [CapturesRepository] serving [files] and recording the deleted paths. */
class FakeCapturesRepository(private val files: Map<String, ByteArray> = emptyMap()) : CapturesRepository {
    val deletedPaths = mutableListOf<String>()

    override suspend fun readCapture(path: String): ByteArray = files.getValue(path)

    override suspend fun deleteCapture(path: String) {
        deletedPaths += path
    }
}

/** Creates a [Memory] for tests. */
fun memory(
    id: String,
    capturedAt: Instant = Instant.fromEpochMilliseconds(0),
    type: MemoryType = MemoryType.PHOTO,
) = Memory(
    id = id,
    type = type,
    capturedAt = capturedAt,
    mediaPath = "/memories/$id.jpg",
    thumbnailPath = null,
    filter = Filter.NORMAL,
    location = null,
    duration = null,
)
