package me.nathanfallet.snapchatclone.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GeoPointTest {
    @Test
    fun acceptsValidCoordinates() {
        val point = GeoPoint(48.8566, 2.3522)
        assertEquals(48.8566, point.latitude)
        assertEquals(2.3522, point.longitude)
    }

    @Test
    fun rejectsOutOfRangeCoordinates() {
        assertFailsWith<IllegalArgumentException> { GeoPoint(91.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { GeoPoint(0.0, -181.0) }
        assertFailsWith<IllegalArgumentException> { GeoPoint(Double.NaN, 0.0) }
    }

    @Test
    fun orNullAcceptsOnlyCompleteAndValidCoordinates() {
        assertEquals(GeoPoint(-33.86, 151.21), GeoPoint.orNull(-33.86, 151.21))
        assertNull(GeoPoint.orNull(null, 151.21))
        assertNull(GeoPoint.orNull(-33.86, null))
        assertNull(GeoPoint.orNull(-100.0, 0.0))
    }
}
