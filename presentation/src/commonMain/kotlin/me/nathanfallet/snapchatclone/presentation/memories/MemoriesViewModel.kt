package me.nathanfallet.snapchatclone.presentation.memories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.usecases.DeleteMemoryUseCase
import me.nathanfallet.snapchatclone.domain.usecases.GetMemoriesUseCase
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

/**
 * The state of the Memories screens.
 *
 * @property isLoading Whether the memories are still being read.
 * @property memories Every memory, newest first (the order of the full-screen viewer).
 * @property sections The memories grouped by month, for the grid.
 * @property message A short message to show the user, if any.
 */
data class MemoriesUiState(
    val isLoading: Boolean = true,
    val memories: List<Memory> = emptyList(),
    val sections: List<MemorySection> = emptyList(),
    val message: String? = null,
)

/**
 * The ViewModel of the Memories grid and viewer.
 *
 * @param getMemories Observes the saved memories, newest first.
 * @param deleteMemory Deletes a memory.
 * @param timeZone The time zone dates are shown in.
 */
class MemoriesViewModel(
    getMemories: GetMemoriesUseCase,
    private val deleteMemory: DeleteMemoryUseCase,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val message = MutableStateFlow<String?>(null)

    /** The state of the screens. */
    val uiState: StateFlow<MemoriesUiState> = combine(getMemories(), message) { memories, message ->
        MemoriesUiState(
            isLoading = false,
            memories = memories,
            sections = sectionsByMonth(memories, timeZone),
            message = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), MemoriesUiState())

    /** Formats the capture date of [memory], e.g. `Nov 16, 2026 at 14:03`. */
    fun formattedDate(memory: Memory): String = formatDateTime(memory.capturedAt, timeZone)

    /** Deletes [memory] and its files. */
    @Suppress("TooGenericExceptionCaught")
    fun delete(memory: Memory) {
        viewModelScope.launch {
            try {
                deleteMemory(memory.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                message.value = "Couldn't delete: ${e.message ?: e}"
                delay(MESSAGE_DURATION)
                message.value = null
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        val MESSAGE_DURATION = 2.5.seconds
    }
}
