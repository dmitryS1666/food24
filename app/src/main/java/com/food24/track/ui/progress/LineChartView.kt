package com.food24.track.ui.progress

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3890F5"); strokeWidth = 6f; style = Paint.Style.STROKE
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334567"); strokeWidth = 1f
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8CCBFF"); style = Paint.Style.FILL
    }

    private var values: List<Float> = emptyList()

    fun setData(points: List<Float>) {
        values = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val left = paddingLeft + 12f
        val right = w - paddingRight - 12f
        val top = paddingTop + 12f
        val bottom = h - paddingBottom - 12f

        // grid
        val rows = 4
        val rowStep = (bottom - top) / rows
        for (i in 0..rows) {
            val y = bottom - i * rowStep
            canvas.drawLine(left, y, right, y, gridPaint)
        }

        val minV = values.minOrNull() ?: 0f
        val maxV = values.maxOrNull() ?: 1f
        val span = max(1e-3f, (maxV - minV)).toFloat()

        val dx = if (values.size <= 1) 0f else (right - left) / (values.size - 1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = left + dx * i
            val y = bottom - (v - minV) / span * (bottom - top)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            canvas.drawCircle(x, y, 5f, dotPaint)
        }
        canvas.drawPath(path, linePaint)
    }
}
