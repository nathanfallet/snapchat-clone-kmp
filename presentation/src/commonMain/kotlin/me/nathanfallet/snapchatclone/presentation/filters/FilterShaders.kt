package me.nathanfallet.snapchatclone.presentation.filters

import me.nathanfallet.snapchatclone.domain.models.Filter

/**
 * The SkSL (Skia's shading language) source of each [Filter], compiled with `RuntimeEffect.makeForShader`.
 *
 * Every shader samples the image with `src.eval(xy)`, and receives the size of the drawn image in `resolution`, so
 * effects that depend on the position (vignette, pixel size) look the same on the preview and on the full-resolution
 * photo.
 */
internal object FilterShaders {
    private const val HEADER = """
        uniform shader src;
        uniform float2 resolution;
    """

    /** The SkSL source of the given [filter]. */
    fun sksl(filter: Filter): String = (HEADER + body(filter)).trimIndent()

    private fun body(filter: Filter): String = when (filter) {
        Filter.NORMAL -> NORMAL
        Filter.BLACK_AND_WHITE -> BLACK_AND_WHITE
        Filter.VINTAGE -> VINTAGE
        Filter.WARM -> WARM
        Filter.COOL -> COOL
        Filter.VIVID -> VIVID
        Filter.PIXELATE -> PIXELATE
        Filter.INVERT -> INVERT
    }

    private const val NORMAL = """
        half4 main(float2 xy) {
            return src.eval(xy);
        }
    """

    private const val BLACK_AND_WHITE = """
        half4 main(float2 xy) {
            half4 color = src.eval(xy);
            half gray = dot(color.rgb, half3(0.299, 0.587, 0.114));
            gray = saturate((gray - 0.5) * 1.25 + 0.5);
            return half4(gray, gray, gray, color.a);
        }
    """

    private const val VINTAGE = """
        half4 main(float2 xy) {
            half4 color = src.eval(xy);
            half3 sepia = half3(
                dot(color.rgb, half3(0.393, 0.769, 0.189)),
                dot(color.rgb, half3(0.349, 0.686, 0.168)),
                dot(color.rgb, half3(0.272, 0.534, 0.131))
            );
            half3 faded = mix(color.rgb, sepia, 0.85) * 0.88 + 0.07;
            float2 centered = xy / resolution - 0.5;
            half vignette = half(1.0 - smoothstep(0.3, 0.8, length(centered)));
            return half4(saturate(faded * mix(0.5, 1.0, vignette)), color.a);
        }
    """

    private const val WARM = """
        half4 main(float2 xy) {
            half4 color = src.eval(xy);
            half3 warm = color.rgb * half3(1.12, 1.03, 0.82) + half3(0.04, 0.02, 0.0);
            return half4(saturate(warm), color.a);
        }
    """

    private const val COOL = """
        half4 main(float2 xy) {
            half4 color = src.eval(xy);
            half3 cool = color.rgb * half3(0.85, 1.0, 1.15) + half3(0.0, 0.02, 0.06);
            return half4(saturate(cool), color.a);
        }
    """

    private const val VIVID = """
        half4 main(float2 xy) {
            half4 color = src.eval(xy);
            half gray = dot(color.rgb, half3(0.299, 0.587, 0.114));
            half3 saturated = mix(half3(gray), color.rgb, 1.6);
            return half4(saturate((saturated - 0.5) * 1.15 + 0.5), color.a);
        }
    """

    private const val PIXELATE = """
        half4 main(float2 xy) {
            float size = max(resolution.x, resolution.y) / 48.0;
            return src.eval((floor(xy / size) + 0.5) * size);
        }
    """

    private const val INVERT = """
        half4 main(float2 xy) {
            half4 color = src.eval(xy);
            return half4(1.0 - color.rgb, color.a);
        }
    """
}
