package me.nathanfallet.snapchatclone.domain.models

import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A photo or a video saved on the device, in the Memories gallery.
 *
 * @property id The unique identifier of this memory.
 * @property type Whether this memory is a photo or a video.
 * @property capturedAt When the media was captured.
 * @property mediaPath The absolute path of the media file.
 * @property thumbnailPath The absolute path of a small JPEG preview of the media, if any (videos have one, photos
 * are displayed from their [mediaPath]).
 * @property filter The filter the photo was captured with. Videos are recorded without filter (see the README).
 * @property location Where the media was captured, if the location was available.
 * @property duration The duration of a video, `null` for photos.
 */
data class Memory(
    val id: String,
    val type: MemoryType,
    val capturedAt: Instant,
    val mediaPath: String,
    val thumbnailPath: String?,
    val filter: Filter,
    val location: GeoPoint?,
    val duration: Duration?,
)
