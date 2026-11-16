package me.nathanfallet.snapchatclone.domain.models

/**
 * A QR code (or barcode) scanned by the camera.
 *
 * @property value The decoded text of the code.
 */
data class ScannedCode(val value: String) {
    /** The web link this code contains, or `null` if it is not an `http(s)` URL. */
    val link: String?
        get() {
            val trimmed = value.trim()
            val isWebLink = LINK_PREFIXES.any { trimmed.startsWith(it, ignoreCase = true) } &&
                trimmed.none { it.isWhitespace() } &&
                LINK_PREFIXES.none { trimmed.equals(it, ignoreCase = true) }
            return trimmed.takeIf { isWebLink }
        }

    private companion object {
        val LINK_PREFIXES = listOf("http://", "https://")
    }
}
