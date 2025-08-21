package com.food24.track.ui.progress

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class BarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3890F5")
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334567"); strokeWidth = 1f
    }

    private var values: List<Float> = emptyList()
    private var goal: Float = 0f

    fun setData(bars: List<Float>, goalLine: Float) {
        values = bars
        goal = goalLine
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val left = paddingLeft + 16f
        val right = w - paddingRight - 16f
        val top = paddingTop + 16f
        val bottom = h - paddingBottom - 24f

        // grid lines (below/within/above target)
        val third = (bottom - top) / 3f
        canvas.drawLine(left, top + third, right, top + third, gridPaint)
        canvas.drawLine(left, top + 2 * third, right, top + 2 * third, gridPaint)

        val maxV = max(values.maxOrNull() ?: goal, goal)
        val span = if (maxV <= 0f) 1f else maxV

        val count = values.size
        val gap = 12f
        val barWidth = ((right - left) - gap * (count + 1)) / count

        values.forEachIndexed { i, v ->
            val x1 = left + gap + i * (barWidth + gap)
            val x2 = x1 + barWidth
            val height = (v / span) * (bottom - top)
            val y1 = bottom - height
            canvas.drawRect(x1, y1, x2, bottom, barPaint)
        }
    }
}
