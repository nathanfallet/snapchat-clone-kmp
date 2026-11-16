package me.nathanfallet.snapchatclone.presentation.camera

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.math.roundToInt

/**
 * Encodes a small JPEG of [image], [THUMBNAIL_WIDTH] pixels wide at most, with Skia. Returns `null` if it can't be
 * encoded.
 */
internal fun encodeThumbnail(image: Image): ByteArray? {
    if (image.width <= 0 || image.height <= 0) return null
    val scale = minOf(1f, THUMBNAIL_WIDTH.toFloat() / image.width)
    val width = (image.width * scale).roundToInt().coerceAtLeast(1)
    val height = (image.height * scale).roundToInt().coerceAtLeast(1)
    val surface = Surface.makeRasterN32Premul(width, height)
    try {
        surface.canvas.drawImageRect(image, Rect.makeWH(width.toFloat(), height.toFloat()))
        val snapshot = surface.makeImageSnapshot()
        try {
            return snapshot.encodeToData(EncodedImageFormat.JPEG, THUMBNAIL_QUALITY)?.bytes
        } finally {
            snapshot.close()
        }
    } finally {
        surface.close()
    }
}

private const val THUMBNAIL_WIDTH = 360
private const val THUMBNAIL_QUALITY = 80
