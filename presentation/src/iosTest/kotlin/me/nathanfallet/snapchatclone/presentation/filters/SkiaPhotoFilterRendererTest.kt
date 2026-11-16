package me.nathanfallet.snapchatclone.presentation.filters

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import me.nathanfallet.snapchatclone.domain.models.Filter
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Color
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Renders real photos with Skia (on the iOS Simulator in CI): the filters, and the EXIF orientation KMP Vision Camera
 * writes in the photos (rotation, and mirroring for the front camera).
 */
class SkiaPhotoFilterRendererTest {
    private val renderer = SkiaPhotoFilterRenderer(Dispatchers.Default)

    @Test
    fun normalKeepsThePhoto() = runTest {
        val output = decode(renderer.render(redBlueJpeg(), Filter.NORMAL))
        assertEquals(WIDTH to HEIGHT, output.width to output.height)
        assertColor(RED, output.getColor(WIDTH / 4, HEIGHT / 2))
        assertColor(BLUE, output.getColor(WIDTH * 3 / 4, HEIGHT / 2))
    }

    @Test
    fun invertInvertsTheColors() = runTest {
        val output = decode(renderer.render(redBlueJpeg(), Filter.INVERT))
        assertColor(CYAN, output.getColor(WIDTH / 4, HEIGHT / 2))
        assertColor(YELLOW, output.getColor(WIDTH * 3 / 4, HEIGHT / 2))
    }

    @Test
    fun blackAndWhiteRemovesTheColors() = runTest {
        val color = decode(renderer.render(redBlueJpeg(), Filter.BLACK_AND_WHITE)).getColor(WIDTH / 4, HEIGHT / 2)
        assertTrue(maxOf(red(color), green(color), blue(color)) - minOf(red(color), green(color), blue(color)) < 16)
    }

    @Test
    fun aRotatedPhotoIsRenderedUpright() = runTest {
        // EXIF 6: the camera buffer must be rotated 90° clockwise to be upright (a portrait photo on a phone).
        val output = decode(renderer.render(withExifOrientation(redBlueJpeg(), 6), Filter.NORMAL))
        assertEquals(HEIGHT to WIDTH, output.width to output.height)
        // The left of the buffer (red) is now at the top.
        assertColor(RED, output.getColor(HEIGHT / 2, WIDTH / 4))
        assertColor(BLUE, output.getColor(HEIGHT / 2, WIDTH * 3 / 4))
    }

    @Test
    fun aMirroredPhotoIsRenderedMirrored() = runTest {
        // EXIF 2: mirrored horizontally, like a selfie.
        val output = decode(renderer.render(withExifOrientation(redBlueJpeg(), 2), Filter.NORMAL))
        assertEquals(WIDTH to HEIGHT, output.width to output.height)
        assertColor(BLUE, output.getColor(WIDTH / 4, HEIGHT / 2))
        assertColor(RED, output.getColor(WIDTH * 3 / 4, HEIGHT / 2))
    }

    /** A JPEG whose left half is red and right half is blue. */
    private fun redBlueJpeg(): ByteArray {
        val surface = Surface.makeRasterN32Premul(WIDTH, HEIGHT)
        val half = Rect.makeWH(WIDTH / 2f, HEIGHT.toFloat())
        surface.canvas.drawRect(half, Paint().apply { color = RED })
        surface.canvas.drawRect(half.offset(WIDTH / 2f, 0f), Paint().apply { color = BLUE })
        return surface.makeImageSnapshot().encodeToData(EncodedImageFormat.JPEG, 100)!!.bytes
    }

    /** Inserts an EXIF segment with the given [orientation] tag after the JPEG's start of image marker. */
    private fun withExifOrientation(jpeg: ByteArray, orientation: Int): ByteArray {
        val tiff = byteArrayOf(
            'M'.code.toByte(), 'M'.code.toByte(), 0, 42, 0, 0, 0, 8, // Big endian TIFF header, first IFD at 8
            0, 1, // One entry
            0x01, 0x12, 0, 3, 0, 0, 0, 1, 0, orientation.toByte(), 0, 0, // Orientation (0x0112), SHORT, 1 value
            0, 0, 0, 0, // No next IFD
        )
        val payload = "Exif".encodeToByteArray() + byteArrayOf(0, 0) + tiff
        val length = payload.size + 2
        val segment = byteArrayOf(0xFF.toByte(), 0xE1.toByte(), (length shr 8).toByte(), (length and 0xFF).toByte())
        return jpeg.copyOfRange(0, 2) + segment + payload + jpeg.copyOfRange(2, jpeg.size)
    }

    private fun decode(jpeg: ByteArray): Bitmap {
        val codec = Codec.makeFromData(Data.makeFromBytes(jpeg))
        return Bitmap().apply {
            allocPixels(codec.imageInfo)
            codec.readPixels(this)
        }
    }

    private fun assertColor(expected: Int, actual: Int) {
        val distance = maxOf(
            kotlin.math.abs(red(expected) - red(actual)),
            kotlin.math.abs(green(expected) - green(actual)),
            kotlin.math.abs(blue(expected) - blue(actual)),
        )
        val message = "Expected ${expected.toUInt().toString(16)}, got ${actual.toUInt().toString(16)}"
        assertTrue(distance < TOLERANCE, message)
    }

    private fun red(color: Int) = (color shr 16) and 0xFF
    private fun green(color: Int) = (color shr 8) and 0xFF
    private fun blue(color: Int) = color and 0xFF

    private companion object {
        const val WIDTH = 64
        const val HEIGHT = 32
        const val TOLERANCE = 48
        val RED = Color.makeRGB(255, 0, 0)
        val BLUE = Color.makeRGB(0, 0, 255)
        val CYAN = Color.makeRGB(0, 255, 255)
        val YELLOW = Color.makeRGB(255, 255, 0)
    }
}
