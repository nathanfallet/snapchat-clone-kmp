package me.nathanfallet.snapchatclone.presentation.memories

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.GeoPoint
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class MemoryFormattingTest {
    private val utc = TimeZone.UTC

    @Test
    fun formatsTheDateAndTime() {
        assertEquals("Nov 16, 2026 at 14:03", formatDateTime(at(2026, 11, 16, 14, 3), utc))
        assertEquals("Jan 2, 2027 at 09:05", formatDateTime(at(2027, 1, 2, 9, 5), utc))
    }

    @Test
    fun formatsTheDateInTheGivenTimeZone() {
        val instant = at(2026, 11, 16, 23, 30)
        assertEquals("Nov 17, 2026 at 00:30", formatDateTime(instant, TimeZone.of("Europe/Paris")))
    }

    @Test
    fun formatsTheMonth() {
        assertEquals("November 2026", formatMonth(at(2026, 11, 16, 14, 3), utc))
    }

    @Test
    fun formatsTheLocationWithItsHemispheres() {
        assertEquals("48.8566° N, 2.3522° E", formatLocation(GeoPoint(48.8566, 2.3522)))
        assertEquals("33.8688° S, 151.2093° E", formatLocation(GeoPoint(-33.8688, 151.2093)))
        assertEquals("40.7128° N, 74.0060° W", formatLocation(GeoPoint(40.7128, -74.006)))
    }

    @Test
    fun formatsTheDurationRoundedToTheSecond() {
        assertEquals("0:07", formatDuration(7.2.seconds))
        assertEquals("0:08", formatDuration(7_600.milliseconds))
        assertEquals("1:00", formatDuration(60.seconds))
        assertEquals("0:00", formatDuration(0.seconds))
    }

    @Test
    fun groupsTheMemoriesByMonthKeepingTheirOrder() {
        val memories = listOf(
            memory("c", at(2026, 11, 16, 14, 3)),
            memory("b", at(2026, 11, 1, 8, 0)),
            memory("a", at(2026, 10, 31, 23, 59)),
            memory("z", at(2025, 11, 20, 12, 0)),
        )
        val sections = sectionsByMonth(memories, utc)
        assertEquals(listOf("November 2026", "October 2026", "November 2025"), sections.map { it.title })
        val ids = sections.map { section -> section.memories.map { it.id } }
        assertEquals(listOf(listOf("c", "b"), listOf("a"), listOf("z")), ids)
    }

    @Test
    fun groupsByMonthInTheGivenTimeZone() {
        // 31 October 23:30 in UTC is already November in Paris.
        val memories = listOf(memory("a", at(2026, 10, 31, 23, 30)))
        assertEquals("November 2026", sectionsByMonth(memories, TimeZone.of("Europe/Paris")).single().title)
    }

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Instant =
        LocalDateTime(year, month, day, hour, minute).toInstant(utc)

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
