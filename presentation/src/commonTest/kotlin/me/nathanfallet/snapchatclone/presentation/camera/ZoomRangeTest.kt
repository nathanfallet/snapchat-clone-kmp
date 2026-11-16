package me.nathanfallet.snapchatclone.presentation.camera

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoomRangeTest {
    private val range = ZoomRange(min = 1.0, max = 100.0, neutral = 2.0)

    @Test
    fun gesturesStopAtEightTimesTheNeutralZoom() {
        assertEquals(16.0, range.upperBound)
        assertEquals(4.0, ZoomRange(min = 1.0, max = 4.0, neutral = 2.0).upperBound)
    }

    @Test
    fun pinchMultipliesTheZoomWithinTheRange() {
        assertEquals(4.0, range.pinch(2.0, 2f))
        assertEquals(1.0, range.pinch(2.0, 0.1f))
        assertEquals(16.0, range.pinch(10.0, 3f))
    }

    @Test
    fun slidingUpZoomsFromTheStartToTheUpperBound() {
        assertEquals(2.0, range.slide(2.0, 0f))
        assertEquals(9.0, range.slide(2.0, 0.5f))
        assertEquals(16.0, range.slide(2.0, 1f))
        assertEquals(16.0, range.slide(2.0, 3f))
        assertEquals(2.0, range.slide(2.0, -1f))
    }

    @Test
    fun displayedZoomIsRelativeToTheNeutralZoom() {
        assertEquals(1.0, range.displayed(2.0))
        assertEquals(0.5, range.displayed(1.0))
    }
}
