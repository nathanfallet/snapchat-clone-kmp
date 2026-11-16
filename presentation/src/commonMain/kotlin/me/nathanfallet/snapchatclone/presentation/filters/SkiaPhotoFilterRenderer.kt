package me.nathanfallet.snapchatclone.presentation.filters

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.nathanfallet.snapchatclone.domain.models.Filter
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.EncodedOrigin
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

/**
 * Renders filtered photos with Skia (Skiko), on the CPU: the photo is decoded, drawn upright with the same SkSL shader
 * as the preview ([FilterPainter]), and encoded as JPEG.
 *
 * The orientation comes from the photo's EXIF metadata, where KMP Vision Camera writes the rotation and the
 * mirroring of the front camera (`Photo.saveToTemporaryFile`), on both platforms.
 *
 * @param dispatcher The dispatcher the rendering runs on (a background one: it takes a moment on a 12 MP photo).
 */
class SkiaPhotoFilterRenderer(private val dispatcher: CoroutineDispatcher) : PhotoFilterRenderer {
    override suspend fun render(encoded: ByteArray, filter: Filter): ByteArray = withContext(dispatcher) {
        val data = Data.makeFromBytes(encoded)
        val codec = Codec.makeFromData(data)
        val bitmap = Bitmap()
        try {
            bitmap.allocPixels(codec.imageInfo)
            codec.readPixels(bitmap)
            val image = Image.makeFromBitmap(bitmap)
            try {
                renderUpright(image, codec.encodedOrigin, filter)
            } finally {
                image.close()
            }
        } finally {
            bitmap.close()
            codec.close()
            data.close()
        }
    }

    private fun renderUpright(image: Image, origin: EncodedOrigin, filter: Filter): ByteArray {
        val (width, height) = uprightSize(origin, image.width, image.height)
        val surface = Surface.makeRasterN32Premul(width, height)
        try {
            FilterPainter().draw(
                canvas = surface.canvas,
                image = image,
                filter = filter,
                bounds = Rect.makeWH(width.toFloat(), height.toFloat()),
                imageMatrix = origin.toMatrix(width, height),
            )
            val snapshot = surface.makeImageSnapshot()
            try {
                val jpeg = checkNotNull(snapshot.encodeToData(EncodedImageFormat.JPEG, JPEG_QUALITY)) {
                    "Couldn't encode the photo"
                }
                try {
                    return jpeg.bytes
                } finally {
                    jpeg.close()
                }
            } finally {
                snapshot.close()
            }
        } finally {
            surface.close()
        }
    }

    internal companion object {
        /** The JPEG quality of filtered photos. */
        const val JPEG_QUALITY = 92

        /** The size of an image of [width] by [height] pixels once its EXIF [origin] is applied. */
        fun uprightSize(origin: EncodedOrigin, width: Int, height: Int): Pair<Int, Int> =
            if (origin.swapsWidthHeight()) height to width else width to height
    }
}
