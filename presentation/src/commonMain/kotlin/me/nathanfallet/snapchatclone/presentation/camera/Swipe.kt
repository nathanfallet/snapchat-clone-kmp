package me.nathanfallet.snapchatclone.presentation.camera

import kotlin.math.abs

/** A swipe on the camera preview. */
enum class Swipe {
    /** The finger moved to the left: next filter. */
    LEFT,

    /** The finger moved to the right: previous filter. */
    RIGHT,

    /** The finger moved up: opens the Memories. */
    UP,

    /** The finger moved down. */
    DOWN,
}

/**
 * Classifies a finished drag of ([dx], [dy]) pixels as a [Swipe] along its main axis, or `null` if it is shorter
 * than [threshold] pixels.
 */
fun classifySwipe(dx: Float, dy: Float, threshold: Float): Swipe? = when {
    abs(dx) >= abs(dy) && abs(dx) >= threshold -> if (dx < 0) Swipe.LEFT else Swipe.RIGHT
    abs(dy) > abs(dx) && abs(dy) >= threshold -> if (dy < 0) Swipe.UP else Swipe.DOWN
    else -> null
}
