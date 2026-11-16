package me.nathanfallet.snapchatclone.domain.models

/**
 * A position on Earth, where a [Memory] was captured.
 *
 * @property latitude The latitude, in degrees, between -90 and 90.
 * @property longitude The longitude, in degrees, between -180 and 180.
 */
data class GeoPoint(val latitude: Double, val longitude: Double) {
    init {
        require(isValid(latitude, longitude)) { "Invalid coordinates: $latitude, $longitude" }
    }

    /** Creates [GeoPoint]s from untrusted coordinates. */
    companion object {
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0

        /** Whether [latitude] and [longitude] are valid coordinates. */
        fun isValid(latitude: Double, longitude: Double): Boolean =
            latitude in -MAX_LATITUDE..MAX_LATITUDE && longitude in -MAX_LONGITUDE..MAX_LONGITUDE

        /** A [GeoPoint] at the given coordinates, or `null` if they are missing or invalid. */
        fun orNull(latitude: Double?, longitude: Double?): GeoPoint? {
            if (latitude == null || longitude == null || !isValid(latitude, longitude)) return null
            return GeoPoint(latitude, longitude)
        }
    }
}
