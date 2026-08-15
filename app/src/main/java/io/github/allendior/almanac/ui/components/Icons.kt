package io.github.allendior.almanac.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Lucide icons (https://lucide.dev, ISC licence), transcribed as stroked vectors.
 * The system draws with strokes, never fills, so every glyph here is outline only.
 */
private fun lucide(name: String, vararg d: String, stroke: Float = 1.7f): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        d.forEach { path ->
            addPath(
                pathData = addPathNodes(path),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = stroke,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

object Lucide {

    val Camera = lucide(
        "camera",
        "M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z",
        "M12 16a3 3 0 1 0 0-6 3 3 0 0 0 0 6z",
    )

    val Rows = lucide(
        "rows",
        "M4 3h16a1 1 0 0 1 1 1v16a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z",
        "M3 9h18",
        "M3 15h18",
    )

    val Calendar = lucide(
        "calendar",
        "M8 2v4",
        "M16 2v4",
        "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
        "M3 10h18",
    )

    val Columns2 = lucide(
        "columns-2",
        "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z",
        "M12 3v18",
    )

    val Archive = lucide(
        "archive",
        "M20 9v11a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V9",
        "M3 3h18a1 1 0 0 1 1 1v4a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z",
        "M10 13h4",
    )

    val ChevronLeft = lucide("chevron-left", "m15 18-6-6 6-6")
    val ChevronRight = lucide("chevron-right", "m9 18 6-6-6-6")
    val ChevronDown = lucide("chevron-down", "m6 9 6 6 6-6")

    val ArrowRight = lucide("arrow-right", "M5 12h14", "m12 5 7 7-7 7")

    val ArrowDownToLine = lucide("arrow-down-to-line", "M12 17V3", "m6 11 6 6 6-6", "M19 21H5")
    val ArrowUpToLine = lucide("arrow-up-to-line", "M12 7v14", "m18 13-6-6-6 6", "M5 3h14")

    val Lock = lucide(
        "lock",
        "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z",
        "M7 11V7a5 5 0 0 1 10 0v4",
    )

    val RefreshFlip = lucide(
        "flip",
        "M21 8a9 9 0 0 0-15.5-3.5L3 7",
        "M3 3v4h4",
        "M3 16a9 9 0 0 0 15.5 3.5L21 17",
        "M21 21v-4h-4",
    )

    /**
     * The placeholder mark for a portrait that does not exist yet. Deliberately a
     * generic silhouette: the app performs no face detection of any kind.
     */
    val PortraitMark = lucide(
        "portrait-mark",
        "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z",
        "M4.5 20a7.5 7.5 0 0 1 15 0",
        stroke = 1.4f,
    )
}
