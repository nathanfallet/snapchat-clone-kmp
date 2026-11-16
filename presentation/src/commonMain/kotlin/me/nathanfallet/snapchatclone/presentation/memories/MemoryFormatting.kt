package me.nathanfallet.snapchatclone.presentation.memories

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import me.nathanfallet.snapchatclone.domain.models.GeoPoint
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Instant

/** Formats the month of [instant] in [timeZone], e.g. `November 2026`. */
fun formatMonth(instant: Instant, timeZone: TimeZone): String {
    val date = instant.toLocalDateTime(timeZone)
    return "${MONTHS[date.month.number - 1]} ${date.year}"
}

/** Formats [instant] in [timeZone], e.g. `Nov 16, 2026 at 14:03`. */
fun formatDateTime(instant: Instant, timeZone: TimeZone): String {
    val date: LocalDateTime = instant.toLocalDateTime(timeZone)
    val month = MONTHS[date.month.number - 1].take(SHORT_MONTH_LENGTH)
    return "$month ${date.day}, ${date.year} at ${date.hour.twoDigits()}:${date.minute.twoDigits()}"
}

/** Formats a [GeoPoint] in degrees with hemispheres, e.g. `48.8566° N, 2.3522° E`. */
fun formatLocation(point: GeoPoint): String {
    val latitude = "${abs(point.latitude).fourDecimals()}° ${if (point.latitude >= 0) "N" else "S"}"
    val longitude = "${abs(point.longitude).fourDecimals()}° ${if (point.longitude >= 0) "E" else "W"}"
    return "$latitude, $longitude"
}

/** Formats a video [duration], e.g. `0:07` or `1:00`. */
fun formatDuration(duration: Duration): String {
    val seconds = duration.inWholeMilliseconds.plus(HALF_SECOND_MILLIS).div(MILLIS_PER_SECOND)
    return "${seconds / SECONDS_PER_MINUTE}:${(seconds % SECONDS_PER_MINUTE).toInt().twoDigits()}"
}

private fun Int.twoDigits() = toString().padStart(2, '0')

private fun Double.fourDecimals(): String {
    val scaled = (this * DECIMALS_FACTOR).roundToLong()
    val decimals = (scaled % DECIMALS_FACTOR.toLong()).toString().padStart(DECIMALS, '0')
    return "${scaled / DECIMALS_FACTOR.toLong()}.$decimals"
}

private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)
private const val SHORT_MONTH_LENGTH = 3
private const val DECIMALS = 4
private const val DECIMALS_FACTOR = 10_000.0
private const val HALF_SECOND_MILLIS = 500L
private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
