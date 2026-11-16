package me.nathanfallet.snapchatclone.presentation.camera

import kotlin.math.max
import kotlin.math.min

/**
 * The zoom a camera supports, and how gestures move within it.
 *
 * @property min The minimum zoom of the device (`CameraController.minZoom`).
 * @property max The maximum zoom of the device (`CameraController.maxZoom`).
 * @property neutral The zoom the camera started with, displayed as `1x`.
 */
data class ZoomRange(val min: Double, val max: Double, val neutral: Double) {
    /** The highest zoom gestures reach: digital zoom past [MAX_FACTOR] times the [neutral] zoom is just blurry. */
    val upperBound: Double get() = max(min, min(max, neutral * MAX_FACTOR))

    /** Clamps [zoom] to the range gestures can reach. */
    fun clamp(zoom: Double): Double = zoom.coerceIn(min, upperBound)

    /** The zoom after a pinch of the given [scale] (`> 1` when the fingers spread) from [current]. */
    fun pinch(current: Double, scale: Float): Double = clamp(current * scale)

    /**
     * The zoom while recording, when the finger holding the shutter slid up by [fraction] (`0` to `1`) of the
     * slide distance, from the zoom the recording [started] with: like Snapchat, sliding up to the top zooms in
     * to the [upperBound], sliding back down returns to [started].
     */
    fun slide(started: Double, fraction: Float): Double {
        val start = clamp(started)
        return clamp(start + (upperBound - start) * fraction.coerceIn(0f, 1f))
    }

    /** The zoom to display to the user for [zoom], relative to the [neutral] zoom (e.g. `2.0` for `2x`). */
    fun displayed(zoom: Double): Double = if (neutral > 0) zoom / neutral else zoom

    private companion object {
        const val MAX_FACTOR = 8.0
    }
}
