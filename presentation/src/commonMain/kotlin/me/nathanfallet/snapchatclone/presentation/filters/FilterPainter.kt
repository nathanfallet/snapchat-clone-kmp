package me.nathanfallet.snapchatclone.presentation.filters

import me.nathanfallet.snapchatclone.domain.models.Filter
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.SamplingMode

/**
 * Draws images with a [Filter], with Skia: the camera Frames of the preview (on the GPU) and the captured photos
 * (on the CPU) share it, so a photo looks like its preview.
 *
 * Not thread-safe: use one painter per thread (the Frame output's thread, the photo processing thread). Shaders are
 * compiled once per filter, the first time it is drawn.
 */
class FilterPainter {
    private val effects = mutableMapOf<Filter, RuntimeEffect>()
    private val paint = Paint()

    /**
     * Draws [image] with [filter] on [canvas].
     *
     * @param canvas The canvas to draw on.
     * @param image The image to draw.
     * @param filter The filter to apply.
     * @param bounds The rectangle to fill, at the origin (the size of the image by default).
     * @param imageMatrix Maps the image to [bounds], e.g. to rotate a photo to its EXIF orientation. `null` draws
     * the image as is.
     */
    fun draw(
        canvas: Canvas,
        image: Image,
        filter: Filter,
        bounds: Rect = Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
        imageMatrix: Matrix33? = null,
    ) {
        val imageShader = image.makeShader(FilterTileMode.CLAMP, FilterTileMode.CLAMP, SamplingMode.LINEAR, imageMatrix)
        val builder = RuntimeShaderBuilder(effect(filter))
        try {
            builder.child("src", imageShader)
            builder.uniform("resolution", bounds.width, bounds.height)
            val shader = builder.makeShader()
            paint.shader = shader
            // Skia ignores the shader of a paint given to drawImage: draw a rect covering the image instead.
            canvas.drawRect(bounds, paint)
            paint.shader = null
            shader.close()
        } finally {
            builder.close()
            imageShader.close()
        }
    }

    private fun effect(filter: Filter): RuntimeEffect =
        effects.getOrPut(filter) { RuntimeEffect.makeForShader(FilterShaders.sksl(filter)) }
}
