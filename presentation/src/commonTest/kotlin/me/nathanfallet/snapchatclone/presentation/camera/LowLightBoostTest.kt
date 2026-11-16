package me.nathanfallet.snapchatclone.presentation.camera

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LowLightBoostTest {
    @Test
    fun supportedDevicesFollowTheNightMode() {
        assertEquals(true, lowLightBoostSetting(isNightModeEnabled = true, isSupported = true))
        assertEquals(false, lowLightBoostSetting(isNightModeEnabled = false, isSupported = true))
    }

    @Test
    fun unsupportedOrUnknownDevicesAreLeftAlone() {
        // The Pixel 7a front camera threw "Low-light boost is not supported!" when given `false`.
        assertNull(lowLightBoostSetting(isNightModeEnabled = false, isSupported = false))
        assertNull(lowLightBoostSetting(isNightModeEnabled = true, isSupported = false))
        assertNull(lowLightBoostSetting(isNightModeEnabled = false, isSupported = null))
    }
}
