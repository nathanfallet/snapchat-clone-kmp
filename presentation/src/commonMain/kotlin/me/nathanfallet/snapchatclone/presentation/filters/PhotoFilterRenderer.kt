package me.nathanfallet.snapchatclone.presentation.filters

import me.nathanfallet.snapchatclone.domain.models.Filter

/**
 * Applies a [Filter] to a captured photo, at full resolution.
 */
fun interface PhotoFilterRenderer {
    /**
     * Decodes the [encoded] photo, applies its EXIF orientation and the [filter], and returns it encoded as JPEG.
     */
    suspend fun render(encoded: ByteArray, filter: Filter): ByteArray
}
