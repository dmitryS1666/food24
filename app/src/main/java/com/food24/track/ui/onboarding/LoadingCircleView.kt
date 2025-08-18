package com.food24.track.ui.onboarding

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class LoadingCircleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 30f
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.MAGENTA
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 30f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 120f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private var progress = 0 // 0–100

    fun setProgress(value: Int) {
        progress = value.coerceIn(0, 100)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = (width.coerceAtMost(height) / 2f) - 40f
        val centerX = width / 2f
        val centerY = height / 2f
        val rect = RectF(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius
        )

        // Фоновое кольцо
        canvas.drawArc(rect, 0f, 360f, false, backgroundPaint)

        // Прогресс дугой
        canvas.drawArc(rect, -90f, progress * 3.6f, false, progressPaint)

        // Текст
        canvas.drawText("$progress%", centerX, centerY + 30f, textPaint)
    }
}
