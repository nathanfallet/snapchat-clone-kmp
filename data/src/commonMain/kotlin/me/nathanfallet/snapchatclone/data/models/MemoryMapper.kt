package me.nathanfallet.snapchatclone.data.models

import me.nathanfallet.snapchatclone.domain.models.Filter
import me.nathanfallet.snapchatclone.domain.models.GeoPoint
import me.nathanfallet.snapchatclone.domain.models.Memory
import me.nathanfallet.snapchatclone.domain.models.MemoryType
import okio.Path
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/**
 * Converts this entity to a domain [Memory], resolving its files in [directory]. Returns `null` if the entity is
 * not readable (e.g. an unknown type written by a newer version), so one bad entry doesn't hide all the others.
 */
fun MemoryEntity.toMemory(directory: Path): Memory? {
    val type = MemoryType.entries.firstOrNull { it.name == type } ?: return null
    val location = GeoPoint.orNull(latitude, longitude)
    return Memory(
        id = id,
        type = type,
        capturedAt = Instant.fromEpochMilliseconds(capturedAt),
        mediaPath = (directory / mediaFile).toString(),
        thumbnailPath = thumbnailFile?.let { (directory / it).toString() },
        filter = Filter.fromName(filter),
        location = location,
        duration = durationMillis?.milliseconds,
    )
}

