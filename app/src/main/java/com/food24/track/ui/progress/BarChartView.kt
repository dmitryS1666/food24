package com.food24.track.ui.progress

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class BarChartView @JvmOverloads constructor(
    c: Context, a: AttributeSet? = null
) : View(c, a) {

    private val values = mutableListOf<Float>()
    private var above = 0f
    private var below = 0f

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4C8CF6")
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22335F"); strokeWidth = 2f
    }
    private val guideAbove = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#8E3B3B") }
    private val guideWithin = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2A6E57") }
    private val guideBelow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#A36A2A") }

    fun setBars(bars: List<Float>, targetLow: Float, targetHigh: Float) {
        values.clear(); values.addAll(bars)
        below = targetLow; above = targetHigh
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return

        val left = paddingLeft + 24f
        val right = width - paddingRight - 24f
        val top = paddingTop + 24f
        val bottom = height - paddingBottom - 24f

        // guides (3 zones: below/within/above target)
        canvas.drawLine(left, bottom - (above - minVal()) / span() * (bottom - top), right, bottom - (above - minVal()) / span() * (bottom - top), guideAbove)
        canvas.drawLine(left, bottom - (below - minVal()) / span() * (bottom - top), right, bottom - (below - minVal()) / span() * (bottom - top), guideBelow)

        val barW = (right - left) / (values.size * 1.5f)
        var x = left + barW * 0.25f

        values.forEach { v ->
            val h = (v - minVal()) / span() * (bottom - top)
            canvas.drawRoundRect(x, bottom - h, x + barW, bottom, 12f, 12f, barPaint)
            x += barW * 1.5f
        }
    }

    private fun minVal(): Float = minOf(values.minOrNull() ?: 0f, below)
    private fun maxVal(): Float = max(values.maxOrNull() ?: 0f, above)
    private fun span(): Float = max(1f, maxVal() - minVal())
}
