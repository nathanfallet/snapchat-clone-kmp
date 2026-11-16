package me.nathanfallet.snapchatclone.presentation.memories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import me.nathanfallet.snapchatclone.domain.usecases.DeleteMemoryUseCase
import me.nathanfallet.snapchatclone.domain.usecases.GetMemoriesUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MemoriesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val memories = MutableStateFlow(
        listOf(
            memory("b", Instant.parse("2026-11-16T14:03:00Z")),
            memory("a", Instant.parse("2026-10-02T09:00:00Z")),
        ),
    )
    private var deleteFailure: Exception? = null
    private val getMemories = object : GetMemoriesUseCase {
        override fun invoke(): Flow<List<Memory>> = memories
    }
    private val deleteMemory = object : DeleteMemoryUseCase {
        override suspend fun invoke(id: String) {
            deleteFailure?.let { throw it }
            memories.update { list -> list.filterNot { it.id == id } }
        }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsTheMemoriesGroupedByMonth() = runTest(dispatcher) {
        val viewModel = MemoriesViewModel(getMemories, deleteMemory, TimeZone.UTC)
        assertTrue(viewModel.uiState.value.isLoading)
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("b", "a"), state.memories.map { it.id })
        assertEquals(listOf("November 2026", "October 2026"), state.sections.map { it.title })
    }

    @Test
    fun formatsTheDateInItsTimeZone() {
        val viewModel = MemoriesViewModel(getMemories, deleteMemory, TimeZone.of("Europe/Paris"))
        assertEquals("Nov 16, 2026 at 15:03", viewModel.formattedDate(memories.value.first()))
    }

    @Test
    fun deletingAMemoryRemovesItFromTheState() = runTest(dispatcher) {
        val viewModel = MemoriesViewModel(getMemories, deleteMemory, TimeZone.UTC)
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.delete(memories.value.first())
        runCurrent()

        assertEquals(listOf("a"), viewModel.uiState.value.memories.map { it.id })
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun aFailedDeletionShowsAMessageForAWhile() = runTest(dispatcher) {
        deleteFailure = IllegalStateException("disk full")
        val viewModel = MemoriesViewModel(getMemories, deleteMemory, TimeZone.UTC)
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()

        viewModel.delete(memories.value.first())
        runCurrent()
        assertEquals("Couldn't delete: disk full", viewModel.uiState.value.message)
        assertEquals(2, viewModel.uiState.value.memories.size)

        advanceTimeBy(3.seconds)
        runCurrent()
        assertNull(viewModel.uiState.value.message)
    }

    private fun memory(id: String, capturedAt: Instant) = Memory(
        id = id,
        type = MemoryType.PHOTO,
        capturedAt = capturedAt,
        mediaPath = "/memories/$id.jpg",
        thumbnailPath = null,
        filter = Filter.NORMAL,
        location = null,
        duration = null,
    )
}
