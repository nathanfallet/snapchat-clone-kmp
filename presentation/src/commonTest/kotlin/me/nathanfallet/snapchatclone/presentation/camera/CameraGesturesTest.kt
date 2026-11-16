package me.nathanfallet.snapchatclone.presentation.camera

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CameraGesturesTest {
    @Test
    fun shortDragsAreNotSwipes() {
        assertNull(classifySwipe(dx = 10f, dy = -20f, threshold = 50f))
    }

    @Test
    fun swipesFollowTheirMainAxis() {
        assertEquals(Swipe.LEFT, classifySwipe(dx = -120f, dy = 30f, threshold = 50f))
        assertEquals(Swipe.RIGHT, classifySwipe(dx = 120f, dy = -60f, threshold = 50f))
        assertEquals(Swipe.UP, classifySwipe(dx = 40f, dy = -200f, threshold = 50f))
        assertEquals(Swipe.DOWN, classifySwipe(dx = -40f, dy = 200f, threshold = 50f))
    }

    @Test
    fun shutterSlideIsAFractionOfTheSlideDistance() {
        assertEquals(0f, slideFraction(distanceUp = -50f, slideDistance = 100f))
        assertEquals(0.5f, slideFraction(distanceUp = 50f, slideDistance = 100f))
        assertEquals(1f, slideFraction(distanceUp = 500f, slideDistance = 100f))
        assertEquals(0f, slideFraction(distanceUp = 50f, slideDistance = 0f))
    }

    @Test
    fun zoomsAreFormattedWithOneDecimal() {
        assertEquals("2.0", 2.0.formatZoom())
        assertEquals("1.5", 1.46.formatZoom())
        assertEquals("10.3", 10.25.formatZoom())
    }
}
