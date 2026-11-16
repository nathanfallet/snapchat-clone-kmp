package me.nathanfallet.snapchatclone.domain.models

/**
 * A color filter the user swipes through on the camera, applied live to the preview and to captured photos.
 *
 * The order of the entries is the order of the swipe carousel. How a filter is drawn (its shader) is a presentation
 * concern: the domain only knows which filter was chosen.
 *
 * @property displayName The name shown to the user when the filter is selected.
 */
enum class Filter(val displayName: String) {
    /** No filter: the camera image as is. */
    NORMAL("Normal"),

    /** Black and white, with a little more contrast. */
    BLACK_AND_WHITE("B&W"),

    /** Sepia tones, faded blacks and a vignette. */
    VINTAGE("Vintage"),

    /** Golden, sunset tones. */
    WARM("Golden"),

    /** Cold, blue tones. */
    COOL("Arctic"),

    /** Boosted saturation and contrast. */
    VIVID("Vivid"),

    /** Big square pixels, like an old video game. */
    PIXELATE("Pixels"),

    /** Inverted colors, like a film negative. */
    INVERT("Negative"),
    ;

    /** The filter after this one in the carousel (swiping left), wrapping around after the last one. */
    fun next(): Filter = entries[(ordinal + 1) % entries.size]

    /** The filter before this one in the carousel (swiping right), wrapping around before the first one. */
    fun previous(): Filter = entries[(ordinal - 1 + entries.size) % entries.size]

    /** Looks filters up. */
    companion object {
        /** Finds a filter by its [name], or returns [NORMAL] if it is unknown (e.g. removed in a later version). */
        fun fromName(name: String): Filter = entries.firstOrNull { it.name == name } ?: NORMAL
    }
}
