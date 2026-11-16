package me.nathanfallet.snapchatclone.presentation.camera

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp

/**
 * The gestures of the camera preview, recognized together like in Snapchat: a tap, a double tap, a long press, a
 * pinch, and swipes.
 *
 * Double taps are recognized by waiting a little after a tap, so a single tap is reported with that delay.
 *
 * @param onTap A single tap at the given position (focus).
 * @param onDoubleTap A double tap (flip the camera).
 * @param onLongPress A long press at the given position (scan a code).
 * @param onPinch A pinch, with the scale since the last event (zoom).
 * @param onSwipe A swipe, once the finger is lifted (filters, Memories).
 */
fun Modifier.cameraGestures(
    onTap: (Offset) -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: (Offset) -> Unit,
    onPinch: (Float) -> Unit,
    onSwipe: (Swipe) -> Unit,
): Modifier = pointerInput(Unit) {
    val swipeThreshold = SWIPE_THRESHOLD.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val start = awaitGestureStart(down.id, viewConfiguration.longPressTimeoutMillis, viewConfiguration.touchSlop)
        when (start) {
            GestureStart.Tap -> {
                // Wait for a second tap before reporting a single one.
                val secondDown = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
                    awaitFirstDown(requireUnconsumed = false)
                }
                if (secondDown == null) {
                    onTap(down.position)
                } else {
                    awaitAllUp()
                    onDoubleTap()
                }
            }

            GestureStart.LongPress -> {
                onLongPress(down.position)
                awaitAllUp()
            }

            GestureStart.Pinch -> trackPinch(onPinch)

            is GestureStart.Drag -> {
                val total = trackDrag(down.id, start.initial, onPinch)
                if (total != null) classifySwipe(total.x, total.y, swipeThreshold)?.let(onSwipe)
            }

            GestureStart.Cancelled -> Unit
        }
    }
}

/** How a gesture started. */
private sealed interface GestureStart {
    data object Tap : GestureStart
    data object LongPress : GestureStart
    data object Pinch : GestureStart
    data class Drag(val initial: Offset) : GestureStart
    data object Cancelled : GestureStart
}

/** Waits until the gesture of the pointer [id] is identified. */
private suspend fun AwaitPointerEventScope.awaitGestureStart(
    id: PointerId,
    longPressTimeout: Long,
    touchSlop: Float,
): GestureStart {
    var total = Offset.Zero
    return withTimeoutOrNull(longPressTimeout) {
        var start: GestureStart? = null
        while (start == null) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == id }
            start = when {
                event.pressedCount() >= 2 -> GestureStart.Pinch
                change == null -> GestureStart.Cancelled
                !change.pressed -> GestureStart.Tap
                else -> {
                    total += change.positionChange()
                    if (total.getDistance() > touchSlop) GestureStart.Drag(total) else null
                }
            }
        }
        start
    } ?: GestureStart.LongPress
}

/** Follows a drag until the finger is lifted, and returns its total offset (or `null` if it became a pinch). */
private suspend fun AwaitPointerEventScope.trackDrag(
    id: PointerId,
    initial: Offset,
    onPinch: (Float) -> Unit,
): Offset? {
    var total: Offset? = initial
    var isDragging = true
    while (isDragging) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == id }
        when {
            event.pressedCount() >= 2 -> {
                trackPinch(onPinch)
                total = null
                isDragging = false
            }

            change == null || !change.pressed -> isDragging = false

            else -> {
                total = total?.plus(change.positionChange())
                change.consume()
            }
        }
    }
    return total
}

/** Reports the zoom of a pinch until every finger is lifted. */
private suspend fun AwaitPointerEventScope.trackPinch(onPinch: (Float) -> Unit) {
    while (true) {
        val event = awaitPointerEvent()
        if (event.pressedCount() == 0) return
        val zoom = event.calculateZoom()
        if (zoom != 1f) onPinch(zoom)
        event.changes.forEach { it.consume() }
    }
}

/** Waits until every finger is lifted. */
private suspend fun AwaitPointerEventScope.awaitAllUp() {
    while (awaitPointerEvent().pressedCount() > 0) Unit
}

private fun PointerEvent.pressedCount() = changes.count { it.pressed }

/** The distance a finger travels for a drag to count as a swipe. */
private val SWIPE_THRESHOLD = 48.dp
