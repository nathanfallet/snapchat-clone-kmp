package me.nathanfallet.snapchatclone.domain.models

/**
 * The kind of media a [Memory] holds.
 */
enum class MemoryType {
    /** A still photo (a JPEG file). */
    PHOTO,

    /** A video recording (an MP4 or MOV file). */
    VIDEO,
}
