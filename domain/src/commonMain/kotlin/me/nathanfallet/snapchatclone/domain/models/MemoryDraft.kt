package me.nathanfallet.snapchatclone.domain.models

import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A captured snap, shown in the preview screen, that the user can save to the Memories or discard.
 *
 * @property type Whether this draft is a photo or a video.
 * @property media The media itself.
 * @property thumbnail A small encoded JPEG preview of the media, if any.
 * @property capturedAt When the media was captured.
 * @property filter The filter applied to the media.
 * @property location Where the media was captured, if known.
 * @property duration The duration of a video, `null` for photos.
 */
@Suppress("LongParameterList") // A plain model, like Memory (not a data class: it holds byte arrays).
class MemoryDraft(
    val type: MemoryType,
    val media: MediaSource,
    val thumbnail: ByteArray?,
    val capturedAt: Instant,
    val filter: Filter,
    val location: GeoPoint?,
    val duration: Duration?,
)
