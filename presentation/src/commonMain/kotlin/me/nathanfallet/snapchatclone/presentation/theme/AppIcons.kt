package me.nathanfallet.snapchatclone.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The icons of the app, drawn on a 24x24 grid. Most paths come from Google's Material Icons (Apache License 2.0).
 */
object AppIcons {
    /** The flash, on. */
    val FlashOn: ImageVector = icon("FlashOn", "M7,2v11h3v9l7,-12h-4l4,-8z")

    /** The flash, off. */
    val FlashOff: ImageVector = icon(
        "FlashOff",
        "M3.27,3L2,4.27l5,5V13h3v9l3.58,-6.14L17.73,20 19,18.73 3.27,3zM17,10h-4l4,-8H7v2.18l8.46,8.46L17,10z",
    )

    /** The night mode (a crescent moon). */
    val Night: ImageVector = icon(
        "Night",
        "M12.34,2.02C6.59,1.82 2,6.42 2,12c0,5.52 4.48,10 10,10c3.71,0 6.93,-2.02 8.66,-5.02" +
            "C13.15,16.73 8.57,8.55 12.34,2.02z",
    )

    /** Flips between the front and back cameras (two circular arrows). */
    val Flip: ImageVector = icon(
        "Flip",
        "M12,4a8,8 0,0 1,7.75 6H22l-3.5,4L15,10h2.65A6,6 0,0 0,6.34 8.2L4.9,6.8A8,8 0,0 1,12 4z" +
            "M12,20a8,8 0,0 1,-7.75 -6H2l3.5,-4L9,14H6.35a6,6 0,0 0,11.31 1.8l1.44,1.4A8,8 0,0 1,12 20z",
    )

    /** Closes a screen. */
    val Close: ImageVector = icon(
        "Close",
        "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z",
    )

    /** Saves to the Memories (an arrow down into a tray). */
    val Save: ImageVector = icon("Save", "M19,9h-4V3H9v6H5l7,7 7,-7zM5,18v2h14v-2H5z")

    /** Deletes (a trash can). */
    val Delete: ImageVector = icon(
        "Delete",
        "M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z",
    )

    /** The Memories (stacked photos). */
    val Memories: ImageVector = icon(
        "Memories",
        "M22,16V4c0,-1.1 -0.9,-2 -2,-2H8c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2z" +
            "M11,12l2.03,2.71L16,11l4,5H8l3,-4zM2,6v14c0,1.1 0.9,2 2,2h14v-2H4V6H2z",
    )

    /** Goes back down (a chevron). */
    val ChevronDown: ImageVector = icon("ChevronDown", "M16.59,8.59L12,13.17 7.41,8.59 6,10l6,6 6,-6z")

    /** Plays a video. */
    val Play: ImageVector = icon("Play", "M8,5v14l11,-7z")

    /** Opens a link. */
    val OpenLink: ImageVector = icon(
        "OpenLink",
        "M19,19H5V5h7V3H5c-1.11,0 -2,0.9 -2,2v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2v-7h-2v7z" +
            "M14,3v2h3.59l-9.83,9.83 1.41,1.41L19,6.41V10h2V3h-7z",
    )

    /** A location pin. */
    val Location: ImageVector = icon(
        "Location",
        "M12,2C8.13,2 5,5.13 5,9c0,5.25 7,13 7,13s7,-7.75 7,-13c0,-3.87 -3.13,-7 -7,-7z" +
            "M12,11.5c-1.38,0 -2.5,-1.12 -2.5,-2.5s1.12,-2.5 2.5,-2.5 2.5,1.12 2.5,2.5 -1.12,2.5 -2.5,2.5z",
    )

    private fun icon(name: String, pathData: String): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.White)).build()
}
