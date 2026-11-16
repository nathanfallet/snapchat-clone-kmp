package me.nathanfallet.snapchatclone.presentation.memories

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.nathanfallet.snapchatclone.domain.models.Memory

/**
 * A group of memories captured the same month, in the Memories grid.
 *
 * @property title The month, e.g. `November 2026`.
 * @property memories The memories of this month, newest first.
 */
data class MemorySection(val title: String, val memories: List<Memory>)

/** Groups [memories] (newest first) by the month they were captured in, in [timeZone]. */
fun sectionsByMonth(memories: List<Memory>, timeZone: TimeZone): List<MemorySection> =
    memories
        .groupBy { it.capturedAt.toLocalDateTime(timeZone).let { date -> date.year to date.month } }
        .map { (_, monthMemories) ->
            MemorySection(title = formatMonth(monthMemories.first().capturedAt, timeZone), memories = monthMemories)
        }
