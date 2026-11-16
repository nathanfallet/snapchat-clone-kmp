package me.nathanfallet.snapchatclone.domain.models

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The rules of the camera, shared by the screens and their tests.
 */
object CaptureRules {
    /** The longest video a snap can be: recording stops by itself after it. */
    val maxVideoDuration: Duration = 60.seconds

    /**
     * The progress of a recording that lasted [elapsed], between `0` and `1`, to draw the ring around the shutter.
     */
    fun recordingProgress(elapsed: Duration): Float =
        (elapsed / maxVideoDuration).toFloat().coerceIn(0f, 1f)
}
