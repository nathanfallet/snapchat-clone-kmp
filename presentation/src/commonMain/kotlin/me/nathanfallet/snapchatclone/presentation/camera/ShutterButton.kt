package me.nathanfallet.snapchatclone.presentation.camera

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.nathanfallet.snapchatclone.presentation.theme.Recording

/**
 * The shutter: a ring, tapped to take a photo or held to record a video, with the recording progress drawn around it.
 *
 * While it is held, sliding the finger up zooms in, like in Snapchat: [onHoldSlide] receives how far the finger
 * slid up, from `0` (where it started) to `1` ([slideDistance] above).
 *
 * @param recording The recording in progress, if any.
 * @param enabled Whether a capture can start.
 * @param slideDistance How far the finger slides up to reach the maximum zoom.
 * @param onTap Takes a photo.
 * @param onHoldStart Starts recording.
 * @param onHoldSlide The finger slid up while holding.
 * @param onHoldEnd Stops recording.
 * @param modifier The modifier of the button.
 */
@Composable
fun ShutterButton(
    recording: RecordingUiState?,
    enabled: Boolean,
    slideDistance: Dp,
    onTap: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldSlide: (Float) -> Unit,
    onHoldEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentEnabled by rememberUpdatedState(enabled)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnHoldStart by rememberUpdatedState(onHoldStart)
    val currentOnHoldSlide by rememberUpdatedState(onHoldSlide)
    val currentOnHoldEnd by rememberUpdatedState(onHoldEnd)
    val isRecording = recording != null
    val ringSize by animateDpAsState(if (isRecording) RECORDING_SIZE else IDLE_SIZE)
    val progress by animateFloatAsState(recording?.progress ?: 0f)

    Canvas(
        modifier = modifier.size(RECORDING_SIZE).pointerInput(Unit) {
            val slidePx = slideDistance.toPx()
            awaitEachGesture {
                val down = awaitFirstDown()
                if (!currentEnabled) return@awaitEachGesture
                val tap = withTimeoutOrNull(HOLD_DELAY_MILLIS) { waitForUpOrCancellation() }
                if (tap != null) {
                    tap.consume()
                    currentOnTap()
                    return@awaitEachGesture
                }
                currentOnHoldStart()
                var change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                while (change != null && change.pressed) {
                    currentOnHoldSlide(slideFraction(down.position.y - change.position.y, slidePx))
                    change.consume()
                    change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                }
                currentOnHoldEnd()
            }
        },
    ) {
        val ringWidth = RING_WIDTH.toPx()
        val diameter = ringSize.toPx() - ringWidth
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        if (isRecording) {
            drawCircle(Color.White.copy(alpha = 0.25f), radius = diameter / 2)
        }
        drawCircle(Color.White, radius = diameter / 2, style = Stroke(width = ringWidth))
        if (progress > 0f) {
            drawArc(
                color = Recording,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = ringWidth, cap = StrokeCap.Round),
            )
        }
    }
}

/** How far a finger that moved up by [distanceUp] pixels slid, between `0` and `1` of [slideDistance] pixels. */
fun slideFraction(distanceUp: Float, slideDistance: Float): Float =
    if (slideDistance <= 0f) 0f else (distanceUp / slideDistance).coerceIn(0f, 1f)

/** How long the shutter is held before a recording starts (a shorter press is a tap: a photo). */
private const val HOLD_DELAY_MILLIS = 300L
private val IDLE_SIZE = 80.dp
private val RECORDING_SIZE = 104.dp
private val RING_WIDTH = 6.dp
