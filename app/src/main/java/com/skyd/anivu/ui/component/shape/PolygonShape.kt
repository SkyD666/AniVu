package com.skyd.anivu.ui.component.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


/**
 * Shape describing Polygons
 *
 * Note: The shape draws within the minimum of provided width and height so can't be used to create stretched shape.
 *
 * @param sides number of sides.
 * @param rotation value between 0 - 360
 */
class PolygonShape(sides: Int, private val rotation: Float = 0f) : Shape {
    private val stepCount = 2 * PI / sides
    private val rotationDegree = PI / 180 * rotation

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(Path().apply {
        val r = min(size.height, size.width) * 0.5f

        val xCenter = size.width * 0.5f
        val yCenter = size.height * 0.5f

        moveTo(xCenter, yCenter)

        var t = -rotationDegree

        while (t <= 2 * PI) {
            lineTo((r * cos(t) + xCenter).toFloat(), (r * sin(t) + yCenter).toFloat())
            t += stepCount
        }

        lineTo((r * cos(t) + xCenter).toFloat(), (r * sin(t) + yCenter).toFloat())
    })
}