package com.skyd.anivu.ui.component.shape

import android.graphics.Matrix
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

val RoundedCornerStarShape: Shape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val baseWidth = 183.51f
        val baseHeight = 183.51f

        val path = Path().apply {
            relativeMoveTo(91.76f, 6.76f)
            relativeLineTo(0f, 0f)
            relativeCubicTo(23.16f, -15.72f, 54.85f, -2.6f, 60.1f, 24.9f)
            relativeLineTo(0f, 0f)
            relativeCubicTo(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            relativeCubicTo(27.49f, 5.26f, 40.62f, 36.95f, 24.9f, 60.1f)
            relativeLineTo(0f, 0f)
            relativeCubicTo(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            relativeCubicTo(15.72f, 23.16f, 2.6f, 54.85f, -24.9f, 60.1f)
            relativeLineTo(0f, 0f)
            relativeCubicTo(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            relativeCubicTo(-5.26f, 27.49f, -36.95f, 40.62f, -60.1f, 24.9f)
            relativeLineTo(0f, 0f)
            relativeCubicTo(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            relativeCubicTo(-23.16f, 15.72f, -54.85f, 2.6f, -60.1f, -24.9f)
            relativeLineTo(0f, 0f)
            relativeCubicTo(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            relativeCubicTo(-27.49f, -5.26f, -40.62f, -36.95f, -24.9f, -60.1f)
            relativeLineTo(0f, 0f)
            relativeCubicTo(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            relativeCubicTo(-15.72f, -23.16f, -2.6f, -54.85f, 24.9f, -60.1f)
            relativeLineTo(0f, 0f)
            relativeCubicTo(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            cubicTo(36.91f, 4.16f, 68.6f, -8.97f, 91.76f, 6.76f)
            relativeLineTo(0f, 0f)
            close()
        }

        return Outline.Generic(
            path
                .asAndroidPath()
                .apply {
                    transform(Matrix().apply {
                        setScale(size.width / baseWidth, size.height / baseHeight)
                    })
                }
                .asComposePath()
        )
    }
}