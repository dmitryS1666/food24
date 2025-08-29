package com.food24.track.ui.progress

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class LineChartView @JvmOverloads constructor(
    c: Context, a: AttributeSet? = null
) : View(c, a) {

    data class Point(val x: Float, val y: Float)
    private val data = mutableListOf<Point>()

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22335F"); strokeWidth = 2f
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3890F5"); strokeWidth = 6f; style = Paint.Style.STROKE
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3890F5")
    }

    fun setPoints(points: List<Point>) {
        data.clear(); data.addAll(points); invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val left = paddingLeft + 24f
        val right = width - paddingRight - 24f
        val top = paddingTop + 24f
        val bottom = height - paddingBottom - 24f

        // grid (simple border)
        canvas.drawRoundRect(left, top, right, bottom, 24f, 24f, gridPaint)

        val minX = data.minOf { it.x }
        val maxX = data.maxOf { it.x }
        val minY = data.minOf { it.y }
        val maxY = data.maxOf { it.y }

        fun mapX(x: Float) =
            left + (x - minX) / max(1f, (maxX - minX)) * (right - left)
        fun mapY(y: Float) =
            bottom - (y - minY) / max(1f, (maxY - minY)) * (bottom - top)

        val path = Path()
        data.forEachIndexed { i, p ->
            val X = mapX(p.x); val Y = mapY(p.y)
            if (i == 0) path.moveTo(X, Y) else path.lineTo(X, Y)
        }
        canvas.drawPath(path, linePaint)

        data.forEach { p -> canvas.drawCircle(mapX(p.x), mapY(p.y), 8f, dotPaint) }
    }
}
