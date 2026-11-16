package me.nathanfallet.snapchatclone.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScannedCodeTest {
    @Test
    fun webLinksAreLinks() {
        assertEquals("https://example.com/a?b=c", ScannedCode("https://example.com/a?b=c").link)
        assertEquals("HTTP://EXAMPLE.COM", ScannedCode("  HTTP://EXAMPLE.COM\n").link)
    }

    @Test
    fun otherValuesAreNotLinks() {
        assertNull(ScannedCode("Hello world").link)
        assertNull(ScannedCode("WIFI:S:home;T:WPA;P:secret;;").link)
        assertNull(ScannedCode("https://").link)
        assertNull(ScannedCode("https://example.com and more text").link)
        assertNull(ScannedCode("javascript:alert(1)").link)
    }
}
